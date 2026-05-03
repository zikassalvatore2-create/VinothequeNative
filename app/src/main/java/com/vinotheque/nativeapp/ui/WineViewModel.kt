package com.vinotheque.nativeapp.ui

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vinotheque.nativeapp.data.AppDatabase
import com.vinotheque.nativeapp.data.Favorite
import com.vinotheque.nativeapp.data.Sale
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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import kotlinx.coroutines.flow.first
import android.util.Base64
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.io.FileOutputStream
import android.graphics.Bitmap
import android.graphics.BitmapFactory

data class EnrichmentResult(val type: String, val dryness: String, val aroma: String, val foodPairing: String, val glass: String, val decanting: String = "No decanting", val servingTemp: String = "12-14°C", val keywords: String = "")

data class RestoreProgress(
    val message: String = "",
    val progress: Int = 0,
    val max: Int = 100,
    val isRestoring: Boolean = false,
    val isComplete: Boolean = false,
    val error: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class WineViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).wineDao()
    private val favDao = AppDatabase.getDatabase(application).favoriteDao()
    private val saleDao = AppDatabase.getDatabase(application).saleDao()
    private val prefs = application.getSharedPreferences("vinotheque_prefs", Context.MODE_PRIVATE)
    private val appContext = application.applicationContext
    private var autoBackupJob: Job? = null

    private val _restoreProgress = MutableStateFlow(RestoreProgress())
    val restoreProgress: StateFlow<RestoreProgress> = _restoreProgress.asStateFlow()

    fun resetRestoreState() { _restoreProgress.value = RestoreProgress() }
    
    init {
        // Re-apply language on start
        val savedLang = prefs.getString("selected_language", "en") ?: "en"
        val appLocales = LocaleListCompat.forLanguageTags(savedLang)
        AppCompatDelegate.setApplicationLocales(appLocales)
    }

    val currentUser = MutableStateFlow(prefs.getString("current_user", "default") ?: "default")
    val isAdmin = MutableStateFlow(false) // Always OFF on cold start
    val allSales = saleDao.getAllSales().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val selectedTheme = MutableStateFlow(prefs.getString("selected_theme", "Midnight") ?: "Midnight")
    fun setTheme(theme: String) {
        selectedTheme.value = theme
        prefs.edit().putString("selected_theme", theme).apply()
    }

    val selectedLanguage = MutableStateFlow(prefs.getString("selected_language", "en") ?: "en")
    fun setLanguage(lang: String) {
        selectedLanguage.value = lang
        prefs.edit().putString("selected_language", lang).apply()
        // Update application locales
        val appLocales = androidx.core.os.LocaleListCompat.forLanguageTags(lang)
        androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(appLocales)
    }

    private var adminLastActivity = System.currentTimeMillis()
    private val ADMIN_TIMEOUT = 30 * 60 * 1000L // 30 minutes

    fun getAdminPin(): String = prefs.getString("admin_pin", "1970") ?: "1970"
    fun setAdminPin(newPin: String) { prefs.edit().putString("admin_pin", newPin).apply() }
    fun checkPin(pin: String): Boolean = pin == getAdminPin()
    fun isFirstLaunch(): Boolean = !prefs.contains("user_name_set")
    fun setNameSet() { prefs.edit().putBoolean("user_name_set", true).apply() }
    
    val imageQuality = MutableStateFlow(prefs.getInt("image_quality", 75))
    fun setImageQuality(q: Int) {
        imageQuality.value = q
        prefs.edit().putInt("image_quality", q).apply()
    }

    fun setAdmin(status: Boolean) {
        isAdmin.value = status
        if (status) adminLastActivity = System.currentTimeMillis()
    }
    fun touchAdmin() { adminLastActivity = System.currentTimeMillis() }
    fun checkAdminTimeout() {
        if (isAdmin.value && System.currentTimeMillis() - adminLastActivity > ADMIN_TIMEOUT) {
            isAdmin.value = false
        }
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
            
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            if (bitmap != null) {
                val file = File(imageDir, "img_$reference.webp")
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.WEBP, 85, out)
                }
                bitmap.recycle()
                file.absolutePath
            } else {
                base64
            }
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
                 image: String?, binLocation: String = "", glassType: String? = null,
                 tastingNotes: String = "", decanting: String = "", servingTemp: String = "",
                 ratingSource: String = "", keywords: String = "") {
        viewModelScope.launch {
            val newRef = "REF" + System.currentTimeMillis().toString()
            val fileImage = if (image != null && image.startsWith("data:image")) saveBase64ToFile(image, newRef) else image
            val finalImage = fileImage ?: allWines.value
                .firstOrNull { it.name.equals(name, ignoreCase = true) && it.image != null }?.image

            val enriched = enrichWine(grape, name)
            
            dao.insertWine(Wine(
                reference = newRef,
                name = name.ifEmpty { "Unknown Wine" }, region = region, vintage = vintage,
                grape = grape, type = type, dryness = dryness, price = price,
                rating = rating, aroma = aroma, foodPairing = foodPairing, image = finalImage,
                binLocation = binLocation,
                glassType = glassType ?: enriched.glass,
                tastingNotes = tastingNotes,
                decanting = decanting.ifEmpty { enriched.decanting },
                servingTemp = servingTemp.ifEmpty { enriched.servingTemp },
                ratingSource = ratingSource,
                keywords = keywords.ifEmpty { enriched.keywords }))

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

    private val _lastSaleId = MutableStateFlow<Long?>(null)

    /** Record a sale: increment wine sold counter and track per-user sales */
    fun sellWine(wine: Wine, quantity: Int = 1) {
        viewModelScope.launch {
            val sale = Sale(
                wineReference = wine.reference,
                wineName = wine.name,
                username = currentUser.value,
                timestamp = System.currentTimeMillis(),
                price = wine.price,
                quantity = quantity
            )
            val saleId = saleDao.insertSale(sale)
            
            // Also update the wine's sold counter
            dao.incrementSold(wine.reference, quantity)
            
            // Track the last sale ID for undo
            _lastSaleId.value = saleId
        }
    }

    /** Undo the last sale */
    fun undoLastSale() {
        viewModelScope.launch {
            val saleId = _lastSaleId.value ?: return@launch
            val sale = saleDao.getSaleById(saleId) ?: return@launch
            
            // Delete the sale
            saleDao.deleteSale(saleId)
            
            // Decrement the wine's sold counter
            dao.decrementSold(sale.wineReference, sale.quantity)
            
            _lastSaleId.value = null
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
                    if (g.contains("pinot noir") || g.contains("nebbiolo")) "Burgundy Glass" else "Bordeaux Glass",
                    if (g.contains("cabernet") || g.contains("syrah") || g.contains("shiraz") || g.contains("nebbiolo")) "Decant 1-2h" else "No decanting",
                    "16-18°C",
                    if (g.contains("cabernet")) "Powerful. Structured. Dark fruit." else if (g.contains("pinot noir")) "Elegant. Silky. Red fruit." else if (g.contains("nebbiolo")) "Complex. Tannic. Roses." else "Bold. Rich. Spice.")
            g.contains("chardonnay") || g.contains("sauvignon blanc") || g.contains("pinot grigio") ||
            g.contains("viognier") || g.contains("albarino") || g.contains("gruner") || g.contains("chenin") ->
                EnrichmentResult("White", "Dry", "Citrus, green apple, floral", "Seafood, poultry, salads",
                    if (g.contains("chardonnay")) "Oaked White Glass" else "White Wine Glass",
                    "No decanting", "8-12°C",
                    if (g.contains("chardonnay")) "Buttery. Oaky. Rich." else if (g.contains("sauvignon")) "Crisp. Fresh. Citrus." else "Light. Floral. Clean.")
            g.contains("riesling") -> EnrichmentResult("White", "Off-Dry", "Peach, lime, mineral", "Asian cuisine, pork, spicy dishes", "White Wine Glass", "No decanting", "6-10°C", "Aromatic. Mineral. Versatile.")
            g.contains("moscato") || g.contains("muscat") || g.contains("tokaji") || n.contains("port") ->
                EnrichmentResult("Dessert", "Sweet", "Honey, apricot, caramel", "Desserts, blue cheese, foie gras", "Dessert Glass", "No decanting", "10-14°C", "Luscious. Sweet. Golden.")
            g.contains("rose") || n.contains("rose") ->
                EnrichmentResult("Rose", "Dry", "Strawberry, melon, herbs", "Salads, seafood, light pasta", "White Wine Glass", "No decanting", "8-10°C", "Fresh. Summery. Delicate.")
            n.contains("champagne") || n.contains("prosecco") || n.contains("cava") || n.contains("sparkling") || n.contains("brut") ->
                EnrichmentResult("Sparkling", "Brut", "Citrus, toast, green apple", "Appetizers, seafood, celebration", "Champagne Flute", "No decanting", "6-8°C", "Festive. Crisp. Bubbles.")
            else -> EnrichmentResult("Red", "Dry", "Fruit, earth, spice", "Grilled meats, pasta, cheese", "Bordeaux Glass", "No decanting", "16-18°C", "Smooth. Balanced. Classic.")
        }
    }

    fun loadSampleData() {
        viewModelScope.launch {
            dao.insertAll(listOf(
                Wine("2665", "Aalto TE, Tempranillo, Bodegas Aalto, Ribero del Duero", "Sample Collection", "2016/23", "Tempranillo", "Red", "Dry", 92.0, 93, "Cherry, leather, dill, tobacco, dried fig", "Full-bodied. Rich. Concentrated.", "Beef, lamb, grilled meats", "", "Bin-1", 4, 4, 3, 1, 0, "Bordeaux Glass", "Yes (1-2 hours)", "16-18°C", "Wine Spectator / Vivino average"),
                Wine("2605", "Altrovino IGT (Merlot, Cab. Franc), Luca D´Attoma", "Sample Collection", "2019/20", "Merlot, Cabernet Franc", "Red", "Dry", 54.0, 93, "Plum, black cherry, chocolate, soft tannins", "Elegant. Silk. Balanced.", "Beef, lamb, grilled meats", "", "Bin-2", 3, 3, 3, 1, 0, "Bordeaux Glass", "Yes (1-2 hours)", "16-18°C", "Wine Spectator / Vivino average"),
                Wine("4250", "Altrovino IGT (Merlot, Cab. Franc), Luca D´Attoma", "Sample Collection", "2018", "Merlot, Cabernet Franc", "Red", "Dry", 120.0, 92, "Plum, black cherry, chocolate, soft tannins", "Deep. Intense. Structured.", "Beef, lamb, grilled meats", "", "Bin-3", 4, 4, 3, 1, 0, "Bordeaux Glass", "Yes (1-2 hours)", "16-18°C", "Wine Spectator / Vivino average"),
                Wine("2318", "Amarone Classico (Corvina+Rondinella, Oseleta), Allegrini", "Sample Collection", "2012", "Corvina, Rondinella, Oseleta", "Red", "Dry", 99.0, 91, "Dried cherry, raisin, spice", "Powerful. Warm. Complex.", "Blue cheese, game", "", "Bin-4", 5, 4, 3, 2, 0, "Bordeaux Glass", "Yes (1-2 hours)", "16-18°C", "Wine Spectator / Vivino average"),
                Wine("2319", "Amarone Classico (Corvina+Rondinella, Oseleta), Allegrini", "Sample Collection", "2010", "Corvina, Rondinella, Oseleta", "Red", "Dry", 106.0, 91, "Dried cherry, raisin, spice", "Mature. Velvet. Robust.", "Blue cheese, game", "", "Bin-5", 5, 4, 3, 2, 0, "Bordeaux Glass", "Yes (1-2 hours)", "16-18°C", "Wine Spectator / Vivino average"),
                Wine("2320", "Amarone Classico (Corvina+Rondinella, Oseleta), Allegrini", "Sample Collection", "2013", "Corvina, Rondinella, Oseleta", "Red", "Dry", 96.0, 92, "Dried cherry, raisin, spice", "Harmonious. Bold. Fruity.", "Blue cheese, game", "", "Bin-6", 5, 4, 3, 2, 0, "Bordeaux Glass", "Yes (1-2 hours)", "16-18°C", "Wine Spectator / Vivino average"),
                Wine("2321", "Amarone Classico (Corvina+Rondinella, Oseleta), Allegrini", "Sample Collection", "2015", "Corvina, Rondinella, Oseleta", "Red", "Dry", 110.0, 93, "Dried cherry, raisin, spice", "Structured. Long. Rich.", "Blue cheese, game", "", "Bin-7", 5, 4, 3, 2, 0, "Bordeaux Glass", "Yes (1-2 hours)", "16-18°C", "Wine Spectator / Vivino average"),
                Wine("2324", "Amarone della Valpolicella, Romano Dal Forno", "Sample Collection", "1997", "Corvina, Rondinella, Molinara", "Red", "Dry", 520.0, 92, "Dried cherry, raisin, spice", "Iconic. Legendary. Concentrated.", "Blue cheese, game", "", "Bin-8", 5, 5, 3, 3, 0, "Bordeaux Glass", "Yes (1-2 hours)", "16-18°C", "Wine Spectator / Vivino average"),
                Wine("2327", "Amarone della Valpolicella, Romano Dal Forno", "Sample Collection", "2004", "Corvina, Rondinella, Molinara", "Red", "Dry", 460.0, 92, "Dried cherry, raisin, spice", "Intense. Massive. Balanced.", "Blue cheese, game", "", "Bin-9", 5, 5, 3, 3, 0, "Bordeaux Glass", "Yes (1-2 hours)", "16-18°C", "Wine Spectator / Vivino average"),
                Wine("2652", "Artadi Cosecheros Pagos Vijos, Rioja Tinto", "Sample Collection", "2000", "Tempranillo", "Red", "Dry", 130.0, 93, "Cherry, leather, dill, tobacco, dried fig", "Classic. Earthy. Refined.", "Beef, lamb, grilled meats", "", "Bin-10", 4, 4, 3, 1, 0, "Bordeaux Glass", "Yes (1-2 hours)", "16-18°C", "Wine Spectator / Vivino average"),
                Wine("2091", "Barbaresco Asili Riserva, Bruno Giacosa", "Sample Collection", "2004", "Nebbiolo", "Red", "Dry", 360.0, 96, "Tar, roses, cherry, truffle", "Noble. Pure. Majestic.", "Truffle dishes, roast duck", "", "Bin-11", 4, 4, 4, 1, 0, "Burgundy Glass", "Yes (1-2 hours)", "16-18°C", "Wine Spectator / Vivino average"),
                Wine("2082", "Barbaresco Nubiola, Pelissero", "Sample Collection", "2017/18", "Nebbiolo", "Red", "Dry", 54.0, 91, "Tar, roses, cherry, truffle", "Modern. Accessible. Floral.", "Truffle dishes, roast duck", "", "Bin-12", 4, 3, 4, 1, 0, "Burgundy Glass", "Yes (1-2 hours)", "16-18°C", "Wine Spectator / Vivino average"),
                Wine("2093", "Barbaresco Tulin, Pelissero", "Sample Collection", "2016", "Nebbiolo", "Red", "Dry", 72.0, 93, "Tar, roses, cherry, truffle", "Structured. Precise. Elegant.", "Truffle dishes, roast duck", "", "Bin-13", 4, 4, 4, 1, 0, "Burgundy Glass", "Yes (1-2 hours)", "16-18°C", "Wine Spectator / Vivino average"),
                Wine("2087", "Barbaresco Vanotu, Pelissero", "Sample Collection", "2016", "Nebbiolo", "Red", "Dry", 92.0, 93, "Tar, roses, cherry, truffle", "Complex. Powerful. Long.", "Truffle dishes, roast duck", "", "Bin-14", 4, 4, 4, 1, 0, "Burgundy Glass", "Yes (1-2 hours)", "16-18°C", "Wine Spectator / Vivino average"),
                Wine("2080", "Barbera d'Alba, Piani, Pelissero", "Sample Collection", "2020/21", "Barbera", "Red", "Dry", 41.0, 91, "Fruit-forward, balanced acidity", "Zesty. Juicy. Vibrant.", "Hard cheeses, charcuterie", "", "Bin-15", 3, 2, 5, 1, 0, "Burgundy Glass", "No", "16-18°C", "Wine Spectator / Vivino average"),
                Wine("2144", "Barolo Bussia, Parusso", "Sample Collection", "2015", "Nebbiolo", "Red", "Dry", 135.0, 93, "Tar, roses, cherry, truffle", "Masculine. Deep. Savory.", "Truffle dishes, roast duck", "", "Bin-16", 5, 5, 4, 1, 0, "Burgundy Glass", "Yes (1-2 hours)", "16-18°C", "Wine Spectator / Vivino average"),
                Wine("2002", "Blauburgunder Mazzon, Gottardi", "Sample Collection", "2016/20", "Pinot Noir", "Red", "Dry", 48.0, 92, "Red cherry, earth, mushroom", "Delicate. Earthy. Silky.", "Salmon, mushroom risotto", "", "Bin-17", 3, 3, 4, 1, 0, "Burgundy Glass", "Optional (30 min)", "16-18°C", "Wine Spectator / Vivino average"),
                Wine("2571", "Bolgheri Rosso, Le Macchiole", "Sample Collection", "2022/23", "Merlot, Cab. Franc, Syrah", "Red", "Dry", 41.0, 93, "Plum, black cherry, chocolate", "Supple. Rich. Mediterranean.", "Beef, lamb, grilled meats", "", "Bin-18", 4, 3, 3, 1, 0, "Bordeaux Glass", "Yes (1-2 hours)", "16-18°C", "Wine Spectator / Vivino average"),
                Wine("2560", "Brunello di Montalcino, Il Marroneto", "Sample Collection", "2010", "Sangiovese", "Red", "Dry", 440.0, 93, "Cherry, tomato leaf, herb", "Ethereal. Pure. Traditional.", "Hard cheeses, charcuterie", "", "Bin-19", 4, 4, 4, 1, 0, "Bordeaux Glass", "No", "16-18°C", "Wine Spectator / Vivino average"),
                Wine("1092", "Chardonnay Baron Salvadori, Nals Margreid", "Sample Collection", "2022", "Chardonnay", "White", "Dry", 54.0, 93, "Apple, citrus, butter, oak", "Creamy. Oaked. Fresh.", "Seafood, poultry, creamy pasta", "", "Bin-20", 3, 0, 4, 1, 0, "Universal Glass", "No", "10-12°C", "Wine Spectator / Vivino average")
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
            o.put("tastingNotes", w.tastingNotes); o.put("decanting", w.decanting)
            o.put("servingTemp", w.servingTemp); o.put("ratingSource", w.ratingSource)
            o.put("keywords", w.keywords)
            // Convert image file path back to Base64 for the backup
            if (!w.image.isNullOrEmpty()) {
                val b64 = imagePathToBase64(w.image)
                if (b64 != null) o.put("image", b64)
                else o.put("image", w.image)
            } else {
                o.put("image", "")
            }
            wineArr.put(o)
        }
        root.put("wines", wineArr)
        
        val salesArr = JSONArray()
        allSales.value.forEach { s ->
            val so = JSONObject()
            so.put("ref", s.wineReference); so.put("name", s.wineName)
            so.put("user", s.username); so.put("time", s.timestamp)
            so.put("price", s.price); so.put("qty", s.quantity)
            salesArr.put(so)
        }
        root.put("sales", salesArr)
        root.put("currentUser", currentUser.value)
        
        return root.toString()
    }

    fun restoreFromJson(json: String) {
        viewModelScope.launch {
            _restoreProgress.value = RestoreProgress(isRestoring = true, message = "Preparing restore...")
            
            val result = withContext(Dispatchers.IO) {
                try {
                    val trimmed = json.trim()
                    if (trimmed.isEmpty()) return@withContext Result.failure(Exception("Backup is empty"))
                    
                    val wineArr: JSONArray
                    var salesArr: JSONArray? = null
                    var restoredUser: String? = null

                    if (trimmed.startsWith("{")) {
                        val root = JSONObject(trimmed)
                        wineArr = root.optJSONArray("wines") ?: JSONArray()
                        salesArr = root.optJSONArray("sales")
                        restoredUser = if (root.has("currentUser")) root.getString("currentUser") else null
                    } else {
                        wineArr = JSONArray(trimmed)
                        salesArr = null
                        restoredUser = null
                    }
                    
                    val total = wineArr.length()
                    val list = mutableListOf<Wine>()
                    val imageDir = File(appContext.filesDir, "wine_images")
                    if (!imageDir.exists()) imageDir.mkdirs()

                    for (i in 0 until total) {
                        val o = wineArr.getJSONObject(i)
                        val ref = o.optString("reference", "IMP_${System.currentTimeMillis()}_$i")
                        val importedImage = if (o.has("image")) o.getString("image") else null
                        
                        // Extract Base64 and save to file if necessary
                        val finalImage = if (importedImage != null && importedImage.startsWith("data:image")) {
                            saveBase64ToFile(importedImage, ref)
                        } else importedImage

                        val grape = o.optString("grape")
                        val name = o.optString("name")
                        val importedGlass = o.optString("glassType", "")
                        val enriched = enrichWine(grape, name)

                        list.add(Wine(
                            ref, name, o.optString("region"), o.optString("vintage"),
                            grape, o.optString("type", "Red"), o.optString("dryness", "Dry"),
                            o.optDouble("price", 0.0), o.optInt("rating", 90), o.optString("aroma"),
                            tastingNotes = o.optString("tastingNotes"),
                            foodPairing = o.optString("foodPairing"), peakMaturity = o.optString("peakMaturity"),
                            binLocation = o.optString("binLocation"),
                            sold = o.optInt("sold", 0),
                            glassType = if (importedGlass.isBlank()) enriched.glass else importedGlass,
                            decanting = o.optString("decanting").ifEmpty { enriched.decanting },
                            servingTemp = o.optString("servingTemp").ifEmpty { enriched.servingTemp },
                            ratingSource = o.optString("ratingSource"),
                            keywords = o.optString("keywords").ifEmpty { enriched.keywords },
                            image = finalImage))

                        if (i % 10 == 0 || i == total - 1) {
                            val progress = ((i.toFloat() / total) * 90).toInt()
                            _restoreProgress.value = RestoreProgress(
                                isRestoring = true,
                                message = "Restoring wine ${i + 1} of $total",
                                progress = progress
                            )
                        }
                    }

                    _restoreProgress.value = RestoreProgress(isRestoring = true, message = "Finalizing database...", progress = 95)
                    dao.deleteAll()
                    dao.insertAll(list)
                    
                    if (salesArr != null) {
                        val sList = mutableListOf<com.vinotheque.nativeapp.data.Sale>()
                        for (i in 0 until salesArr.length()) {
                            val so = salesArr.getJSONObject(i)
                            sList.add(com.vinotheque.nativeapp.data.Sale(
                                wineReference = so.optString("ref"),
                                wineName = so.optString("name"),
                                username = so.optString("user"),
                                timestamp = so.optLong("time"),
                                price = so.optDouble("price"),
                                quantity = so.optInt("qty", 1)
                            ))
                        }
                        saleDao.clearAllSales()
                        saleDao.insertAll(sList)
                    }
                    
                    if (restoredUser != null) {
                        withContext(Dispatchers.Main) { setUser(restoredUser) }
                    }
                    
                    Result.success("Restored $total wines successfully")
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

            _restoreProgress.value = if (result.isSuccess) {
                RestoreProgress(isComplete = true, message = result.getOrNull() ?: "Restore Complete")
            } else {
                RestoreProgress(error = result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }

    fun exportCsv(): String {
        val sb = StringBuilder()
        sb.appendLine("Name,Reference,Region,Vintage,Grape,Price,Type,Dryness,Rating,Aroma,TastingNotes,FoodPairing,BinLocation,GlassType,Decanting,ServingTemp,Keywords,RatingSource,Sold")
        for (w in allWinesUnfiltered.value) {
            sb.appendLine(csvEscape(w.name) + "," + csvEscape(w.reference) + "," + csvEscape(w.region) + "," +
                csvEscape(w.vintage) + "," + csvEscape(w.grape) + "," + w.price + "," + w.type + "," +
                w.dryness + "," + w.rating + "," + csvEscape(w.aroma) + "," + csvEscape(w.tastingNotes) + "," +
                csvEscape(w.foodPairing) + "," + csvEscape(w.binLocation) + "," + csvEscape(w.glassType) + "," +
                csvEscape(w.decanting) + "," + csvEscape(w.servingTemp) + "," + csvEscape(w.keywords) + "," +
                csvEscape(w.ratingSource) + "," + w.sold)
        }
        return sb.toString()
    }

    fun importCsv(csv: String, onResult: (added: Int, updated: Int, unchanged: Int) -> Unit = { _, _, _ -> }) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val allLines = csv.lines().filter { it.isNotBlank() }
                if (allLines.isEmpty()) return@launch
                val headerLine = allLines.first()
                val headers = parseCsvLine(headerLine).map { it.lowercase().trim() }
                val dataLines = allLines.drop(1)

                fun findIdx(names: List<String>, exclude: List<String> = emptyList()): Int {
                    // Try exact match first for ANY of the names
                    val exact = headers.indexOfFirst { h -> names.any { it.equals(h, ignoreCase = true) } }
                    if (exact >= 0) return exact
                    
                    // Try exact match without underscores/spaces
                    val exactNormalized = headers.indexOfFirst { h -> 
                        val hn = h.replace("_", "").replace(" ", "").lowercase()
                        names.any { n -> n.replace("_", "").replace(" ", "").lowercase() == hn }
                    }
                    if (exactNormalized >= 0) return exactNormalized

                    // Try partial match with exclusions
                    return headers.indexOfFirst { h -> 
                        names.any { n -> h.contains(n, ignoreCase = true) } &&
                        exclude.none { e -> h.contains(e, ignoreCase = true) }
                    }
                }

                val nameIdx = findIdx(listOf("name", "wine", "label", "nom"))
                val refIdx = findIdx(listOf("reference", "ref", "id", "sku", "code"))
                val regionIdx = findIdx(listOf("region", "appellation", "origin", "terroir", "pays"))
                val vintageIdx = findIdx(listOf("vintage", "year", "millésime", "millesime", "annee"))
                val grapeIdx = findIdx(listOf("grape", "varietal", "variety", "cépage", "cepage"))
                val priceIdx = findIdx(listOf("price", "cost", "prix", "valeur"))
                val typeIdx = findIdx(listOf("type", "color", "couleur", "wine_color"), exclude = listOf("glass"))
                val drynessIdx = findIdx(listOf("dryness", "sugar", "sucre", "dry"))
                val ratingIdx = findIdx(listOf("rating", "score", "points", "note"))
                val ratingSourceIdx = findIdx(listOf("rating_source", "source", "critic"))
                val aromaIdx = findIdx(listOf("aroma", "nose", "bouquet", "nez"))
                val pairingIdx = findIdx(listOf("pairing", "food", "accord", "plat"))
                val binIdx = findIdx(listOf("bin", "location", "shelf", "emplacement", "cave"))
                val keywordsIdx = findIdx(listOf("keywords", "selling", "tags", "mots"))
                val notesIdx = findIdx(listOf("notes", "tasting", "description", "degustation"))
                val glassIdx = findIdx(listOf("glass", "verre", "glass_type"))
                val decantIdx = findIdx(listOf("decant", "decanting", "carafe"))
                val tempIdx = findIdx(listOf("temp", "serving", "temperature", "service"))

                // Build a map of existing wines for upsert
                val existingWines = allWines.value.associateBy { it.reference }
                var added = 0; var updated = 0; var unchanged = 0

                for ((idx, line) in dataLines.withIndex()) {
                    val cols = parseCsvLine(line)
                    if (cols.size < 2) continue

                    val name = if (nameIdx >= 0 && nameIdx < cols.size) cols[nameIdx] else ""
                    val ref = if (refIdx >= 0 && refIdx < cols.size && cols[refIdx].isNotBlank()) cols[refIdx]
                              else "CSV" + System.currentTimeMillis().toString() + "_" + idx.toString()

                    fun col(i: Int) = if (i >= 0 && i < cols.size) cols[i] else ""

                    val existing = existingWines[ref]
                    val enriched = enrichWine(col(grapeIdx), name)

                    fun merge(csvVal: String, existingVal: String, fallback: String = ""): String =
                        csvVal.ifBlank { existingVal.ifBlank { fallback } }

                    val wine = Wine(
                        reference = ref,
                        name = merge(name, existing?.name ?: "").ifEmpty { "Unknown Wine" },
                        region = merge(col(regionIdx), existing?.region ?: ""),
                        vintage = merge(col(vintageIdx), existing?.vintage ?: ""),
                        grape = merge(col(grapeIdx), existing?.grape ?: ""),
                        type = merge(col(typeIdx), existing?.type ?: "", "Red"),
                        dryness = merge(col(drynessIdx), existing?.dryness ?: "", "Dry"),
                        price = col(priceIdx).replace(",", ".").toDoubleOrNull() ?: existing?.price ?: 0.0,
                        rating = col(ratingIdx).toIntOrNull() ?: existing?.rating ?: 90,
                        aroma = merge(col(aromaIdx), existing?.aroma ?: "", enriched.aroma),
                        foodPairing = merge(col(pairingIdx), existing?.foodPairing ?: "", enriched.foodPairing),
                        binLocation = merge(col(binIdx), existing?.binLocation ?: ""),
                        glassType = merge(col(glassIdx), existing?.glassType ?: "", enriched.glass),
                        decanting = merge(col(decantIdx), existing?.decanting ?: "", enriched.decanting),
                        servingTemp = merge(col(tempIdx), existing?.servingTemp ?: "", enriched.servingTemp),
                        ratingSource = merge(col(ratingSourceIdx), existing?.ratingSource ?: "User"),
                        keywords = merge(col(keywordsIdx), existing?.keywords ?: "", enriched.keywords),
                        tastingNotes = merge(col(notesIdx), existing?.tastingNotes ?: ""),
                        sold = existing?.sold ?: 0,
                        image = existing?.image
                    )

                    if (existing != null) {
                        if (wine != existing) { dao.updateWine(wine); updated++ } else unchanged++
                    } else { dao.insertWine(wine); added++ }
                }
                launch(Dispatchers.Main) { onResult(added, updated, unchanged) }
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
        var i = 0
        while (i < line.length) {
            val ch = line[i]
            when {
                ch == '"' -> {
                    if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                        current.append('"') // Escaped quote
                        i++
                    } else {
                        inQuotes = !inQuotes
                    }
                }
                ch == ',' && !inQuotes -> {
                    result.add(current.toString().trim())
                    current = StringBuilder()
                }
                else -> current.append(ch)
            }
            i++
        }
        result.add(current.toString().trim())
        return result
    }
}

