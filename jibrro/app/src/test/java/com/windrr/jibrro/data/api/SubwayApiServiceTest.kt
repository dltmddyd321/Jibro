package com.windrr.jibrro.data.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.buffer
import okio.source
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.nio.charset.StandardCharsets

class SubwayApiServiceTest {
    private lateinit var service: SubwayApiService
    private lateinit var mockWebServer: MockWebServer

    companion object {
        private const val TEST_KEY = "test_key"
        private const val STATION_NAME = "강남"
        private const val STRANGE_NAME = "안드로메다"
    }

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        val client = OkHttpClient.Builder().build()

        service = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(SubwayApiService::class.java)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `getSubwayArrivalData returns expected response`() = runTest {
        val response = MockResponse()
            .setBodyFromFile("subway_arrival_response.json")
            .setResponseCode(200)

        mockWebServer.enqueue(response)

        val actualResponse = service.getSubwayArrivalData(
            KEY = TEST_KEY,
            statnNm = STATION_NAME
        )

        assertTrue(actualResponse.isSuccessful)
        assertNotNull(actualResponse.body())

        val request = mockWebServer.takeRequest()
        assertEquals("GET", request.method)

        val segments = request.requestUrl?.pathSegments
        assertEquals(
            listOf(
                "api", "subway", TEST_KEY, "json",
                "realtimeStationArrival", "0", "20", STATION_NAME
            ), segments
        )
    }

    @Test
    fun `getSubwayArrivalData with error returns error response`() = runTest {
        val response = MockResponse()
            .setResponseCode(404)
            .setBody("Not Found")

        mockWebServer.enqueue(response)

        val actualResponse = service.getSubwayArrivalData(
            KEY = TEST_KEY,
            statnNm = STRANGE_NAME
        )

        assertFalse(actualResponse.isSuccessful)
        assertEquals(404, actualResponse.code())
    }

    private fun MockResponse.setBodyFromFile(fileName: String): MockResponse {
        val inputStream = javaClass.classLoader?.getResourceAsStream(fileName)
        val source = inputStream?.source()?.buffer()
        val jsonString = source?.readString(StandardCharsets.UTF_8)

        jsonString?.let { setBody(it) }
        return this
    }
}
