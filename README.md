# Jibro
인근 지하철역의 도착 정보를 확인하고 막차, 목적지 도착 알림을 받을 수 있는 서비스입니다.<br />
<br />
Play Store : https://play.google.com/store/apps/details?id=com.windrr.jibrro&pli=1
<br />
<br />
## 프로젝트 구조
<details>
<summary>폴더 트리 보기</summary>

```text
app/src/main/java/com/windrr/jibrro
├── data
│   ├── api
│   │   └── SubwayService.kt
│   ├── db
│   │   ├── SubwayDao.kt
│   │   └── SubwayDatabase.kt
│   ├── model
│   │   ├── AlarmInfo.kt
│   │   ├── CheckStation.kt
│   │   ├── Destination.kt
│   │   ├── ErrorMessage.kt
│   │   ├── RealtimeArrival.kt
│   │   ├── SubwayArrivalResponse.kt
│   │   ├── SubwayInfo.kt
│   │   └── SubwayStation.kt
│   ├── repository
│   │   ├── datasource
│   │   │   ├── StationDataSource.kt
│   │   │   ├── SubwayArrivalRemoteDataSource.kt
│   │   │   └── SubwayLocalDataSource.kt
│   │   ├── datasourceImpl
│   │   │   ├── SubwayArrivalRemoteDataSourceImpl.kt
│   │   │   └── SubwayLocalDataSourceImpl.kt
│   │   └── repositoryImpl
│   │       ├── AlarmRepositoryImpl.kt
│   │       ├── CheckStationRepositoryImpl.kt
│   │       ├── SettingsRepositoryImpl.kt
│   │       ├── StationRepositoryImpl.kt
│   │       └── SubwayRepositoryImpl.kt
│   └── util
│       ├── JibroWorker.kt
│       ├── Result.kt
│       ├── SettingDataStore.kt
│       └── SubwayInfo.kt
├── domain
│   ├── repository
│   │   ├── AlarmRepository.kt
│   │   ├── CheckStationRepository.kt
│   │   ├── SettingsRepository.kt
│   │   ├── StationRepository.kt
│   │   └── SubwayRepository.kt
│   └── usecase
│       ├── DeleteStationUseCase.kt
│       ├── GetCheckStationListUseCase.kt
│       ├── GetDestinationUseCase.kt
│       ├── GetLastTrainNotificationUseCase.kt
│       ├── GetStationListUseCase.kt
│       ├── GetSubwayArrivalDataUseCase.kt
│       ├── RegisterAlarmUseCase.kt
│       ├── SaveStationListUseCase.kt
│       ├── SetDestinationUseCase.kt
│       └── SetLastTrainNotificationUseCase.kt
├── infrastructure
│   ├── AppCore.kt
│   ├── BootReceiver.kt
│   ├── LocationForegroundService.kt
│   └── LocationHelper.kt
├── presentation
│   ├── activity
│   │   ├── LikeStationActivity.kt
│   │   ├── MainActivity.kt
│   │   ├── SettingsActivity.kt
│   │   ├── SplashActivity.kt
│   │   └── ui
│   │       └── theme
│   │           ├── Color.kt
│   │           ├── Theme.kt
│   │           └── Type.kt
│   ├── alarm
│   │   └── AlarmReceiver.kt
│   ├── component
│   │   ├── AlarmPermissionModal.kt
│   │   ├── BannerAdView.kt
│   │   ├── DestinationBanner.kt
│   │   └── LocationPermissionDialog.kt
│   ├── di
│   │   ├── DatabaseModule.kt
│   │   ├── LocalDataModule.kt
│   │   ├── LocationModule.kt
│   │   ├── NetworkModule.kt
│   │   ├── RemoteDataModule.kt
│   │   ├── RepositoryModule.kt
│   │   ├── StationModule.kt
│   │   └── UseCaseModule.kt
│   ├── ui
│   │   └── theme
│   │       ├── Color.kt
│   │       ├── Theme.kt
│   │       └── Type.kt
│   ├── viewmodel
│   │   ├── CheckStationViewModel.kt
│   │   ├── SettingsViewModel.kt
│   │   ├── StationViewModel.kt
│   │   └── SubwayArrivalDataViewModel.kt
│   └── widget
│       ├── ArrivalInfoWidget.kt
│       ├── ArrivalInfoWidgetReceiver.kt
│       └── action
│           └── RefreshAction.kt
└── util
    └── Action.kt
```
</details>

