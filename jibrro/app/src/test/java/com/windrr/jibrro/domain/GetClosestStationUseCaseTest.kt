package com.windrr.jibrro.domain

import com.windrr.jibrro.data.model.SubwayStation
import com.windrr.jibrro.domain.repository.StationRepository
import com.windrr.jibrro.domain.usecase.GetClosestStationUseCase
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class GetClosestStationUseCaseTest {
    inner class FakeStationRepository : StationRepository {

        private val stations = mutableListOf<SubwayStation>()

        fun addStation(station: SubwayStation) {
            stations.add(station)
        }

        override fun getSubwayStations(): List<SubwayStation> {
            return stations
        }
    }

    private lateinit var getClosestStationUseCase: GetClosestStationUseCase
    private lateinit var fakeStationRepository: FakeStationRepository

    @Before
    fun setUp() {
        fakeStationRepository = FakeStationRepository()
        getClosestStationUseCase = GetClosestStationUseCase(fakeStationRepository)

        val stationsToInsert = mutableListOf<SubwayStation>()

        ('A'..'Z').forEachIndexed { index, c ->
            stationsToInsert += SubwayStation(
                line = "L${(index % 9) + 1}",
                name = "Station $c",
                station_nm_chn = "站$c",
                station_nm_jpn = "ステーション$c",
                station_nm_eng = "Station $c",
                fr_code = "%02d-%02d".format((index % 9) + 1, index % 99),
                station_cd = (1000 + index).toString(),
                bldn_id = "B${10000 + index}",
                lat = if (index % 7 == 0) 0.0 else 37.5000 + index * 0.001,
                lng = if (index % 11 == 0) 0.0 else 127.0000 + index * 0.001
            )
        }

        stationsToInsert.shuffle()
        stationsToInsert.forEach {
            fakeStationRepository.addStation(it)
        }
    }

    @Test
    fun `returns one nearest station`() {
        val userLat = 37.5665
        val userLng = 126.9780

        val expected = SubwayStation(
            line = "L1",
            name = "Station ZERO",
            station_nm_chn = "站ZERO",
            station_nm_jpn = "ステーションZERO",
            station_nm_eng = "Station ZERO",
            fr_code = "01-00",
            station_cd = "9999",
            bldn_id = "B19999",
            lat = userLat,
            lng = userLng
        )

        val sub = SubwayStation(
            line = "L2",
            name = "Sub Station",
            station_nm_chn = "Sub Station",
            station_nm_jpn = "Sub Station",
            station_nm_eng = "Sub Station",
            fr_code = "01-01",
            station_cd = "9999",
            bldn_id = "B19999",
            lat = 57.5474,
            lng = 129.6453
        )
        fakeStationRepository.addStation(expected)
        fakeStationRepository.addStation(sub)

        val result = getClosestStationUseCase(userLat, userLng)

        assertNotNull(result, "가장 가까운 역이 null이면 안 됩니다.")
        assertEquals(expected.line, result.line, "가장 가까운 역이 기대한 역과 일치해야 합니다.")
    }
}

