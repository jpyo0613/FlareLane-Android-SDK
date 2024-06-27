package com.flarelane

import com.flarelane.HTTPClient.ResponseHandler
import com.flarelane.model.ModelInAppMessage
import org.json.JSONObject

internal object InAppService {
    @JvmStatic
    fun getMessage(projectId: String, deviceId: String, callback: (ModelInAppMessage?) -> Unit) {
        HTTPClient.get(
            "internal/v1/projects/$projectId/devices/$deviceId/in-app-messages",
            object : ResponseHandler() {
                override fun onSuccess(responseCode: Int, response: JSONObject) {
                    try {
                        val jsonArray = response.getJSONArray("data")
                        val jsonObject = jsonArray.getJSONObject(0)
                        val model = ModelInAppMessage(
                            id = jsonObject.getString("id"),
                            htmlString = jsonObject.getString("htmlString")
                        )
                        callback.invoke(model)
                    } catch (e: Exception) {
                        BaseErrorHandler.handle(e)
                    }
                }
            })
    }
}
