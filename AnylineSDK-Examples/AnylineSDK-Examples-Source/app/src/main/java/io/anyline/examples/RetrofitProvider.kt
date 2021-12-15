package io.anyline.examples

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitProvider {

    private const val baseURL = "https://api.hsforms.com"

    private val retrofitInstance by lazy {
        Retrofit.Builder()
                .client(OkHttpClient.Builder().build())
                .baseUrl(baseURL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
    }

    fun provideRetrofitInstance(): Retrofit {
        return retrofitInstance
    }
}