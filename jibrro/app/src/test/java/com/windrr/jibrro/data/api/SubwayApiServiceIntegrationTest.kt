package com.windrr.jibrro.data.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.windrr.jibrro.BuildConfig
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
class SubwayApiServiceIntegrationTest {
    private lateinit var service: SubwayApiService
    private val BASE_URL = "http://swopenapi.seoul.go.kr/"

    @Before
    fun setUp() {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        service = retrofit.create(SubwayApiService::class.java)
    }

    @Test
    fun `getSubwayArrivalData returns successful response`() = runTest {
        val apiKey = "6a44734675646c7433366279584a64"
        val stationName = "강남"

        val response = service.getSubwayArrivalData(
            KEY = apiKey,
            statnNm = stationName
        )

        assertTrue("API request failed: ${response.message()}", response.isSuccessful)
        assertNotNull("Response body is null", response.body())
        
        val realtimeArrivalList = response.body()?.realtimeArrivalList
        assertNotNull("realtimeArrivalList is null", realtimeArrivalList)
        assertTrue("realtimeArrivalList is empty", realtimeArrivalList?.isNotEmpty() == true)
    }

    @Test
    fun `getSubwayArrivalData with invalid api key returns error`() = runTest {
        val invalidApiKey = "INVALID_KEY"
        val stationName = "강남"

        val response = service.getSubwayArrivalData(
            KEY = invalidApiKey,
            statnNm = stationName
        )

        assertFalse("Expected API call with invalid key to fail but it succeeded", response.isSuccessful)
    }
}
