package kr.ac.tukorea.mapsie

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var actionBar : ActionBar?

        actionBar = supportActionBar
        actionBar?.hide()

        val mapView = MapView(this)

        val mapViewContainer = findViewById<View>(R.id.map_view) as ViewGroup
        mapViewContainer.addView(mapView)

        mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(37.38003556741972, 126.80284928809695), true) // 지도 첫화면 시흥시청

        mapView.zoomIn(true)  // 줌 인
        mapView.zoomOut(true)  // 줌 아웃

        val marker = MapPOIItem()
        val mapPoint = MapPoint.mapPointWithGeoCoord(37.38003556741972, 126.80284928809695)
        marker.itemName = "마커 표시 테스트 (시흥시청)"
        marker.tag = 0
        marker.mapPoint = mapPoint
        marker.markerType = MapPOIItem.MarkerType.RedPin
        marker.selectedMarkerType = MapPOIItem.MarkerType.RedPin
        // marker.mapPoint = MARKER_POINT
        mapView.addPOIItem(marker)
        // mapView.onMapViewSingleTapped(0, 0)

//        getAppKeyHash() 키 해시 로그로 출력
    }
/*
    키 해시 알아보는 코드
    private fun getAppKeyHash() {
        try {
            val info = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            for (signature in info.signatures) {
                var md: MessageDigest
                md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val something = String(android.util.Base64.encode(md.digest(), 0))
                Log.d("Hash key", something)
            }
        } catch (e: Exception) {
            // TODO Auto-generated catch block
            Log.e("name not found", e.toString())
        }
    }
*/

}