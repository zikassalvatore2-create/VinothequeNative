package com.vinotheque.nativeapp.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vinotheque.nativeapp.data.AppDatabase
import com.vinotheque.nativeapp.data.Wine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class WineViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).wineDao()

    val wines: StateFlow<List<Wine>> = dao.getAllWines()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun saveWine(
        name: String,
        region: String,
        vintage: String,
        grape: String,
        price: Double,
        type: String,
        image: String?
    ) {
        viewModelScope.launch {
            dao.insertWine(
                Wine(
                    reference = "REF" + System.currentTimeMillis().toString(),
                    name = name.ifEmpty { "Unknown Wine" },
                    region = region,
                    vintage = vintage,
                    grape = grape,
                    type = type,
                    price = price,
                    image = image
                )
            )
        }
    }

    fun addSampleWine() {
        viewModelScope.launch {
            dao.insertWine(
                Wine(
                    reference = "REF" + System.currentTimeMillis().toString(),
                    name = "Chateau Margaux",
                    region = "Margaux, Bordeaux",
                    vintage = "2015",
                    grape = "Cabernet Sauvignon",
                    type = "Red",
                    price = 650.0,
                    rating = 98,
                    peakMaturity = "2025-2040",
                    binLocation = "Rack A1",
                    body = 4,
                    tannin = 4,
                    acidity = 3,
                    sweetness = 1,
                    image = null
                )
            )
        }
    }

    fun deleteWine(reference: String) {
        viewModelScope.launch {
            dao.deleteWine(reference)
        }
    }

    fun getBackupJson(): String {
        val wineList = wines.value
        val jsonArray = JSONArray()
        for (wine in wineList) {
            val obj = JSONObject()
            obj.put("reference", wine.reference)
            obj.put("name", wine.name)
            obj.put("region", wine.region)
            obj.put("vintage", wine.vintage)
            obj.put("grape", wine.grape)
            obj.put("type", wine.type)
            obj.put("price", wine.price)
            obj.put("rating", wine.rating)
            obj.put("peakMaturity", wine.peakMaturity)
            obj.put("binLocation", wine.binLocation)
            obj.put("body", wine.body)
            obj.put("tannin", wine.tannin)
            obj.put("acidity", wine.acidity)
            obj.put("sweetness", wine.sweetness)
            if (wine.image != null) {
                obj.put("image", wine.image)
            }
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
                    val obj = jsonArray.getJSONObject(i)
                    wineList.add(
                        Wine(
                            reference = obj.optString("reference", "REF" + System.currentTimeMillis().toString() + "_" + i.toString()),
                            name = obj.optString("name", ""),
                            region = obj.optString("region", ""),
                            vintage = obj.optString("vintage", ""),
                            grape = obj.optString("grape", ""),
                            type = obj.optString("type", "Red"),
                            price = obj.optDouble("price", 0.0),
                            rating = obj.optInt("rating", 90),
                            peakMaturity = obj.optString("peakMaturity", ""),
                            binLocation = obj.optString("binLocation", ""),
                            body = obj.optInt("body", 3),
                            tannin = obj.optInt("tannin", 3),
                            acidity = obj.optInt("acidity", 3),
                            sweetness = obj.optInt("sweetness", 1),
                            image = if (obj.has("image")) obj.getString("image") else null
                        )
                    )
                }
                dao.insertAll(wineList)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
