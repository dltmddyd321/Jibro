package com.windrr.jibrro.domain

import com.windrr.jibrro.data.model.SubwayStation
import com.windrr.jibrro.domain.repository.StationRepository
import com.windrr.jibrro.domain.usecase.GetClosestStationUseCase
import org.junit.Before

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
}

