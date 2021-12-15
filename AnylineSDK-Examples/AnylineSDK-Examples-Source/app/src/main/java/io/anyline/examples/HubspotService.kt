package io.anyline.examples

import com.google.gson.JsonObject
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface HubspotService {

    @POST("submissions/v3/integration/submit/$accountId/$guidContactUs?hapikey=$hApiKey")
    suspend fun contactUs(@Body requestBody: RequestBody): Response<JsonObject>

    @POST("submissions/v3/integration/submit/$accountId/$guidRequestSamples?hapikey=$hApiKey")
    suspend fun requestSamples(@Body requestBody: RequestBody): Response<JsonObject>

    companion object {
        private const val accountId = "24936173"
        private const val guidContactUs = "146a91f0-fa54-4331-a0b5-1ff786d30d11"
        private const val guidRequestSamples = "0c0166a7-ac60-4d12-a032-bbbdb2344153"
        private const val hApiKey = "eu1-9513-9d1d-459f-8f0b-5f9926b79c87"
    }
}