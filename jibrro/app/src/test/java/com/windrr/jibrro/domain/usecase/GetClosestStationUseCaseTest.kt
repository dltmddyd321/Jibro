package com.windrr.jibrro.domain.usecase

import com.windrr.jibrro.data.model.SubwayStation
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import kotlin.math.abs

class GetClosestStationUseCaseTest {
    private lateinit var getClosestStationUseCase: GetClosestStationUseCase
    private val mockGetStationListUseCase: GetStationListUseCase = mockk()
    
    @Before
    fun setUp() {
        getClosestStationUseCase = GetClosestStationUseCase(mockGetStationListUseCase)
    }
    
    @Test
    fun `가장 가까운 역을 정확히 찾아야 한다`() {
        // Given
        val testStations = listOf(
            SubwayStation("1", "역삼역", 37.5006, 127.0365),  // ~3.2km from test position
            SubwayStation("2", "강남역", 37.4979, 127.0276),   // ~1.5km from test position (closest)
            SubwayStation("3", "교대역", 37.4934, 127.0142)    // ~3.0km from test position
        )
        
        every { mockGetStationListUseCase() } returns testStations
        
        // When: Test position near Gangnam Station
        val testLat = 37.4979
        val testLng = 127.0276
        val result = getClosestStationUseCase(testLat, testLng)
        
        // Then
        assertNotNull(result)
        assertEquals("강남역", result?.name)
    }
    
    @Test
    fun `유효하지 않은 좌표의 역은 제외해야 한다`() {
        // Given: One valid and two invalid stations (with 0,0 coordinates)
        val testStations = listOf(
            SubwayStation("1", "올바른역", 37.5006, 127.0365),
            SubwayStation("2", "잘못된역1", 0.0, 0.0),
            SubwayStation("3", "잘못된역2", 0.0, 0.0)
        )
        
        every { mockGetStationListUseCase() } returns testStations
        
        // When
        val result = getClosestStationUseCase(37.5006, 127.0365)
        
        // Then
        assertNotNull(result)
        assertEquals("올바른역", result?.name)
    }
    
    @Test
    fun `빈 리스트가 주어지면 null을 반환해야 한다`() {
        // Given
        every { mockGetStationListUseCase() } returns emptyList()
        
        // When
        val result = getClosestStationUseCase(37.5006, 127.0365)
        
        // Then
        assertNull(result)
    }
}
