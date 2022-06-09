package kr.ac.tukorea.mapsie


import  android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.util.FusedLocationSource
import kr.ac.tukorea.mapsie.MapPage.ThemePlaceRecycleActivity
import kr.ac.tukorea.mapsie.databinding.ActivityMapBinding



class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMapBinding
    var TAG: String = "로그"
    var db: FirebaseFirestore = Firebase.firestore

    private lateinit var locationSource: FusedLocationSource
    private lateinit var naverMap: NaverMap

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000

        // 카카오 검색 API
        const val BASE_URL = "https://dapi.kakao.com/"
        const val API_KEY = "KakaoAK d17bbf0efd9f63a03f1bfc74fa148dbd"  // REST API 키

        lateinit var Tvalue : String
        lateinit var TCollect : String
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        // db의 컬렉션 가져오기
        Tvalue = intent.getStringExtra("ThemeName").toString()
        TCollect = intent.getStringExtra("ThemeCollection").toString()
        Toast.makeText(this, Tvalue.toString(), Toast.LENGTH_SHORT).show()
        Log.d("checktest", "title: $Tvalue")
        val reIntent = Intent(this, ThemePlaceRecycleActivity::class.java)
        reIntent.putExtra("ThemeName1", Tvalue)
        Log.d("checkF",Tvalue.toString())
        reIntent.putExtra("ThemeCollection1", TCollect)


        binding.themaDetailListButton.setOnClickListener {
            val intentlist = Intent(this, ThemePlaceRecycleActivity::class.java)
            startActivity(intentlist)
            db.collection(TCollect.toString()).document(Tvalue.toString()).collection(Tvalue.toString())
                .get().addOnSuccessListener { result ->
                    for(document in result) {
                        var name = document.data?.get("name").toString()
                        var address = document["address"].toString()
                        Log.d("checkVname", name)
                        Log.d("checkVaddress", address)
                    }
                }

        }


        // 타이틀바 숨기기
        var actionBar: ActionBar?
        actionBar = supportActionBar
        actionBar?.hide()

        // 뷰 역할을 하는 프래그먼트 객체 얻기
        val fm = supportFragmentManager
        val mapFragment = fm.findFragmentById(R.id.map_fragment) as MapFragment?
            ?: MapFragment.newInstance().also {
                fm.beginTransaction().add(R.id.map_fragment, it).commit()
            }
        // 인터페이스 역할을 하는 NaverMap 객체 얻기
        // 프래그먼트(MapFragment)의 getMapAsync() 메서드로 OnMapReadyCallback 을 등록하면 비동기로 NaverMap 객체를 얻음
        // NaverMap 객체가 준비되면 OnMapReady() 콜백 메서드 호출
        mapFragment.getMapAsync(this)

        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        Log.d(TAG, "MainActivity - onRequestPermissionsResult")
        if (locationSource.onRequestPermissionsResult(
                requestCode, permissions,
                grantResults
            )
        ) {
            if (!locationSource.isActivated) { // 권한 거부됨
                Log.d(TAG, "MainActivity - onRequestPermissionsResult 권한 거부됨")
                naverMap.locationTrackingMode = LocationTrackingMode.None
            } else {
                Log.d(TAG, "MainActivity - onRequestPermissionsResult 권한 승인됨")
                naverMap.locationTrackingMode = LocationTrackingMode.Follow // 현위치 버튼 컨트롤 활성
            }
            return
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap
        naverMap.locationSource = locationSource
        naverMap.uiSettings.isLocationButtonEnabled = true // GPS 현위치 버튼 생성하는거 true

        // 첫 화면 위치 (시흥시청 부근)
        val camera = CameraUpdate.scrollAndZoomTo(LatLng(37.38, 126.8),11.0)
            .animate(CameraAnimation.Easing, 1200) // 카메라 에니메이션효과 1.2초안에
        naverMap.moveCamera(camera)
    }

}