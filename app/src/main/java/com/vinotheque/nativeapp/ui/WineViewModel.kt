package com.vinotheque.nativeapp.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vinotheque.nativeapp.data.AppDatabase
import com.vinotheque.nativeapp.data.Wine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

data class EnrichmentResult(
    val type: String,
    val dryness: String,
    val aroma: String,
    val foodPairing: String
)

class WineViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).wineDao()

    private val allWines: StateFlow<List<Wine>> = dao.getAllWines()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val searchQuery = MutableStateFlow("")
    val typeFilter = MutableStateFlow("")
    val drynessFilter = MutableStateFlow("")

    val wines: StateFlow<List<Wine>> = combine(
        allWines, searchQuery, typeFilter, drynessFilter
    ) { list, query, type, dryness ->
        list.filter { wine ->
            val matchesQuery = query.isEmpty() ||
                wine.name.contains(query, ignoreCase = true) ||
                wine.grape.contains(query, ignoreCase = true) ||
                wine.region.contains(query, ignoreCase = true) ||
                wine.reference.contains(query, ignoreCase = true)
            val matchesType = type.isEmpty() || wine.type.equals(type, ignoreCase = true)
            val matchesDryness = dryness.isEmpty() || wine.dryness.equals(dryness, ignoreCase = true)
            matchesQuery && matchesType && matchesDryness
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWinesUnfiltered: StateFlow<List<Wine>> = allWines

    fun saveWine(
        name: String, region: String, vintage: String, grape: String,
        price: Double, type: String, dryness: String, rating: Int,
        aroma: String, foodPairing: String, image: String?
    ) {
        viewModelScope.launch {
            dao.insertWine(
                Wine(
                    reference = "REF" + System.currentTimeMillis().toString(),
                    name = name.ifEmpty { "Unknown Wine" },
                    region = region, vintage = vintage, grape = grape,
                    type = type, dryness = dryness, price = price,
                    rating = rating, aroma = aroma, foodPairing = foodPairing,
                    image = image
                )
            )
        }
    }

    fun updateWine(wine: Wine) {
        viewModelScope.launch { dao.updateWine(wine) }
    }

    fun deleteWine(reference: String) {
        viewModelScope.launch { dao.deleteWine(reference) }
    }

    fun clearAll() {
        viewModelScope.launch { dao.deleteAll() }
    }

    fun enrichWine(grape: String, name: String): EnrichmentResult {
        val g = grape.lowercase()
        val n = name.lowercase()
        return when {
            g.contains("cabernet") || g.contains("merlot") || g.contains("malbec") ||
            g.contains("syrah") || g.contains("shiraz") || g.contains("tempranillo") ||
            g.contains("sangiovese") || g.contains("nebbiolo") || g.contains("pinot noir") ||
            g.contains("grenache") || g.contains("zinfandel") || g.contains("carmenere") ->
                EnrichmentResult("Red", "Dry", "Dark fruits, oak, spice", "Beef, lamb, aged cheese")
            g.contains("chardonnay") || g.contains("sauvignon blanc") || g.contains("pinot grigio") ||
            g.contains("riesling") || g.contains("viognier") || g.contains("albarino") ||
            g.contains("gruner") || g.contains("chenin") || g.contains("vermentino") ->
                EnrichmentResult("White", if (g.contains("riesling")) "Off-Dry" else "Dry",
                    "Citrus, green apple, floral", "Seafood, poultry, salads")
            g.contains("moscato") || g.contains("muscat") || g.contains("tokaji") ||
            g.contains("sauternes") || n.contains("port") || n.contains("ice wine") ->
                EnrichmentResult("Dessert", "Sweet", "Honey, apricot, caramel", "Desserts, blue cheese, foie gras")
            g.contains("rose") || n.contains("rose") ->
                EnrichmentResult("Rose", "Dry", "Strawberry, melon, herbs", "Salads, seafood, light pasta")
            n.contains("champagne") || n.contains("prosecco") || n.contains("cava") ||
            n.contains("cremant") || n.contains("sparkling") || n.contains("brut") ->
                EnrichmentResult("Sparkling", "Brut", "Citrus, toast, green apple", "Appetizers, seafood, celebration")
            else -> EnrichmentResult("Red", "Dry", "Fruit, earth, spice", "Grilled meats, pasta, cheese")
        }
    }

    fun loadSampleData() {
        viewModelScope.launch {
            val samples = listOf(
                Wine("SAM001", "Chateau Margaux 2015", "Margaux, Bordeaux", "2015",
                    "Cabernet Sauvignon", "Red", "Dry", 650.0, 98,
                    "Blackcurrant, violet, cedar", "Prime ribeye, lamb rack", "2025-2040", "A1"),
                Wine("SAM002", "Dom Perignon 2012", "Champagne, France", "2012",
                    "Chardonnay/Pinot Noir", "Sparkling", "Brut", 220.0, 96,
                    "Citrus, brioche, almond", "Oysters, caviar, lobster"),
                Wine("SAM003", "Sassicaia 2018", "Bolgheri, Tuscany", "2018",
                    "Cabernet Sauvignon", "Red", "Dry", 280.0, 97,
                    "Black cherry, herbs, tobacco", "Florentine steak, wild boar"),
                Wine("SAM004", "Cloudy Bay Sauvignon Blanc", "Marlborough, NZ", "2022",
                    "Sauvignon Blanc", "White", "Dry", 28.0, 90,
                    "Passion fruit, lime, herbs", "Grilled fish, goat cheese"),
                Wine("SAM005", "Opus One 2019", "Napa Valley, USA", "2019",
                    "Cabernet Sauvignon", "Red", "Dry", 420.0, 97,
                    "Cassis, dark plum, vanilla", "Filet mignon, truffle risotto"),
                Wine("SAM006", "Whispering Angel Rose", "Provence, France", "2023",
                    "Grenache/Cinsault", "Rose", "Dry", 22.0, 88,
                    "Strawberry, peach, herbs", "Mediterranean salad, grilled shrimp"),
                Wine("SAM007", "Penfolds Grange 2017", "South Australia", "2017",
                    "Shiraz", "Red", "Dry", 750.0, 99,
                    "Plum, chocolate, spice", "Wagyu beef, dark chocolate dessert"),
                Wine("SAM008", "Chateau d'Yquem 2015", "Sauternes, Bordeaux", "2015",
                    "Semillon/Sauvignon Blanc", "Dessert", "Sweet", 380.0, 98,
                    "Honey, apricot, saffron", "Foie gras, creme brulee"),
                Wine("SAM009", "Barolo Giacomo Conterno", "Piedmont, Italy", "2016",
                    "Nebbiolo", "Red", "Dry", 320.0, 96,
                    "Rose, tar, cherry, truffle", "Braised veal, mushroom risotto"),
                Wine("SAM010", "Puligny-Montrachet 2020", "Burgundy, France", "2020",
                    "Chardonnay", "White", "Dry", 85.0, 93,
                    "Lemon, hazelnut, mineral", "Lobster thermidor, white fish")
            )
            dao.insertAll(samples)
        }
    }

    fun getBackupJson(): String {
        val wineList = allWinesUnfiltered.value
        val jsonArray = JSONArray()
        for (wine in wineList) {
            val obj = JSONObject()
            obj.put("reference", wine.reference)
            obj.put("name", wine.name)
            obj.put("region", wine.region)
            obj.put("vintage", wine.vintage)
            obj.put("grape", wine.grape)
            obj.put("type", wine.type)
            obj.put("dryness", wine.dryness)
            obj.put("price", wine.price)
            obj.put("rating", wine.rating)
            obj.put("aroma", wine.aroma)
            obj.put("foodPairing", wine.foodPairing)
            obj.put("peakMaturity", wine.peakMaturity)
            obj.put("binLocation", wine.binLocation)
            obj.put("body", wine.body)
            obj.put("tannin", wine.tannin)
            obj.put("acidity", wine.acidity)
            obj.put("sweetness", wine.sweetness)
            if (wine.image != null) obj.put("image", wine.image)
            jsonArray.put(obj)
        }
        return jsonArray.toString(2)
    }

    fun restoreFromJson(json: String) {
        viewModelScope.launch {
            try {
                val jsonArray = JSONArray(json)
                val wineList = mutableListOf<Wine>()
                for (i in 0 until jsonArray.length()) {
                    val o = jsonArray.getJSONObject(i)
                    wineList.add(Wine(
                        reference = o.optString("reference", "IMP" + System.currentTimeMillis().toString() + "_" + i.toString()),
                        name = o.optString("name", ""),
                        region = o.optString("region", ""),
                        vintage = o.optString("vintage", ""),
                        grape = o.optString("grape", ""),
                        type = o.optString("type", "Red"),
                        dryness = o.optString("dryness", "Dry"),
                        price = o.optDouble("price", 0.0),
                        rating = o.optInt("rating", 90),
                        aroma = o.optString("aroma", ""),
                        foodPairing = o.optString("foodPairing", ""),
                        peakMaturity = o.optString("peakMaturity", ""),
                        binLocation = o.optString("binLocation", ""),
                        body = o.optInt("body", 3),
                        tannin = o.optInt("tannin", 3),
                        acidity = o.optInt("acidity", 3),
                        sweetness = o.optInt("sweetness", 1),
                        image = if (o.has("image")) o.getString("image") else null
                    ))
                }
                dao.insertAll(wineList)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }
}
