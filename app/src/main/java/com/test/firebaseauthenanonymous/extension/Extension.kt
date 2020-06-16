package com.test.firebaseauthenanonymous.extension

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Created by PrewSitthirat on 16/6/2020 AD.
 */

inline fun <reified T> Gson.fromJson(json: String) = this.fromJson<T>(json, object : TypeToken<T>() {}.type)