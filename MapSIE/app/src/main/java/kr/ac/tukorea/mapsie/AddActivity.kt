package kr.ac.tukorea.mapsie

import android.app.Dialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.util.FusedLocationSource
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.main_body.*
import kotlinx.android.synthetic.main.main_toolbar.*
import kr.ac.tukorea.mapsie.SearchPage.ListAdapter
import kr.ac.tukorea.mapsie.SearchPage.ListLayout
import kr.ac.tukorea.mapsie.SearchPage.ResultSearchKeyword
import kr.ac.tukorea.mapsie.SearchPage.SearchActivity
import kr.ac.tukorea.mapsie.databinding.ActivityAddBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AddActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var locationSource: FusedLocationSource

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000

        // 카카오 검색 API
        const val BASE_URL = "https://dapi.kakao.com/"
        const val API_KEY = "KakaoAK d17bbf0efd9f63a03f1bfc74fa148dbd"  // REST API 키
    }

    private val listItems = arrayListOf<ListLayout>()   // 리사이클러 뷰 아이템
    private val listAdapter = ListAdapter(listItems)    // 리사이클러 뷰 어댑터
    private var pageNumber = 1      // 검색 페이지 번호
    private var keyword = ""        // 검색 키워드

    // 스피너 배열 index로 뽑아오기 위해 사용
    var pos = 0
    // 스피너에 들어갈 배열
    var dataArr = arrayOf(
        "카공하기 좋은 곳",
        "디저트 맛집",
        "뷰가 좋은 카페",
        "양식이 땡길 때",
        "혼밥하기 좋은 곳",
        "소개팅 할 때 추천",
        "산책하기 좋은 공원",
        "런닝하기 좋은 공원",
        "꽃구경하기 좋은 공원"
    )

    private lateinit var binding: ActivityAddBinding

    var db: FirebaseFirestore = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        //binding 방식으로 변경
        binding = ActivityAddBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) //왼쪽에 뒤로가기버튼생성
        supportActionBar?.setDisplayShowTitleEnabled(false) // 툴바에 타이틀 안보이게
        toolbar.title = "MapSIE"
        binding.navigationView.setNavigationItemSelectedListener(this)

        // !!!!!! 주소 검색 관련 findViewById 추가
        var btnSearch = findViewById<Button>(R.id.btn_search)
        var rv_list = findViewById<RecyclerView>(R.id.rv_list)
        var add_adress = findViewById<EditText>(R.id.add_adress)
        var add_name = findViewById<EditText>(R.id.add_name)
        var adr_text = findViewById<TextView>(R.id.adr_text)

        rv_list.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rv_list.adapter = listAdapter

        if (intent.hasExtra("road") && intent.hasExtra("name")) {
            add_name.setText(intent.getStringExtra("name"))
            add_adress.setText(intent.getStringExtra("road"))
//            Toast.makeText(this@AddActivity, "주소,장소 값 INTENT TEST", Toast.LENGTH_SHORT).show()
        }


        // 검색 버튼
        btnSearch.setOnClickListener {
            keyword = add_name.text.toString()
            pageNumber = 1
            searchKeyword(keyword, pageNumber)
            rv_list.visibility = View.VISIBLE
            adr_text.visibility = View.VISIBLE
        }

        // 리사이클러 뷰 (아이템 클릭 시)
        listAdapter.setItemClickListener(object: ListAdapter.OnItemClickListener {
            override fun onClick(v: View, position: Int) {
                add_adress.setText(listItems[position].road)
                add_name.setText(listItems[position].name)
                rv_list.visibility = View.GONE
                adr_text.visibility = View.GONE
            }
        })

        var adapter: ArrayAdapter<String>
        adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, dataArr)
        binding.mainLayout.addTheme.adapter = adapter
        // 스피너 수정 -> 목록에 스피너 띄우기
        binding.mainLayout.addTheme.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {

                }
                override fun onItemSelected(parent: AdapterView<*>?, view: View?,position: Int, id: Long) {
                    pos = position
                    // 스피너 선택 이벤트, pos는 0 부터 위 dataArr의 배열에서 index로 들고옴
                    when (position) {
                        0 -> { //카공하기 좋은 곳 Cafe1
                            var countNum: Int = 0
                            binding.mainLayout.saveBtn.setOnClickListener {
                                // 사용자가 모든 정보를 입력하지 않으면 "모든 정보를 입력해주세요" 토스트메시지
                                if (binding.mainLayout.addName.text.toString().equals("") || binding.mainLayout.addAdress.text.toString().equals("") || binding.mainLayout.addIntroduce.text.toString().equals(""))
                                {Toast.makeText(this@AddActivity, "모든 정보를 입력해주세요", Toast.LENGTH_SHORT).show()
                                } else {
                                    // 저장할 장소에 함께 들어가야 할 데이터 hashmap으로 저장 -> **(차후에 address 지도 팀이랑 연결 필요)
                                    var storeInfoMap = hashMapOf(
                                        "address" to binding.mainLayout.addAdress.text.toString(),
                                        "name" to binding.mainLayout.addName.text.toString(),
                                        "introduce" to binding.mainLayout.addIntroduce.text.toString()
                                    )
                                    // firebase 구조에 따라 데이터 저장
                                    db.collection("Cafes").document("Cafe1").collection("cafe1")
                                        .document("Cafe1_" + countNum.toString())
                                        .set(storeInfoMap)
                                    // 0 -> {...} 함수 내에서 count를 해줌으로 하나의 테마에 새로운 장소가 저장될 때마다 각각 1을 count 해줌
                                    countNum++
                                }
                                // db에 저장 완료 시 "저장완료" 토스트메시지로 출력
                                Toast.makeText(this@AddActivity, "저장 완료!", Toast.LENGTH_SHORT).show()
                            }
                        } // 이하 반복
                        1 -> { //디저트 맛집 Cafe2
                            var countNum: Int = 0
                            binding.mainLayout.saveBtn.setOnClickListener {
                                if (binding.mainLayout.addName.text.toString().equals("") || binding.mainLayout.addAdress.text.toString().equals("") || binding.mainLayout.addIntroduce.text.toString().equals(""))
                                {Toast.makeText(this@AddActivity, "모든 정보를 입력해주세요", Toast.LENGTH_SHORT).show()
                                } else {
                                    var storeInfoMap = hashMapOf(
                                        "address" to binding.mainLayout.addAdress.text.toString(),
                                        "name" to binding.mainLayout.addName.text.toString(),
                                        "introduce" to binding.mainLayout.addIntroduce.text.toString()
                                    )
                                    db.collection("Cafes").document("Cafe2").collection("cafe2")
                                        .document("Cafe2_" + countNum.toString())
                                        .set(storeInfoMap)
                                    countNum++
                                }
                                Toast.makeText(this@AddActivity, "저장 완료!", Toast.LENGTH_SHORT).show()
                            }
                        }
                        2 -> { //뷰가 좋은 카페 Cafe3
                            var countNum: Int = 0
                            binding.mainLayout.saveBtn.setOnClickListener {
                                if (binding.mainLayout.addName.text.toString().equals("") || binding.mainLayout.addAdress.text.toString().equals("") || binding.mainLayout.addIntroduce.text.toString().equals(""))
                                {Toast.makeText(this@AddActivity, "모든 정보를 입력해주세요", Toast.LENGTH_SHORT).show()
                                } else {
                                    var storeInfoMap = hashMapOf(
                                        "address" to binding.mainLayout.addAdress.text.toString(),
                                        "name" to binding.mainLayout.addName.text.toString(),
                                        "introduce" to binding.mainLayout.addIntroduce.text.toString()
                                    )
                                    db.collection("Cafes").document("Cafe3").collection("cafe3")
                                        .document("Cafe3_" + countNum.toString())
                                        .set(storeInfoMap)
                                    countNum++
                                }
                                Toast.makeText(this@AddActivity, "저장 완료!", Toast.LENGTH_SHORT).show()
                            }
                        }
                        3 -> { //양식이 땡길 때 Food1
                            var countNum: Int = 0
                            binding.mainLayout.saveBtn.setOnClickListener {
                                if (binding.mainLayout.addName.text.toString().equals("") || binding.mainLayout.addAdress.text.toString().equals("") || binding.mainLayout.addIntroduce.text.toString().equals(""))
                                {Toast.makeText(this@AddActivity, "모든 정보를 입력해주세요", Toast.LENGTH_SHORT).show()
                                } else {
                                    var storeInfoMap = hashMapOf(
                                        "address" to binding.mainLayout.addAdress.text.toString(),
                                        "name" to binding.mainLayout.addName.text.toString(),
                                        "introduce" to binding.mainLayout.addIntroduce.text.toString()
                                    )
                                    db.collection("Foods").document("Food1").collection("food1")
                                        .document("Food1_" + countNum.toString())
                                        .set(storeInfoMap)
                                    countNum++
                                }
                                Toast.makeText(this@AddActivity, "저장 완료!", Toast.LENGTH_SHORT).show()
                            }
                        }
                        4 -> { //혼밥하기 좋은 곳 Food2
                            var countNum: Int = 0
                            binding.mainLayout.saveBtn.setOnClickListener {
                                if (binding.mainLayout.addName.text.toString().equals("") || binding.mainLayout.addAdress.text.toString().equals("") || binding.mainLayout.addIntroduce.text.toString().equals(""))
                                {Toast.makeText(this@AddActivity, "모든 정보를 입력해주세요", Toast.LENGTH_SHORT).show()
                                } else {
                                    var storeInfoMap = hashMapOf(
                                        "address" to binding.mainLayout.addAdress.text.toString(),
                                        "name" to binding.mainLayout.addName.text.toString(),
                                        "introduce" to binding.mainLayout.addIntroduce.text.toString()
                                    )
                                    db.collection("Foods").document("Food2").collection("food2")
                                        .document("Food2_" + countNum.toString())
                                        .set(storeInfoMap)
                                    countNum++
                                }
                                Toast.makeText(this@AddActivity, "저장 완료!", Toast.LENGTH_SHORT).show()
                            }
                        }
                        5 -> { //소개팅 할 때 추천 Food3
                            var countNum: Int = 0
                            binding.mainLayout.saveBtn.setOnClickListener {
                                if (binding.mainLayout.addName.text.toString().equals("") || binding.mainLayout.addAdress.text.toString().equals("") || binding.mainLayout.addIntroduce.text.toString().equals(""))
                                {Toast.makeText(this@AddActivity, "모든 정보를 입력해주세요", Toast.LENGTH_SHORT).show()
                                } else {
                                    var storeInfoMap = hashMapOf(
                                        "address" to binding.mainLayout.addAdress.text.toString(),
                                        "name" to binding.mainLayout.addName.text.toString(),
                                        "introduce" to binding.mainLayout.addIntroduce.text.toString()
                                    )
                                    db.collection("Foods").document("Food3").collection("food3")
                                        .document("Food3_" + countNum.toString())
                                        .set(storeInfoMap)
                                    countNum++
                                }
                                Toast.makeText(this@AddActivity, "저장 완료!", Toast.LENGTH_SHORT).show()
                            }
                        }
                        6 -> { //산책하기 좋은 공원 Park1
                            var countNum: Int = 0
                            binding.mainLayout.saveBtn.setOnClickListener {
                                if (binding.mainLayout.addName.text.toString().equals("") || binding.mainLayout.addAdress.text.toString().equals("") || binding.mainLayout.addIntroduce.text.toString().equals(""))
                                {Toast.makeText(this@AddActivity, "모든 정보를 입력해주세요", Toast.LENGTH_SHORT).show()
                                } else {
                                    var storeInfoMap = hashMapOf(
                                        "address" to binding.mainLayout.addAdress.text.toString(),
                                        "name" to binding.mainLayout.addName.text.toString(),
                                        "introduce" to binding.mainLayout.addIntroduce.text.toString()
                                    )
                                    db.collection("Park").document("Park1").collection("park1")
                                        .document("Park1_"+ countNum.toString())
                                        .set(storeInfoMap)
                                    countNum++
                                }
                                Toast.makeText(this@AddActivity, "저장 완료!", Toast.LENGTH_SHORT).show()
                            }
                        }
                        7 -> { //런닝하기 좋은 공원 Park2
                            var countNum: Int = 0
                            binding.mainLayout.saveBtn.setOnClickListener {
                                if (binding.mainLayout.addName.text.toString().equals("") || binding.mainLayout.addAdress.text.toString().equals("") || binding.mainLayout.addIntroduce.text.toString().equals(""))
                                {Toast.makeText(this@AddActivity, "모든 정보를 입력해주세요", Toast.LENGTH_SHORT).show()
                                } else {
                                    var storeInfoMap = hashMapOf(
                                        "address" to binding.mainLayout.addAdress.text.toString(),
                                        "name" to binding.mainLayout.addName.text.toString(),
                                        "introduce" to binding.mainLayout.addIntroduce.text.toString()
                                    )
                                    db.collection("Park").document("Park2").collection("park2")
                                        .document("Park2_"+ countNum.toString())
                                        .set(storeInfoMap)
                                    countNum++
                                }
                                Toast.makeText(this@AddActivity, "저장 완료!", Toast.LENGTH_SHORT).show()
                            }
                        }
                        8 -> { //꽃구경하기 좋은 공원 Park3
                            var countNum: Int = 0
                            binding.mainLayout.saveBtn.setOnClickListener {
                                if (binding.mainLayout.addName.text.toString().equals("") || binding.mainLayout.addAdress.text.toString().equals("") || binding.mainLayout.addIntroduce.text.toString().equals(""))
                                {Toast.makeText(this@AddActivity, "모든 정보를 입력해주세요", Toast.LENGTH_SHORT).show()
                                } else {
                                    var storeInfoMap = hashMapOf(
                                        "address" to binding.mainLayout.addAdress.text.toString(),
                                        "name" to binding.mainLayout.addName.text.toString(),
                                        "introduce" to binding.mainLayout.addIntroduce.text.toString()
                                    )
                                    db.collection("Park").document("Park3").collection("park3")
                                        .document("Park3_"+ countNum.toString())
                                        .set(storeInfoMap)
                                    countNum++
                                }
                                Toast.makeText(this@AddActivity, "저장 완료!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
        }
        Toast.makeText(this, "생성완료", Toast.LENGTH_SHORT).show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)       //툴바 메뉴
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {   //툴바에 있는 메뉴 누를 때
        when (item.itemId) {
            android.R.id.home -> {   //뒤로가기 버튼
                finish()
                return true
            }
            R.id.toolbar_menu -> { // 메뉴 버튼
                drawer_layout.openDrawer(GravityCompat.END)    // 네비게이션 드로어 열기(오른쪽에서 왼쪽으로)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {  //기본 폰에 내장되어 있는 ◀뒤로가기 누르면
        if (drawer_layout.isDrawerOpen(GravityCompat.END)) {
            drawer_layout.closeDrawers()
            // 테스트를 위해 뒤로가기 버튼시 Toast 메시지
            Toast.makeText(this, "뒤로가기버튼 테스트", Toast.LENGTH_SHORT).show()
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {    //메뉴바 클릭 시 실행하는 메서드
        when (item.itemId) {
            R.id.home -> startActivity(Intent(this, MainActivity::class.java))
            R.id.mypage -> startActivity(Intent(this, MyPageActivity::class.java))
            R.id.guideline -> startActivity(Intent(this, GuideActivity::class.java))
            R.id.addPage -> Toast.makeText(this, "추가화면 실행", Toast.LENGTH_SHORT).show()
            R.id.logout -> {
                val builder = AlertDialog.Builder(this)
                    .apply {
                        setTitle("알림")
                        setMessage("로그아웃 하시겠습니까?")
                        setPositiveButton("네") { _, _ ->
                            FirebaseAuth.getInstance().signOut()
                            Handler().postDelayed({
                                ActivityCompat.finishAffinity(this@AddActivity)
                                System.runFinalization()
                                System.exit(0)
                            }, 1000)
                        }
                        setNegativeButton("아니요") { _, _, ->
                            return@setNegativeButton
                        }
                        show()
                    }
            }
        }
        return false
    }


// 여기부터 수정


    // 키워드 검색 함수
    private fun searchKeyword(keyword: String, page: Int) {
        val retrofit = Retrofit.Builder()          // Retrofit 구성
            .baseUrl(SearchActivity.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(KakaoAPI::class.java)            // 통신 인터페이스를 객체로 생성
        val call = api.getSearchKeyword(SearchActivity.API_KEY, "경기도 시흥시 $keyword", page)    // 검색 조건 입력

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
            }

        } else {
            // 검색 결과 없음
            Toast.makeText(this, "검색 결과가 없습니다", Toast.LENGTH_SHORT).show()
        }
    }

}
