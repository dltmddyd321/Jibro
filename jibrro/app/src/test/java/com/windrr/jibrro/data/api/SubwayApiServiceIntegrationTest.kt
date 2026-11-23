package com.windrr.jibrro.data.api

import com.windrr.jibrro.BuildConfig
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class SubwayApiServiceIntegrationTest {
    private lateinit var service: SubwayApiService
    private val BASE_URL = "http://swopenapi.seoul.go.kr/"

    @Before
    fun setUp() {
        assumeTrue(System.getenv("JIBRO_RUN_LIVE_TESTS") == "1")
        check(BuildConfig.SUBWAY_API_KEY.isNotBlank()) { "Set SUBWAY_API_KEY to run live tests." }

        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        service = retrofit.create(SubwayApiService::class.java)
    }

    @Test
    fun `getSubwayArrivalData returns successful response`() = runTest {
        val stationName = "강남"

        val response = service.getSubwayArrivalData(
            KEY = BuildConfig.SUBWAY_API_KEY,
            statnNm = stationName
        )

        assertTrue("API request failed: ${response.message()}", response.isSuccessful)
        val realtimeArrivalList = response.body()?.realtimeArrivalList
        assertFalse("No live arrivals returned at 강남", realtimeArrivalList.isNullOrEmpty())
    }

    @Test
    fun `getSubwayArrivalData with invalid api key returns error`() = runTest {
        val invalidApiKey = "INVALID_KEY"
        val stationName = "강남"

        val response = service.getSubwayArrivalData(
            KEY = invalidApiKey,
            statnNm = stationName
        )

        val providerError = response.body()?.errorMessage?.code?.let { it != "INFO-000" } == true
        assertTrue("Expected HTTP or provider error for an invalid key", !response.isSuccessful || providerError)
    }
}
