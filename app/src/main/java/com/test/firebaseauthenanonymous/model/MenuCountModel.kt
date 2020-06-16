package com.test.firebaseauthenanonymous.model

import com.google.gson.annotations.SerializedName

/**
 * Created by PrewSitthirat on 16/6/2020 AD.
 */

data class MenuCountModel(
    @SerializedName("count")
    var count: Int,
    @SerializedName("menu_id")
    val menu_id: String
)