클린 아키텍처를 지향하는 형태로 구성되었습니다.

### Data : 실제 데이터의 저장, 네트워크 통신, 외부 데이터 소스와의 연결을 담당합니다.
  - repository/ : Domain 레이어의 Repository 인터페이스 구현체와 실제 로컬 및 API를 통해 데이터를 가져오는 Source로 구성되어 있습니다.
  - api/ : 서버 API 통신을 위한 Service가 구성
  - db/ : ROOM 데이터베이스와 DAO
  - model/ : 네트워크 통신 및 DB에 사용되는 각종 데이터 모델로 구성
  - util/ : 데이터 관련 유틸리티

### Domain : 외부 라이브러리나 프레임워크에 의존하지 않고, 앱의 비즈니스 로직들로 구성됩니다.
  - usecase/ : 하나의 기능 단위로 비즈니스 로직을 구현
  - repository/ : 데이터 접근을 추상화하는 인터페이스

### Infrastructure : 서비스, 브로드캐스트 리시버 등 안드로이드 시스템과 직접적으로 연관된 기능을 담당합니다.

### Presentation : 사용자 인터페이스(UI)와 관련된 코드들로 구성됩니다.
  - activity/ : 액티비티(화면 진입점)
  - component/ : Jetpack Compose 등 UI 컴포넌트
  - widget/ : 위젯 관련 코드
  - viewmodel/ : 화면 상태 및 UI 로직을 담당하는 ViewModel
<br />

## 사용 기술 스택
  - Retrofit: 서울 지하철 정보 api를 통해 데이터를 수신받고 앱에서 필요한 형태로 변환하는 역할
  - Hilt : 의존성 주입 자동화를 통해 수동으로 객체 생성 등의 보일러 플레이트 감소 역할
  - Room : 사용자가 즐겨찾기로 등록한 지하철 역을 로컬에 저장하고 관리하는 역할
  - Data Store : 사용자의 알람 설정 및 목적지 설정에 대한 정보를 로컬에 저장하고 관리하는 역할
  - WorkManaer : 백그라운드로 사용자 인근 지하철 역 도착 정보를 주기적으로 확인하여 막차 정보에 따라 알람을 보내는 역할
  - Compose & Glance : 인앱 UI 및 위젯을 구현하는데 사용
<br />

## 화면별 주요 기능
### MainActivity

<table width="100%">
  <tr valign="top">
    <td width="360">
      <img src="./screen_shot/main.jpeg" alt="MainActivity" width="320" />
    </td>

    <td style="padding-left:16px;">
      <strong>주요 기능</strong>
      <ul>
        <li>현재 이동 방향/행선지 배너 표시</li>
        <li>즐겨찾은 역의 실시간 도착 정보 카드</li>
        <li>새로고침 액션(우상단)</li>
        <li>위치/권한 상태에 따른 안내</li>
      </ul>

      <strong>상태 변화</strong>
      <ul>
        <li>위치 이동 시 배너 문구/색상 변경</li>
        <li>데이터 로딩/에러 토스트 또는 상태 뷰</li>
      </ul>

      <strong>탭/제스처</strong>
      <ul>
        <li>카드 탭 → 역 상세</li>
        <li>스와이프 새로고침 <em>(있다면)</em></li>
      </ul>
    </td>
  </tr>
</table>
