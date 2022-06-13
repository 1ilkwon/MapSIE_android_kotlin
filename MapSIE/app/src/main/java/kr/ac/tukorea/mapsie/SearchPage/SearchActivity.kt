package kr.ac.tukorea.mapsie.SearchPage

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.KeyEvent.KEYCODE_ENTER
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.InfoWindow
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.util.FusedLocationSource
import kr.ac.tukorea.mapsie.AddActivity
import kr.ac.tukorea.mapsie.KakaoAPI
import kr.ac.tukorea.mapsie.R
import kr.ac.tukorea.mapsie.databinding.ActivitySearchBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class SearchActivity : AppCompatActivity(), OnMapReadyCallback {

    var TAG: String = "로그"

    val infoWindow = InfoWindow()

    private lateinit var locationSource: FusedLocationSource
    private lateinit var naverMap: NaverMap

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000

        // 카카오 검색 API
        const val BASE_URL = "https://dapi.kakao.com/"
        const val API_KEY = "KakaoAK d17bbf0efd9f63a03f1bfc74fa148dbd"  // REST API 키
    }

    private lateinit var binding : ActivitySearchBinding
    private val listItems = arrayListOf<ListLayout>()   // 리사이클러 뷰 아이템
    private val listAdapter = ListAdapter(listItems)    // 리사이클러 뷰 어댑터
    private var pageNumber = 1      // 검색 페이지 번호
    private var keyword = ""        // 검색 키워드

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        var btn_prevPage = findViewById<Button>(R.id.btn_prevPage)
        var btn_nextPage = findViewById<Button>(R.id.btn_nextPage)
        var tv_pageNumber = findViewById<TextView>(R.id.tv_pageNumber)
        var rv_list = findViewById<RecyclerView>(R.id.rv_list)
