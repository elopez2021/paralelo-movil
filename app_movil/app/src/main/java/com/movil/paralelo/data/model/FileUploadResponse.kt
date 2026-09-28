package com.movil.paralelo.data.model

import com.google.gson.annotations.SerializedName

data class FileUploadResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("filename") val filename: String,
    @SerializedName("url") val url: String,
    @SerializedName("original_name") val originalName: String? = null,
    @SerializedName("size") val size: Int? = null,
    @SerializedName("createdAt") val createdAt: String? = null
)
