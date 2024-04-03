package com.flarelane.notification

import android.os.Bundle
import android.os.Parcelable
import com.flarelane.InteractionClass
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.json.JSONObject

@Parcelize
data class NotificationAction(
    @JvmField val id: String,
    @JvmField val url: String?,
    @JvmField val data: String?
) : Parcelable, InteractionClass {
    constructor(jsonObject: JSONObject) : this(
        jsonObject.getString("id"),
        if (jsonObject.has("url")) jsonObject.getString("url") else null,
        if (jsonObject.has("data")) jsonObject.getString("data") else null
    )

    override fun toHashMap(): HashMap<String, Any?> {
        return hashMapOf<String, Any?>().also {
            it["id"] = id
            it["url"] = url
            it["data"] = data
        }
    }

    override fun toBundle(): Bundle {
        return Bundle().also {
            it.putString("id", id)
            it.putString("url", url)
            it.putString("data", data)
        }
    }
}