/*
        infoWindow.setOnClickListener(object : Overlay.OnClickListener {
            override fun onClick(overlay: Overlay): Boolean {

                return false
            }
        })
*/
        // 리사이클러 뷰
        binding.rvList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rvList.adapter = listAdapter
        val addpageintent = Intent(this, AddActivity::class.java)

        // 리사이클러 뷰
        listAdapter.setItemClickListener(object: ListAdapter.OnItemClickListener {
            override fun onClick(v: View, position: Int) {
                val cameraUpdate = CameraUpdate.scrollAndZoomTo(LatLng(listItems[position].y, listItems[position].x), 13.5)
                naverMap.moveCamera(cameraUpdate)

                rv_list.visibility = View.GONE
                btn_prevPage.visibility = View.GONE
                btn_nextPage.visibility = View.GONE
                tv_pageNumber.visibility = View.GONE

                val marker = Marker()
                marker.position = LatLng(listItems[position].y, listItems[position].x)
                marker.map = naverMap

                infoWindow.adapter = object : InfoWindow.DefaultTextAdapter(application) {
                    override fun getText(infoWindow: InfoWindow): CharSequence {
                        return "★ 내 장소 등록/수정하기"
                    }
                }

                infoWindow.position = LatLng(listItems[position].y, listItems[position].x)
                infoWindow.open(marker)


                // 마커 클릭 시
                marker.setOnClickListener { overlay ->
                    infoWindow.open(marker)
                    infoWindow.setOnClickListener(Overlay.OnClickListener {
                        Toast.makeText(this@SearchActivity, "내 장소 등록/수정하기", Toast.LENGTH_SHORT).show()
                        addpageintent.putExtra("name", listItems[position].name)
                        addpageintent.putExtra("road", listItems[position].road)
                        addpageintent.putExtra("x", listItems[position].x)
                        addpageintent.putExtra("y", listItems[position].y)
                        startActivity(addpageintent)
                        false
                    })
                    // 정보창 클릭 시
                    true
                }

                infoWindow.setOnClickListener(Overlay.OnClickListener {
                    Toast.makeText(this@SearchActivity, "내 장소 등록/수정하기", Toast.LENGTH_SHORT).show()
                    addpageintent.putExtra("name", listItems[position].name)
                    addpageintent.putExtra("road", listItems[position].road)
                    addpageintent.putExtra("x", listItems[position].x)
                    addpageintent.putExtra("y", listItems[position].y)
                    startActivity(addpageintent)
                    false
                })
                
                /* 리사이클러 뷰에서 선택한 부분만 마커 표시 (off)
                val marker = Marker()
                marker.position = LatLng(listItems[position].y, listItems[position].x)
                marker.map = naverMap

                // infowindow 작성
                val infoWindow = InfoWindow()
                infoWindow.adapter = object : InfoWindow.DefaultTextAdapter(application) {
                    override fun getText(infoWindow: InfoWindow): CharSequence {
                        return "내 장소 등록/수정하기"
                    }
                }
                infoWindow.position = LatLng(listItems[position].y, listItems[position].x)
                marker.setOnClickListener { overlay ->
                    infoWindow.open(marker)
                    true
                }*/
            }
        })

        // 검색 버튼
        binding.btnSearch.setOnClickListener {
            keyword = binding.etSearchField.text.toString()
            pageNumber = 1
            binding.tvPageNumber.text = pageNumber.toString()
            searchKeyword(keyword, pageNumber)
            rv_list.visibility = View.VISIBLE
            btn_prevPage.visibility = View.VISIBLE
            btn_nextPage.visibility = View.VISIBLE
            tv_pageNumber.visibility = View.VISIBLE
            softkeyboardHide() // 키보드 내리기
        }

        // 이전 페이지 버튼
        binding.btnPrevPage.setOnClickListener {
            pageNumber--
            binding.tvPageNumber.text = pageNumber.toString()
            searchKeyword(keyword, pageNumber)
        }

        // 다음 페이지 버튼
        binding.btnNextPage.setOnClickListener {
            pageNumber++
            binding.tvPageNumber.text = pageNumber.toString()
            searchKeyword(keyword, pageNumber)
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

    override fun onMapReady(naverMap: NaverMap) {

        var btn_prevPage = findViewById<Button>(R.id.btn_prevPage)
        var btn_nextPage = findViewById<Button>(R.id.btn_nextPage)
        var tv_pageNumber = findViewById<TextView>(R.id.tv_pageNumber)
        var rv_list = findViewById<RecyclerView>(R.id.rv_list)

        Log.d(TAG, "SearchActivity - onMapReady")
        this.naverMap = naverMap
        naverMap.locationSource = locationSource
        naverMap.uiSettings.isLocationButtonEnabled = true // GPS 현위치 버튼 생성하는거 true

        // 첫 화면 위치 (시흥시청 부근)
        val camera = CameraUpdate.scrollAndZoomTo(LatLng(37.38, 126.8),11.0)
            .animate(CameraAnimation.Easing, 1200) // 카메라 에니메이션효과 1.2초안에
        naverMap.moveCamera(camera)

        // 네이버 맵 터치시
        naverMap.setOnMapClickListener { pointF, latLng ->
            rv_list.visibility = View.GONE
            btn_prevPage.visibility = View.GONE
            btn_nextPage.visibility = View.GONE
            tv_pageNumber.visibility = View.GONE
            softkeyboardHide() // 키보드 내리기
//            infoWindow.close()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        Log.d(TAG, "SearchActivity - onRequestPermissionsResult")
        if (locationSource.onRequestPermissionsResult(
                requestCode, permissions,
                grantResults
            )
        ) {
            if (!locationSource.isActivated) { // 권한 거부됨
                Log.d(TAG, "SearchActivity - onRequestPermissionsResult 권한 거부됨")
                naverMap.locationTrackingMode = LocationTrackingMode.None
            } else {
                Log.d(TAG, "SearchActivity - onRequestPermissionsResult 권한 승인됨")
                naverMap.locationTrackingMode = LocationTrackingMode.Follow // 현위치 버튼 컨트롤 활성
            }
            return
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    // 키워드 검색 함수 !!!!
    private fun searchKeyword(keyword: String, page: Int) {
        val retrofit = Retrofit.Builder()          // Retrofit 구성
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(KakaoAPI::class.java)            // 통신 인터페이스를 객체로 생성
        val call = api.getSearchKeyword(API_KEY, "경기도 시흥시 $keyword", page)    // 검색 조건 입력

        // API 서버에 요청
        call.enqueue(object : Callback<ResultSearchKeyword> {
            override fun onResponse(
                call: Call<ResultSearchKeyword>,
                response: Response<ResultSearchKeyword>
            ) {
                // 통신 성공
                Log.w("LocalSearch", "통신 성공")
                addItemsAndMarkers(response.body())
            }

            override fun onFailure(call: Call<ResultSearchKeyword>, t: Throwable) {
                // 통신 실패
                Log.w("LocalSearch", "통신 실패: ${t.message}")
            }
        })
    }
    // 검색 결과 처리 함수
    private fun addItemsAndMarkers(searchResult: ResultSearchKeyword?) {
        if (!searchResult?.documents.isNullOrEmpty()) {
            // 검색 결과 있음
            listItems.clear()                   // 리스트 초기화
            for (document in searchResult!!.documents) {
                // 결과를 리사이클러 뷰에 추가
                val item = ListLayout(
                    document.place_name,
                    document.road_address_name,
                    document.address_name,
                    document.x.toDouble(),
                    document.y.toDouble()
                )
                listItems.add(item)
                listAdapter.notifyDataSetChanged()

                /* 검색 결과 전부 다 마커 표시하기 (on)
                val marker = Marker()
                marker.position = LatLng(document.y.toDouble(), document.x.toDouble())
                marker.map = naverMap
                */
                // infowindow 작성

/*                infoWindow.adapter = object : InfoWindow.DefaultTextAdapter(application) {
                    override fun getText(infoWindow: InfoWindow): CharSequence {
                        return "★ 내 장소 등록/수정하기"
                    }
                }
                infoWindow.position = LatLng(document.y.toDouble(), document.x.toDouble())
                marker.setOnClickListener { overlay ->
                    infoWindow.open(marker)
                    true
                }
                // 정보창 클릭 시

                infoWindow.setOnClickListener(Overlay.OnClickListener {
                    Toast.makeText(this@SearchActivity, "내 장소 등록/수정하기", Toast.LENGTH_SHORT).show()
                    val addpageintent = Intent(this, AddActivity::class.java)
                    addpageintent.putExtra("place", document.place_name)
                    addpageintent.putExtra("road", document.road_address_name)
                    startActivity(addpageintent)
                    false
                })*/
            }
            binding.btnNextPage.isEnabled = !searchResult.meta.is_end // 페이지가 더 있을 경우 다음 버튼 활성화
            binding.btnPrevPage.isEnabled = pageNumber != 1             // 1페이지가 아닐 경우 이전 버튼 활성화

        } else {
            // 검색 결과 없음
            Toast.makeText(this, "검색 결과가 없습니다", Toast.LENGTH_SHORT).show()
        }
    }

    // 자동으로 키보드 내리기
    fun softkeyboardHide() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.etSearchField.windowToken, 0)
    }
    /*
    fun mapmarker(){

    }*/
}