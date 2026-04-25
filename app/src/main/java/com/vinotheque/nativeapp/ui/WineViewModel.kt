package com.vinotheque.nativeapp.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vinotheque.nativeapp.data.AppDatabase
import com.vinotheque.nativeapp.data.Favorite
import com.vinotheque.nativeapp.data.Wine
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

data class EnrichmentResult(val type: String, val dryness: String, val aroma: String, val foodPairing: String)

@OptIn(ExperimentalCoroutinesApi::class)
class WineViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).wineDao()
    private val favDao = AppDatabase.getDatabase(application).favoriteDao()
    private val prefs = application.getSharedPreferences("vinotheque_prefs", Context.MODE_PRIVATE)

    val currentUser = MutableStateFlow(prefs.getString("current_user", "default") ?: "default")
    val searchQuery = MutableStateFlow("")
    val typeFilter = MutableStateFlow("")
    val drynessFilter = MutableStateFlow("")

    private val allWines: StateFlow<List<Wine>> = dao.getAllWines()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val wines: StateFlow<List<Wine>> = combine(allWines, searchQuery, typeFilter, drynessFilter) { list, q, t, d ->
        list.filter { w ->
            val mq = q.isEmpty() || w.name.contains(q, true) || w.grape.contains(q, true) ||
                w.region.contains(q, true) || w.reference.contains(q, true)
            val mt = t.isEmpty() || w.type.equals(t, true)
            val md = d.isEmpty() || w.dryness.equals(d, true)
            mq && mt && md
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWinesUnfiltered: StateFlow<List<Wine>> = allWines

    // Favorites
    private val userFavorites: StateFlow<List<Favorite>> = currentUser.flatMapLatest { user ->
        favDao.getFavorites(user)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteWines: StateFlow<List<Wine>> = combine(allWines, userFavorites) { wines, favs ->
        val favRefs = favs.map { it.wineReference }.toSet()
        wines.filter { it.reference in favRefs }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteRefs: StateFlow<Set<String>> = userFavorites.map { favs ->
        favs.map { it.wineReference }.toSet()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

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
                 type: String, dryness: String, rating: Int, aroma: String, foodPairing: String, image: String?) {
        viewModelScope.launch {
            dao.insertWine(Wine(
                reference = "REF" + System.currentTimeMillis().toString(),
                name = name.ifEmpty { "Unknown Wine" }, region = region, vintage = vintage,
                grape = grape, type = type, dryness = dryness, price = price,
                rating = rating, aroma = aroma, foodPairing = foodPairing, image = image))
        }
    }

    fun updateWine(wine: Wine) { viewModelScope.launch { dao.updateWine(wine) } }
    fun deleteWine(ref: String) { viewModelScope.launch { dao.deleteWine(ref) } }
    fun clearAll() { viewModelScope.launch { dao.deleteAll() } }

    fun getWinesByPairing(dish: String): List<Wine> {
        val d = dish.lowercase()
        return allWines.value.filter { w ->
            val fp = w.foodPairing.lowercase()
            val wt = w.type.lowercase()
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
        val g = grape.lowercase(); val n = name.lowercase()
        return when {
            g.contains("cabernet") || g.contains("merlot") || g.contains("malbec") || g.contains("syrah") ||
            g.contains("shiraz") || g.contains("tempranillo") || g.contains("sangiovese") ||
            g.contains("nebbiolo") || g.contains("pinot noir") || g.contains("grenache") || g.contains("zinfandel") ->
                EnrichmentResult("Red", "Dry", "Dark fruits, oak, spice", "Beef, lamb, aged cheese")
            g.contains("chardonnay") || g.contains("sauvignon blanc") || g.contains("pinot grigio") ||
            g.contains("viognier") || g.contains("albarino") || g.contains("gruner") || g.contains("chenin") ->
                EnrichmentResult("White", "Dry", "Citrus, green apple, floral", "Seafood, poultry, salads")
            g.contains("riesling") -> EnrichmentResult("White", "Off-Dry", "Peach, lime, mineral", "Asian cuisine, pork, spicy dishes")
            g.contains("moscato") || g.contains("muscat") || g.contains("tokaji") || n.contains("port") ->
                EnrichmentResult("Dessert", "Sweet", "Honey, apricot, caramel", "Desserts, blue cheese, foie gras")
            g.contains("rose") || n.contains("rose") ->
                EnrichmentResult("Rose", "Dry", "Strawberry, melon, herbs", "Salads, seafood, light pasta")
            n.contains("champagne") || n.contains("prosecco") || n.contains("cava") || n.contains("sparkling") || n.contains("brut") ->
                EnrichmentResult("Sparkling", "Brut", "Citrus, toast, green apple", "Appetizers, seafood, celebration")
            else -> EnrichmentResult("Red", "Dry", "Fruit, earth, spice", "Grilled meats, pasta, cheese")
        }
    }

    fun loadSampleData() {
        viewModelScope.launch {
            dao.insertAll(listOf(
                Wine("SAM001", "Chateau Margaux 2015", "Margaux, Bordeaux", "2015", "Cabernet Sauvignon", "Red", "Dry", 650.0, 98, "Blackcurrant, violet, cedar", "Prime ribeye, lamb rack", "2025-2040", "A1"),
                Wine("SAM002", "Dom Perignon 2012", "Champagne, France", "2012", "Chardonnay/Pinot Noir", "Sparkling", "Brut", 220.0, 96, "Citrus, brioche, almond", "Oysters, caviar, lobster"),
                Wine("SAM003", "Sassicaia 2018", "Bolgheri, Tuscany", "2018", "Cabernet Sauvignon", "Red", "Dry", 280.0, 97, "Black cherry, herbs, tobacco", "Florentine steak, wild boar"),
                Wine("SAM004", "Cloudy Bay Sauvignon Blanc", "Marlborough, NZ", "2022", "Sauvignon Blanc", "White", "Dry", 28.0, 90, "Passion fruit, lime, herbs", "Grilled fish, goat cheese"),
                Wine("SAM005", "Opus One 2019", "Napa Valley, USA", "2019", "Cabernet Sauvignon", "Red", "Dry", 420.0, 97, "Cassis, dark plum, vanilla", "Filet mignon, truffle risotto"),
                Wine("SAM006", "Whispering Angel Rose", "Provence, France", "2023", "Grenache/Cinsault", "Rose", "Dry", 22.0, 88, "Strawberry, peach, herbs", "Mediterranean salad, grilled shrimp"),
                Wine("SAM007", "Penfolds Grange 2017", "South Australia", "2017", "Shiraz", "Red", "Dry", 750.0, 99, "Plum, chocolate, spice", "Wagyu beef, dark chocolate dessert"),
                Wine("SAM008", "Chateau d'Yquem 2015", "Sauternes, Bordeaux", "2015", "Semillon/Sauvignon Blanc", "Dessert", "Sweet", 380.0, 98, "Honey, apricot, saffron", "Foie gras, creme brulee"),
                Wine("SAM009", "Barolo Giacomo Conterno", "Piedmont, Italy", "2016", "Nebbiolo", "Red", "Dry", 320.0, 96, "Rose, tar, cherry, truffle", "Braised veal, mushroom risotto"),
                Wine("SAM010", "Puligny-Montrachet 2020", "Burgundy, France", "2020", "Chardonnay", "White", "Dry", 85.0, 93, "Lemon, hazelnut, mineral", "Lobster thermidor, white fish")
            ))
        }
    }

    fun getBackupJson(): String {
        val arr = JSONArray()
        for (w in allWinesUnfiltered.value) {
            val o = JSONObject()
            o.put("reference", w.reference); o.put("name", w.name); o.put("region", w.region)
            o.put("vintage", w.vintage); o.put("grape", w.grape); o.put("type", w.type)
            o.put("dryness", w.dryness); o.put("price", w.price); o.put("rating", w.rating)
            o.put("aroma", w.aroma); o.put("foodPairing", w.foodPairing)
            o.put("peakMaturity", w.peakMaturity); o.put("binLocation", w.binLocation)
            if (w.image != null) o.put("image", w.image)
            arr.put(o)
        }
        return arr.toString(2)
    }

    fun restoreFromJson(json: String) {
        viewModelScope.launch {
            try {
                val arr = JSONArray(json)
                val list = mutableListOf<Wine>()
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    list.add(Wine(
                        o.optString("reference", "IMP" + System.currentTimeMillis().toString() + "_" + i.toString()),
                        o.optString("name"), o.optString("region"), o.optString("vintage"),
                        o.optString("grape"), o.optString("type", "Red"), o.optString("dryness", "Dry"),
                        o.optDouble("price", 0.0), o.optInt("rating", 90), o.optString("aroma"),
                        o.optString("foodPairing"), o.optString("peakMaturity"), o.optString("binLocation"),
                        image = if (o.has("image")) o.getString("image") else null))
                }
                dao.insertAll(list)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun exportCsv(): String {
        val sb = StringBuilder()
        sb.appendLine("Name,Region,Vintage,Grape,Type,Dryness,Price,Rating,Aroma,FoodPairing")
        for (w in allWinesUnfiltered.value) {
            sb.appendLine(csvEscape(w.name) + "," + csvEscape(w.region) + "," + w.vintage + "," +
                csvEscape(w.grape) + "," + w.type + "," + w.dryness + "," + w.price + "," + w.rating + "," +
                csvEscape(w.aroma) + "," + csvEscape(w.foodPairing))
        }
        return sb.toString()
    }

    fun importCsv(csv: String) {
        viewModelScope.launch {
            try {
                val lines = csv.lines().drop(1).filter { it.isNotBlank() }
                val list = mutableListOf<Wine>()
                for ((idx, line) in lines.withIndex()) {
                    val cols = parseCsvLine(line)
                    if (cols.size >= 8) {
                        list.add(Wine(
                            reference = "CSV" + System.currentTimeMillis().toString() + "_" + idx.toString(),
                            name = cols.getOrElse(0) { "" }, region = cols.getOrElse(1) { "" },
                            vintage = cols.getOrElse(2) { "" }, grape = cols.getOrElse(3) { "" },
                            type = cols.getOrElse(4) { "Red" }, dryness = cols.getOrElse(5) { "Dry" },
                            price = cols.getOrElse(6) { "0" }.toDoubleOrNull() ?: 0.0,
                            rating = cols.getOrElse(7) { "90" }.toIntOrNull() ?: 90,
                            aroma = cols.getOrElse(8) { "" }, foodPairing = cols.getOrElse(9) { "" }))
                    }
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
