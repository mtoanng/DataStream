package com.mtoanng.datastream.data.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FuelPriceDto(
    val id: Long,
    val eventTimestamp: String?,
    val fuelType: String,
    val price: Double,
    val priceUnit: String?,
    val location: String?,
    val region: String?,
    val source: String?,
)
