package com.vinotheque.nativeapp.ui

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vinotheque.nativeapp.data.AppDatabase
import com.vinotheque.nativeapp.data.Favorite
import com.vinotheque.nativeapp.data.Wine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import kotlinx.coroutines.flow.first
import android.util.Base64
import java.io.FileOutputStream
import android.graphics.Bitmap
import android.graphics.BitmapFactory

data class EnrichmentResult(val type: String, val dryness: String, val aroma: String, val foodPairing: String, val glass: String)

@OptIn(ExperimentalCoroutinesApi::class)
class WineViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).wineDao()
    private val favDao = AppDatabase.getDatabase(application).favoriteDao()
    private val saleDao = AppDatabase.getDatabase(application).saleDao()
    private val prefs = application.getSharedPreferences("vinotheque_prefs", Context.MODE_PRIVATE)
    private val appContext = application.applicationContext
    private var autoBackupJob: Job? = null

    val currentUser = MutableStateFlow(prefs.getString("current_user", "default") ?: "default")
    val isAdmin = MutableStateFlow(prefs.getBoolean("is_admin", false))
    val allSales = saleDao.getAllSales().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun setAdmin(status: Boolean) {
        isAdmin.value = status
        prefs.edit().putBoolean("is_admin", status).apply()
    }
    val searchQuery = MutableStateFlow("")
    val typeFilter = MutableStateFlow("")
    val drynessFilter = MutableStateFlow("")

    // Eagerly subscribe with Eagerly so data is always warm in memory
    private val allWines: StateFlow<List<Wine>> = dao.getAllWines()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val wines: StateFlow<List<Wine>> = combine(allWines, searchQuery, typeFilter, drynessFilter) { list, q, t, d ->
        list.filter { w ->
            val mq = q.isEmpty() || w.name.contains(q, true) || w.grape.contains(q, true) ||
                w.region.contains(q, true) || w.reference.contains(q, true) ||
                w.binLocation.contains(q, true)
            val mt = t.isEmpty() || w.type.replace("é", "e", true).equals(t.replace("é", "e", true), true)
            val md = d.isEmpty() || w.dryness.equals(d, true)
            mq && mt && md
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val allWinesUnfiltered: StateFlow<List<Wine>> = allWines

    // Favorites — eagerly subscribed
    private val userFavorites: StateFlow<List<Favorite>> = currentUser.flatMapLatest { user ->
        favDao.getFavorites(user)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val favoriteWines: StateFlow<List<Wine>> = combine(allWines, userFavorites) { wines, favs ->
        val favRefs = favs.map { it.wineReference }.toSet()
        wines.filter { it.reference in favRefs }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val favoriteRefs: StateFlow<Set<String>> = userFavorites.map { favs ->
        favs.map { it.wineReference }.toSet()
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    private fun saveBase64ToFile(base64: String, reference: String): String {
        if (!base64.startsWith("data:image")) return base64
        return try {
            val imageDir = File(appContext.filesDir, "wine_images")
            if (!imageDir.exists()) imageDir.mkdirs()
            val data = base64.substringAfter(",")
            val bytes = Base64.decode(data, Base64.DEFAULT)
            val file = File(imageDir, "img_$reference.webp")
            FileOutputStream(file).use { it.write(bytes) }
            file.absolutePath
        } catch (e: Exception) {
            base64
        }
    }

    private fun imagePathToBase64(path: String?): String? {
        if (path == null) return null
        if (path.startsWith("data:image")) return path
        return try {
            val file = File(path)
            if (file.exists()) {
                val bytes = file.readBytes()
                val b64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                val ext = path.substringAfterLast(".", "png").lowercase()
                val mime = if (ext == "jpg" || ext == "jpeg") "jpeg" else if (ext == "webp") "webp" else "png"
                "data:image/$mime;base64,$b64"
            } else null
        } catch (e: Exception) { null }
    }

    private fun runImageMigration() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val winesList = allWines.first()
                var migratedCount = 0
                for (wine in winesList) {
                    if (wine.image != null && wine.image.startsWith("data:image")) {
                        val path = saveBase64ToFile(wine.image, wine.reference)
                        if (path != wine.image) {
                            dao.updateWine(wine.copy(image = path))
                            migratedCount++
                        }
                    }
                }
                if (migratedCount > 0) Log.d("Vinotheque", "Migrated $migratedCount Base64 images to file system")
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    init {
        runImageMigration()
        // Watch for data changes and auto-backup with debounce
        viewModelScope.launch {
            allWines.collect { wines ->
                if (wines.isNotEmpty()) {
                    scheduleAutoBackup()
                }
            }
        }
    }

    /** 
     * Iterates through all wine images, detects black backgrounds,
     * and makes them transparent using the fixBlackBackground utility.
     */
    fun repairAllImages(onComplete: (Int) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            var repairedCount = 0
            try {
                val winesList = allWines.first()
                for (wine in winesList) {
                    val path = wine.image ?: continue
                    if (path.startsWith("data:image")) continue // Skip non-migrated
                    
                    val file = File(path)
                    if (file.exists()) {
                        val bitmap = BitmapFactory.decodeFile(file.absolutePath) ?: continue
                        
                        // Check if it's likely to have a black background (JPEG usually)
                        // Or just process everything to be safe
                        val fixedBitmap = fixBlackBackground(bitmap)
                        
                        // Save the fixed version back as WEBP (supports transparency)
                        FileOutputStream(file).use { out ->
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                                fixedBitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, 85, out)
                            } else {
                                @Suppress("DEPRECATION")
                                fixedBitmap.compress(Bitmap.CompressFormat.WEBP, 85, out)
                            }
                        }
                        repairedCount++
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
            
            launch(Dispatchers.Main) {
                onComplete(repairedCount)
            }
        }
    }

    /** Debounced auto-backup: waits 3s after last change, then saves to internal storage */
    private fun scheduleAutoBackup() {
        autoBackupJob?.cancel()
        autoBackupJob = viewModelScope.launch(Dispatchers.IO) {
            delay(3000) // Wait 3 seconds for more changes before saving
            try {
                val backupDir = File(appContext.filesDir, "auto_backup")
                if (!backupDir.exists()) backupDir.mkdirs()

                // Write current backup
                val file = File(backupDir, "vinotheque_auto.json")
                file.bufferedWriter().use { writer ->
                    writer.write(getBackupJson())
                }

                // Keep a rolling backup (previous version)
                val prev = File(backupDir, "vinotheque_auto_prev.json")
                if (file.exists() && file.length() > 0) {
                    val rollFile = File(backupDir, "vinotheque_auto_prev.json")
                    if (rollFile.exists()) rollFile.delete()
                    file.copyTo(prev, overwrite = true)
                }

                prefs.edit().putLong("last_auto_backup", System.currentTimeMillis()).apply()
                Log.d("Vinotheque", "Auto-backup saved: ${file.length() / 1024}KB")
            } catch (e: Exception) {
                Log.e("Vinotheque", "Auto-backup failed", e)
            }
        }
    }

    /** Get timestamp of last auto-backup */
    fun getLastBackupTime(): Long = prefs.getLong("last_auto_backup", 0)

    fun setUser(username: String) {
        currentUser.value = username
        prefs.edit().putString("current_user", username).apply()
    }

    fun toggleFavorite(wineRef: String) {
        viewModelScope.launch {
            val user = currentUser.value
            if (favoriteRefs.value.contains(wineRef)) {
                favDao.removeFavorite(user, wineRef)
            } else {
                favDao.addFavorite(Favorite(username = user, wineReference = wineRef))
            }
        }
    }

    fun saveWine(name: String, region: String, vintage: String, grape: String, price: Double,
                 type: String, dryness: String, rating: Int, aroma: String, foodPairing: String,
                 image: String?, binLocation: String = "", glassType: String? = null) {
        viewModelScope.launch {
            // If no image provided, inherit from a same-name wine that already has one
            val newRef = "REF" + System.currentTimeMillis().toString()
            val fileImage = if (image != null && image.startsWith("data:image")) saveBase64ToFile(image, newRef) else image
            val finalImage = fileImage ?: allWines.value
                .firstOrNull { it.name.equals(name, ignoreCase = true) && it.image != null }?.image

            val calculatedGlass = glassType ?: enrichWine(grape, name).glass
            
            dao.insertWine(Wine(
                reference = newRef,
                name = name.ifEmpty { "Unknown Wine" }, region = region, vintage = vintage,
                grape = grape, type = type, dryness = dryness, price = price,
                rating = rating, aroma = aroma, foodPairing = foodPairing, image = finalImage,
                binLocation = binLocation,
                glassType = calculatedGlass))

            // If we have an image, propagate to same-name wines that don't have one
            if (finalImage != null) {
                propagateImageToSameName(name, finalImage)
            }
        }
    }

    fun updateWine(wine: Wine) {
        viewModelScope.launch {
            val updatedWine = if (wine.image != null && wine.image.startsWith("data:image")) {
                wine.copy(image = saveBase64ToFile(wine.image, wine.reference))
            } else wine
            
            dao.updateWine(updatedWine)
            // If this wine has an image, propagate to all same-name wines
            if (updatedWine.image != null) {
                propagateImageToSameName(updatedWine.name, updatedWine.image)
            }
        }
    }

    /** Propagate image to all wines with the same name (different vintages) that lack an image or have a different one */
    private suspend fun propagateImageToSameName(wineName: String, image: String) {
        val sameNameWines = allWines.value.filter {
            it.name.equals(wineName, ignoreCase = true) && it.image != image
        }
        for (w in sameNameWines) {
            dao.updateWine(w.copy(image = image))
        }
    }

    fun deleteWine(ref: String) { viewModelScope.launch { dao.deleteWine(ref) } }
    fun clearAll() { 
        viewModelScope.launch { 
            dao.deleteAll()
            saleDao.clearAllSales()
        } 
    }

    /** Record a sale: increment wine sold counter and track per-user sales */
    fun sellWine(wine: Wine) {
        viewModelScope.launch {
            dao.updateWine(wine.copy(sold = wine.sold + 1))
            saleDao.insertSale(com.vinotheque.nativeapp.data.Sale(
                wineReference = wine.reference,
                wineName = wine.name,
                username = currentUser.value,
                timestamp = System.currentTimeMillis(),
                price = wine.price
            ))
        }
    }

    fun getUserSalesCount(): Int = prefs.getInt("sales_${currentUser.value}", 0)
    fun getUserRevenue(): Double = prefs.getFloat("revenue_${currentUser.value}", 0f).toDouble()

    fun getWinesByPairing(dish: String): List<Wine> {
        val d = dish.lowercase()
        return allWines.value.filter { w ->
            val fp = w.foodPairing.lowercase()
            val wt = w.type.lowercase().replace("é", "e")
            when {
                d.contains("beef") || d.contains("steak") || d.contains("lamb") ->
                    fp.contains("beef") || fp.contains("lamb") || fp.contains("steak") ||
                    fp.contains("meat") || wt == "red"
                d.contains("pork") -> fp.contains("pork") || fp.contains("meat") || wt == "red" || wt == "rose"
                d.contains("poultry") || d.contains("chicken") || d.contains("turkey") ->
                    fp.contains("poultry") || fp.contains("chicken") || wt == "white" || wt == "rose"
                d.contains("seafood") || d.contains("fish") || d.contains("shrimp") ->
                    fp.contains("seafood") || fp.contains("fish") || fp.contains("shrimp") ||
                    fp.contains("lobster") || wt == "white" || wt == "sparkling"
                d.contains("pasta") -> fp.contains("pasta") || fp.contains("risotto") || wt == "red" || wt == "white"
                d.contains("cheese") -> fp.contains("cheese") || wt == "red" || wt == "dessert"
                d.contains("dessert") || d.contains("chocolate") ->
                    fp.contains("dessert") || fp.contains("chocolate") || wt == "dessert" || wt == "sparkling"
                else -> fp.contains(d)
            }
        }
    }

    fun enrichWine(grape: String, name: String): EnrichmentResult {
        val g = grape.lowercase().replace("é", "e"); val n = name.lowercase().replace("é", "e")
        return when {
            g.contains("cabernet") || g.contains("merlot") || g.contains("malbec") || g.contains("syrah") ||
            g.contains("shiraz") || g.contains("tempranillo") || g.contains("sangiovese") ||
            g.contains("nebbiolo") || g.contains("pinot noir") || g.contains("grenache") || g.contains("zinfandel") ->
                EnrichmentResult("Red", "Dry", "Dark fruits, oak, spice", "Beef, lamb, aged cheese", 
                    if (g.contains("pinot noir") || g.contains("nebbiolo")) "Burgundy Glass" else "Bordeaux Glass")
            g.contains("chardonnay") || g.contains("sauvignon blanc") || g.contains("pinot grigio") ||
            g.contains("viognier") || g.contains("albarino") || g.contains("gruner") || g.contains("chenin") ->
                EnrichmentResult("White", "Dry", "Citrus, green apple, floral", "Seafood, poultry, salads",
                    if (g.contains("chardonnay")) "Oaked White Glass" else "White Wine Glass")
            g.contains("riesling") -> EnrichmentResult("White", "Off-Dry", "Peach, lime, mineral", "Asian cuisine, pork, spicy dishes", "White Wine Glass")
            g.contains("moscato") || g.contains("muscat") || g.contains("tokaji") || n.contains("port") ->
                EnrichmentResult("Dessert", "Sweet", "Honey, apricot, caramel", "Desserts, blue cheese, foie gras", "Dessert Glass")
            g.contains("rose") || n.contains("rose") ->
                EnrichmentResult("Rose", "Dry", "Strawberry, melon, herbs", "Salads, seafood, light pasta", "White Wine Glass")
            n.contains("champagne") || n.contains("prosecco") || n.contains("cava") || n.contains("sparkling") || n.contains("brut") ->
                EnrichmentResult("Sparkling", "Brut", "Citrus, toast, green apple", "Appetizers, seafood, celebration", "Champagne Flute")
            else -> EnrichmentResult("Red", "Dry", "Fruit, earth, spice", "Grilled meats, pasta, cheese", "Bordeaux Glass")
        }
    }

    fun loadSampleData() {
        viewModelScope.launch {
            dao.insertAll(listOf(
                Wine("SAM001", "Chateau Margaux 2015", "Margaux, Bordeaux", "2015", "Cabernet Sauvignon", "Red", "Dry", 650.0, 98, "Blackcurrant, violet, cedar", "Prime ribeye, lamb rack", "2025-2040", "A1, Shelf 1"),
                Wine("SAM002", "Dom Perignon 2012", "Champagne, France", "2012", "Chardonnay/Pinot Noir", "Sparkling", "Brut", 220.0, 96, "Citrus, brioche, almond", "Oysters, caviar, lobster", binLocation = "A2, Shelf 2"),
                Wine("SAM003", "Sassicaia 2018", "Bolgheri, Tuscany", "2018", "Cabernet Sauvignon", "Red", "Dry", 280.0, 97, "Black cherry, herbs, tobacco", "Florentine steak, wild boar", binLocation = "B1, Shelf 1"),
                Wine("SAM004", "Cloudy Bay Sauvignon Blanc", "Marlborough, NZ", "2022", "Sauvignon Blanc", "White", "Dry", 28.0, 90, "Passion fruit, lime, herbs", "Grilled fish, goat cheese", binLocation = "C1, Shelf 3"),
                Wine("SAM005", "Opus One 2019", "Napa Valley, USA", "2019", "Cabernet Sauvignon", "Red", "Dry", 420.0, 97, "Cassis, dark plum, vanilla", "Filet mignon, truffle risotto", binLocation = "A3, Shelf 1"),
                Wine("SAM006", "Whispering Angel Rose", "Provence, France", "2023", "Grenache/Cinsault", "Rose", "Dry", 22.0, 88, "Strawberry, peach, herbs", "Mediterranean salad, grilled shrimp", binLocation = "D1, Shelf 2"),
                Wine("SAM007", "Penfolds Grange 2017", "South Australia", "2017", "Shiraz", "Red", "Dry", 750.0, 99, "Plum, chocolate, spice", "Wagyu beef, dark chocolate dessert", binLocation = "A1, Shelf 2"),
                Wine("SAM008", "Chateau d'Yquem 2015", "Sauternes, Bordeaux", "2015", "Semillon/Sauvignon Blanc", "Dessert", "Sweet", 380.0, 98, "Honey, apricot, saffron", "Foie gras, creme brulee", binLocation = "B2, Shelf 1"),
                Wine("SAM009", "Barolo Giacomo Conterno", "Piedmont, Italy", "2016", "Nebbiolo", "Red", "Dry", 320.0, 96, "Rose, tar, cherry, truffle", "Braised veal, mushroom risotto", binLocation = "B1, Shelf 2"),
                Wine("SAM010", "Puligny-Montrachet 2020", "Burgundy, France", "2020", "Chardonnay", "White", "Dry", 85.0, 93, "Lemon, hazelnut, mineral", "Lobster thermidor, white fish", binLocation = "C2, Shelf 1")
            ))
        }
    }

    fun getBackupJson(): String {
        val root = JSONObject()
        val wineArr = JSONArray()
        val wineList = allWinesUnfiltered.value
        for (w in wineList) {
            val o = JSONObject()
            o.put("reference", w.reference); o.put("name", w.name); o.put("region", w.region)
            o.put("vintage", w.vintage); o.put("grape", w.grape); o.put("type", w.type)
            o.put("dryness", w.dryness); o.put("price", w.price); o.put("rating", w.rating)
            o.put("aroma", w.aroma); o.put("foodPairing", w.foodPairing)
            o.put("peakMaturity", w.peakMaturity); o.put("binLocation", w.binLocation)
            o.put("sold", w.sold); o.put("glassType", w.glassType)
            if (w.image != null) {
                val b64 = imagePathToBase64(w.image)
                if (b64 != null) o.put("image", b64)
            }
            wineArr.put(o)
        }
        root.put("wines", wineArr)
        
        val salesArr = JSONArray()
        allSales.value.forEach { s ->
            val so = JSONObject()
            so.put("ref", s.wineReference); so.put("name", s.wineName)
            so.put("user", s.username); so.put("time", s.timestamp)
            so.put("price", s.price)
            salesArr.put(so)
        }
        root.put("sales", salesArr)
        root.put("currentUser", currentUser.value)
        
        return root.toString()
    }

    fun restoreFromJson(json: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val root = JSONObject(json)
                val wineArr = if (root.has("wines")) root.getJSONArray("wines") else JSONArray(json) // Backwards compatibility
                
                val list = mutableListOf<Wine>()
                for (i in 0 until wineArr.length()) {
                    val o = wineArr.getJSONObject(i)
                    val importedImage = if (o.has("image")) o.getString("image") else null
                    val ref = o.optString("reference", "IMP" + System.currentTimeMillis().toString() + "_" + i.toString())
                    val finalImage = if (importedImage != null && importedImage.startsWith("data:image")) {
                        saveBase64ToFile(importedImage, ref)
                    } else importedImage

                    val grape = o.optString("grape")
                    val name = o.optString("name")
                    val importedGlass = o.optString("glassType", "")
                    val glassToUse = if (importedGlass.isBlank()) enrichWine(grape, name).glass else importedGlass

                    list.add(Wine(
                        ref,
                        name, o.optString("region"), o.optString("vintage"),
                        grape, o.optString("type", "Red"), o.optString("dryness", "Dry"),
                        o.optDouble("price", 0.0), o.optInt("rating", 90), o.optString("aroma"),
                        o.optString("foodPairing"), o.optString("peakMaturity"), o.optString("binLocation"),
                        sold = o.optInt("sold", 0),
                        glassType = glassToUse,
                        image = finalImage))
                }
                dao.deleteAll()
                dao.insertAll(list)
                
                if (root.has("sales")) {
                    val salesArr = root.getJSONArray("sales")
                    val sList = mutableListOf<com.vinotheque.nativeapp.data.Sale>()
                    for (i in 0 until salesArr.length()) {
                        val so = salesArr.getJSONObject(i)
                        sList.add(com.vinotheque.nativeapp.data.Sale(
                            wineReference = so.optString("ref"),
                            wineName = so.optString("name"),
                            username = so.optString("user"),
                            timestamp = so.optLong("time"),
                            price = so.optDouble("price")
                        ))
                    }
                    saleDao.clearAllSales()
                    saleDao.insertAll(sList)
                }
                
                if (root.has("currentUser")) {
                    val user = root.getString("currentUser")
                    launch(Dispatchers.Main) { setUser(user) }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun exportCsv(): String {
        val sb = StringBuilder()
        sb.appendLine("Name,Reference,Region,Vintage,Grape,Price,Type,Dryness,Rating,Aroma,FoodPairing,BinLocation,Sold")
        for (w in allWinesUnfiltered.value) {
            sb.appendLine(csvEscape(w.name) + "," + csvEscape(w.reference) + "," + csvEscape(w.region) + "," +
                csvEscape(w.vintage) + "," + csvEscape(w.grape) + "," + w.price + "," + w.type + "," +
                w.dryness + "," + w.rating + "," + csvEscape(w.aroma) + "," + csvEscape(w.foodPairing) + "," +
                csvEscape(w.binLocation) + "," + w.sold)
        }
        return sb.toString()
    }

    fun importCsv(csv: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val allLines = csv.lines().filter { it.isNotBlank() }
                if (allLines.isEmpty()) return@launch
                val headerLine = allLines.first()
                val headers = parseCsvLine(headerLine).map { it.lowercase().trim() }
                val dataLines = allLines.drop(1)

                val nameIdx = headers.indexOfFirst { it.contains("name") }
                val refIdx = headers.indexOfFirst { it.contains("ref") }
                val regionIdx = headers.indexOfFirst { it.contains("region") }
                val vintageIdx = headers.indexOfFirst { it.contains("vintage") || it.contains("year") }
                val grapeIdx = headers.indexOfFirst { it.contains("grape") || it.contains("varietal") || it.contains("variety") }
                val priceIdx = headers.indexOfFirst { it.contains("price") }
                val typeIdx = headers.indexOfFirst { it.contains("type") }
                val drynessIdx = headers.indexOfFirst { it.contains("dry") }
                val ratingIdx = headers.indexOfFirst { it.contains("rating") || it.contains("score") || it.contains("point") }
                val aromaIdx = headers.indexOfFirst { it.contains("aroma") || it.contains("nose") || it.contains("bouquet") }
                val pairingIdx = headers.indexOfFirst { it.contains("pair") || it.contains("food") }
                val binIdx = headers.indexOfFirst { it.contains("bin") || it.contains("location") || it.contains("shelf") }

                val list = mutableListOf<Wine>()
                for ((idx, line) in dataLines.withIndex()) {
                    val cols = parseCsvLine(line)
                    if (cols.size < 2) continue

                    val name = if (nameIdx >= 0 && nameIdx < cols.size) cols[nameIdx] else ""
                    val ref = if (refIdx >= 0 && refIdx < cols.size && cols[refIdx].isNotBlank()) cols[refIdx]
                              else "CSV" + System.currentTimeMillis().toString() + "_" + idx.toString()
                    val region = if (regionIdx >= 0 && regionIdx < cols.size) cols[regionIdx] else ""
                    val vintage = if (vintageIdx >= 0 && vintageIdx < cols.size) cols[vintageIdx] else ""
                    val grape = if (grapeIdx >= 0 && grapeIdx < cols.size) cols[grapeIdx] else ""
                    val priceStr = if (priceIdx >= 0 && priceIdx < cols.size) cols[priceIdx] else "0"
                    val type = if (typeIdx >= 0 && typeIdx < cols.size && cols[typeIdx].isNotBlank()) cols[typeIdx] else "Red"
                    val dryness = if (drynessIdx >= 0 && drynessIdx < cols.size && cols[drynessIdx].isNotBlank()) cols[drynessIdx] else "Dry"
                    val ratingStr = if (ratingIdx >= 0 && ratingIdx < cols.size) cols[ratingIdx] else "90"
                    val aroma = if (aromaIdx >= 0 && aromaIdx < cols.size) cols[aromaIdx] else ""
                    val pairing = if (pairingIdx >= 0 && pairingIdx < cols.size) cols[pairingIdx] else ""
                    val bin = if (binIdx >= 0 && binIdx < cols.size) cols[binIdx] else ""

                    val enriched = enrichWine(grape, name)

                    list.add(Wine(
                        reference = ref,
                        name = name.ifEmpty { "Unknown Wine" },
                        region = region, vintage = vintage, grape = grape,
                        type = type, dryness = dryness,
                        price = priceStr.replace(",", ".").toDoubleOrNull() ?: 0.0,
                        rating = ratingStr.toIntOrNull() ?: 90,
                        aroma = aroma.ifEmpty { enriched.aroma },
                        foodPairing = pairing.ifEmpty { enriched.foodPairing },
                        binLocation = bin,
                        glassType = enriched.glass
                    ))
                }
                dao.insertAll(list)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private fun csvEscape(s: String): String {
        return if (s.contains(",") || s.contains("\"")) {
            "\"" + s.replace("\"", "\"\"") + "\""
        } else s
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var current = StringBuilder()
        var inQuotes = false
        for (ch in line) {
            when {
                ch == '"' -> inQuotes = !inQuotes
                ch == ',' && !inQuotes -> { result.add(current.toString().trim()); current = StringBuilder() }
                else -> current.append(ch)
            }
        }
        result.add(current.toString().trim())
        return result
    }
}

