package com.windrr.jibrro

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.windrr.jibrro.domain.commute.BoardingArrival
import com.windrr.jibrro.domain.commute.CommuteLeg
import com.windrr.jibrro.domain.commute.CommuteRoute
import com.windrr.jibrro.domain.commute.Coordinates
import com.windrr.jibrro.domain.commute.HomeLocation
import com.windrr.jibrro.domain.commute.TransitMode
import com.windrr.jibrro.presentation.commute.ArrivalDetails
import com.windrr.jibrro.presentation.commute.CommuteUiState
import com.windrr.jibrro.presentation.commute.homeRouteItems
import com.windrr.jibrro.presentation.ui.theme.JibrroTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CommuteLoadingInstrumentedTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun showsRouteAndArrivalLoadingUntilDataOrAnEmptyResultArrives() {
        val home = HomeLocation("집", Coordinates(37.369, 127.122))
        val origin = Coordinates(37.386, 127.112)
        val route = CommuteRoute(18, 2, listOf(CommuteLeg(TransitMode.BUS, "탑승 정류장", "하차 정류장")))
        val state = mutableStateOf<CommuteUiState>(CommuteUiState.Loading)
        var selected: ArrivalDetails? = null

        compose.setContent {
            JibrroTheme {
                LazyColumn {
                    homeRouteItems(
                        home, true, origin, null, false, state.value,
                        onEditHome = {}, onRequestLocation = {}, onRefresh = {}, onArrivalDetails = { selected = it }
                    )
                }
            }
        }

        compose.onNodeWithText("집으로 가는 경로를 찾는 중이에요.").assertIsDisplayed()

        compose.runOnIdle { state.value = CommuteUiState.Ready(listOf(route)) }
        compose.onNodeWithText("집으로 가는 경로를 찾는 중이에요.").assertDoesNotExist()
        compose.onNodeWithText("실시간 도착 확인 중").assertIsDisplayed()
        compose.onNodeWithText("실시간 도착 정보 없음").assertDoesNotExist()

        compose.runOnIdle {
            state.value = CommuteUiState.Ready(
                listOf(route), mapOf(route to BoardingArrival(180, 4)), loadingArrivals = false
            )
        }
        compose.onNodeWithText("실시간 도착 확인 중").assertDoesNotExist()
        compose.onNodeWithText("약 3분").assertIsDisplayed()
        compose.onNodeWithText("4정거장 전").assertIsDisplayed()
        compose.onNodeWithText("약 3분").performClick()
        compose.runOnIdle {
            assertEquals("탑승 정류장", selected?.stop)
            assertEquals("하차 정류장", selected?.alightingStop)
            assertEquals("약 3분", selected?.eta)
            assertEquals("4정거장 전", selected?.stopsAway)
            assertEquals(true, selected?.bus)
        }

        compose.runOnIdle {
            state.value = CommuteUiState.Ready(listOf(route), mapOf(route to null), loadingArrivals = false)
        }
        compose.onNodeWithText("실시간 도착 정보 없음").assertIsDisplayed()
        compose.onNodeWithText("실시간 도착 확인 중").assertDoesNotExist()
    }
}
