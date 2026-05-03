package com.vinotheque.nativeapp.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.vinotheque.nativeapp.R

@Composable
fun getLocalizedType(type: String): String {
    return when (type.lowercase().replace("é", "e")) {
        "red" -> stringResource(R.string.type_red)
        "white" -> stringResource(R.string.type_white)
        "rose" -> stringResource(R.string.type_rose)
        "sparkling" -> stringResource(R.string.type_sparkling)
        "dessert" -> stringResource(R.string.type_dessert)
        else -> type
    }
}

@Composable
fun getLocalizedDryness(dryness: String): String {
    return when (dryness.lowercase().replace("-", " ")) {
        "dry" -> stringResource(R.string.dry)
        "medium dry" -> stringResource(R.string.medium_dry)
        "sweet" -> stringResource(R.string.sweet)
        "off dry" -> stringResource(R.string.off_dry)
        else -> dryness
    }
}

@Composable
fun getLocalizedLabel(label: String): String {
    return when (label.lowercase()) {
        "price" -> stringResource(R.string.price)
        "vintage" -> stringResource(R.string.vintage)
        "sold" -> stringResource(R.string.sold)
        "reference" -> stringResource(R.string.reference)
        "grape" -> stringResource(R.string.grape)
        "name" -> stringResource(R.string.name)
        "region" -> stringResource(R.string.region)
        "type" -> stringResource(R.string.type_label)
        "dry" -> stringResource(R.string.dry_label)
        "rating" -> stringResource(R.string.rating_stat)
        "aroma" -> stringResource(R.string.aroma_label)
        "pairing" -> stringResource(R.string.pairing_label)
        "recommended glass" -> stringResource(R.string.glass_recommended)
        "bin/location" -> stringResource(R.string.bin_loc_label)
        "decant" -> stringResource(R.string.decant_label)
        "temp" -> stringResource(R.string.temp_label)
        "keywords" -> stringResource(R.string.keywords_label)
        "tasting notes" -> stringResource(R.string.tasting_notes)
        "aroma profile" -> stringResource(R.string.aroma_profile)
        "food pairing" -> stringResource(R.string.food_pairing)
        "peak maturity" -> stringResource(R.string.peak_maturity)
        "rating source" -> stringResource(R.string.rating_source)
        "glass" -> stringResource(R.string.glass)
        "decanting" -> stringResource(R.string.decanting)
        "serving temp" -> stringResource(R.string.serving_temp)
        else -> label
    }
}

@Composable
fun getLocalizedDish(dish: String): String {
    return when (dish.lowercase()) {
        "beef" -> stringResource(R.string.beef)
        "lamb" -> stringResource(R.string.lamb)
        "pork" -> stringResource(R.string.pork)
        "poultry" -> stringResource(R.string.poultry)
        "seafood" -> stringResource(R.string.seafood)
        "pasta" -> stringResource(R.string.pasta)
        "cheese" -> stringResource(R.string.cheese)
        "dessert" -> stringResource(R.string.dessert)
        else -> dish
    }
}
