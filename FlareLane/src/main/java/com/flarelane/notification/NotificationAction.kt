package com.flarelane.notification

import android.os.Bundle
import android.os.Parcelable
import com.flarelane.InteractionClass
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.json.JSONObject

@Parcelize
data class NotificationAction(
    @JvmField val type: NotificationActionType,
    internal val id: String,
    @JvmField val url: String?,
    @JvmField val data: String?
) : Parcelable, InteractionClass {
    override fun toHashMap(): HashMap<String, Any?> {
        return hashMapOf<String, Any?>().also {
            it["type"] = type.name
            it["actionId"] = id
            it["url"] = url
            it["data"] = data
        }
    }

    override fun toBundle(): Bundle {
        return Bundle().also {
            it.putString("type", type.name)
            it.putString("actionId", id)
            it.putString("url", url)
            it.putString("data", data)
        }
    }

    companion object {
        internal fun create(
            actionType: NotificationActionType,
            jsonObject: JSONObject
        ) = NotificationAction(
            actionType,
            jsonObject.getString("actionId"),
            if (jsonObject.has("url")) jsonObject.getString("url") else null,
            if (jsonObject.has("data")) jsonObject.getString("data") else null
        )
    }
}
