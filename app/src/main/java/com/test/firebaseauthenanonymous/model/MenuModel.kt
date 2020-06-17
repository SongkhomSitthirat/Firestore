package com.test.firebaseauthenanonymous.model

import com.google.gson.annotations.SerializedName

/**
 * Created by PrewSitthirat on 17/6/2020 AD.
 */

data class MenuModel(
    @SerializedName("key")
    val key: String?,
    @SerializedName("title")
    val title: HashMap<String, String>?
)