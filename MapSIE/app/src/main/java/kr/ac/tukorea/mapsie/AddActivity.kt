package kr.ac.tukorea.mapsie

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.util.FusedLocationSource
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.activity_sign_up.*
import kotlinx.android.synthetic.main.add_body.*
import kotlinx.android.synthetic.main.main_body.*
import kotlinx.android.synthetic.main.main_drawer_header.*
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
import java.text.SimpleDateFormat
import java.util.*

class AddActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var imagesRef: StorageReference
    private val OPEN_GALLERY = 1111
    var fileName: String = SimpleDateFormat("yyyymmdd_HHmmss").format(Date())
    var downloadUri: Uri? = null    //storage에서 다운받는 이미지의 uri

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
    var storage: FirebaseStorage = FirebaseStorage.getInstance()

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

        db.collection("users").document(Firebase.auth.currentUser?.uid ?: "No User").get()
            .addOnSuccessListener {
                member_nickname.text = it["signName"].toString()
                Glide.with(this)
                    .load(it["signImg"])
                    .override(60, 60)
                    .error(R.drawable.ic_baseline_account_circle_24)    //에러가 났을 때
                    .fallback(R.drawable.ic_baseline_account_circle_24) //signImg값이 없다면 기본 사진 출력
                    .into(member_icon as ImageView)
            }.addOnFailureListener {
            Toast.makeText(this, ".", Toast.LENGTH_SHORT).show()
        }

        // !!!!!! 주소 검색 관련 findViewById 추가
        var btnSearch = findViewById<Button>(R.id.btn_search)
        var rv_list = findViewById<RecyclerView>(R.id.rv_list)
        var add_adress = findViewById<EditText>(R.id.add_adress)
        var add_name = findViewById<EditText>(R.id.add_name)
        var adr_text = findViewById<TextView>(R.id.adr_text)
        var x1: Double? = null
        var y1: Double? = null
        val mainpageintent = Intent(this, MainActivity::class.java)

        rv_list.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rv_list.adapter = listAdapter

        if (intent.hasExtra("road") && intent.hasExtra("name")) {
            add_name.setText(intent.getStringExtra("name"))
            add_adress.setText(intent.getStringExtra("road"))

            // 좌표 가져온거 x1과 y1로 표시
            x1 = (intent.getDoubleExtra("x", 0.0))
            y1 = (intent.getDoubleExtra("y", 0.0))

            // 좌표 가져와졌는지 확인 지우시면됩니다.
//            Toast.makeText(this@AddActivity, "$x1\n$y1", Toast.LENGTH_SHORT).show()

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
        listAdapter.setItemClickListener(object : ListAdapter.OnItemClickListener {
            override fun onClick(v: View, position: Int) {
                add_adress.setText(listItems[position].road)
                add_name.setText(listItems[position].name)

                // 좌표 가져온거 x1과 y1로 표시
                x1 = listItems[position].x
                y1 = listItems[position].y
                // 좌표 가져와졌는지 확인 지우시면됩니다.
//                Toast.makeText(this@AddActivity, "$x1\n$y1", Toast.LENGTH_SHORT).show()

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

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    pos = position
                    // 스피너 선택 이벤트, pos는 0 부터 위 dataArr의 배열에서 index로 들고옴
                    when (position) {
                        0 -> { //카공하기 좋은 곳 Cafe1
                            var countNum: Int
                            var allCountNum: Int = 0
                            binding.mainLayout.saveBtn.setOnClickListener {
                                // 사용자가 모든 정보를 입력하지 않으면 "모든 정보를 입력해주세요" 토스트메시지
                                if (binding.mainLayout.addName.text.toString()
                                        .equals("") || binding.mainLayout.addAdress.text.toString()
                                        .equals("") || binding.mainLayout.addIntroduce.text.toString()
                                        .equals("")
                                ) {
                                    Toast.makeText(
                                        this@AddActivity,
                                        "모든 정보를 입력해주세요",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {

                                    db.collection("Cafes").document("Cafe1").get().addOnSuccessListener {
                                        countNum = it["count"].hashCode()

                                        // 저장할 장소에 함께 들어가야 할 데이터 hashmap으로 저장 -> **(차후에 address 지도 팀이랑 연결 필요)
                                        var storeInfoMap = hashMapOf(
                                            "address" to binding.mainLayout.addAdress.text.toString(),
                                            "name" to binding.mainLayout.addName.text.toString(),
                                            "introduce" to binding.mainLayout.addIntroduce.text.toString(),
                                            "storeNum" to "Cafe1_" + countNum.toString(),
                                            "Tname" to "카공하기 좋은 곳",
                                            "placeImage" to downloadUri.toString(),
                                            "x" to x1,
                                            "y" to y1,
                                            "Rcount" to 0,
                                            "Tcount" to countNum,
                                        )
                                        // firebase 구조에 따라 데이터 저장
                                        db.collection("Cafes").document("Cafe1").collection("Cafe1")
                                            .document("Cafe1_" + countNum.toString())
                                            .set(storeInfoMap)
                                        // 0 -> {...} 함수 내에서 count를 해줌으로 하나의 테마에 새로운 장소가 저장될 때마다 각각 1을 count 해줌
                                        countNum++
                                        var countMap = hashMapOf(
                                            "count" to countNum,
                                            "Tcollect" to "Cafes",
                                            "Tname" to "카공하기 좋은 곳",
                                            "Tnum" to "Cafe1",
                                            "Timg" to "https://firebasestorage.googleapis.com/v0/b/mapsie-1b20c.appspot.com/o/cafe_study.png?alt=media&token=77ec8e95-2806-4c32-87c8-a7dcb6499240"
                                        )
                                        db.collection("Cafes").document("Cafe1").set(countMap)

                                        // firebase All 전체 저장
                                        db.collection("All").document("All1").collection("All1")
                                            .document("All1_" + countNum.toString())
                                            .set(storeInfoMap)
                                    }

                                    allCountNum++
                                }
                                // db에 저장 완료 시 "저장완료" 토스트메시지로 출력
                                startActivity(mainpageintent)
                                false
                                Toast.makeText(this@AddActivity, "저장 완료!", Toast.LENGTH_SHORT)
                                    .show()

                            }
                        } // 이하 반복
                        1 -> { //디저트 맛집 Cafe2
                            var countNum: Int
                            var allCountNum: Int = 0
                            binding.mainLayout.saveBtn.setOnClickListener {
                                if (binding.mainLayout.addName.text.toString()
                                        .equals("") || binding.mainLayout.addAdress.text.toString()
                                        .equals("") || binding.mainLayout.addIntroduce.text.toString()
                                        .equals("")
                                ) {
                                    Toast.makeText(
                                        this@AddActivity,
                                        "모든 정보를 입력해주세요",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {

                                    db.collection("Cafes").document("Cafe2").get().addOnSuccessListener {
                                        countNum = it["count"].hashCode()
                                        var storeInfoMap = hashMapOf(
                                            "address" to binding.mainLayout.addAdress.text.toString(),
                                            "name" to binding.mainLayout.addName.text.toString(),
                                            "introduce" to binding.mainLayout.addIntroduce.text.toString(),
                                            "storeNum" to "Cafe2_" + countNum.toString(),
                                            "Tname" to "디저트 맛집",
                                            "placeImage" to downloadUri.toString(),
                                            "x" to x1,
                                            "y" to y1,
                                            "Rcount" to 0,
                                            "Tcount" to countNum,
                                        )
                                        db.collection("Cafes").document("Cafe2").collection("Cafe2")
                                            .document("Cafe2_" + countNum.toString())
                                            .set(storeInfoMap)
                                        countNum++
                                        var countMap = hashMapOf(
                                            "count" to countNum,
                                            "Tcollect" to "Cafes",
                                            "Tname" to "디저트 맛집",
                                            "Tnum" to "Cafe2",
                                            "Timg" to "https://firebasestorage.googleapis.com/v0/b/mapsie-1b20c.appspot.com/o/cafe_desert.png?alt=media&token=77ec8e95-2806-4c32-87c8-a7dcb6499240"
                                        )
                                        db.collection("Cafes").document("Cafe2").set(countMap)

                                        // firebase All 전체 저장
                                        db.collection("All").document("All2").collection("All2")
                                            .document("All2_" + countNum.toString())
                                            .set(storeInfoMap)
                                    }
                                    allCountNum++
                                }
                                startActivity(mainpageintent)
                                false
                                Toast.makeText(this@AddActivity, "저장 완료!", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                        2 -> { //뷰가 좋은 카페 Cafe3
                            var countNum: Int = 0
                            var allCountNum: Int = 0
                            binding.mainLayout.saveBtn.setOnClickListener {
                                if (binding.mainLayout.addName.text.toString()
                                        .equals("") || binding.mainLayout.addAdress.text.toString()
                                        .equals("") || binding.mainLayout.addIntroduce.text.toString()
                                        .equals("")
                                ) {
                                    Toast.makeText(
                                        this@AddActivity,
                                        "모든 정보를 입력해주세요",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {

                                    db.collection("Cafes").document("Cafe3").get().addOnSuccessListener {
                                        countNum = it["count"].hashCode()
                                        var storeInfoMap = hashMapOf(
                                            "address" to binding.mainLayout.addAdress.text.toString(),
                                            "name" to binding.mainLayout.addName.text.toString(),
                                            "introduce" to binding.mainLayout.addIntroduce.text.toString(),
                                            "storeNum" to "Cafe3_" + countNum.toString(),
                                            "Tname" to "뷰가 좋은 카페",
                                            "placeImage" to downloadUri.toString(),
                                            "x" to x1,
                                            "y" to y1,
                                            "Rcount" to 0,
                                            "Tcount" to countNum,
                                        )
                                        db.collection("Cafes").document("Cafe3").collection("Cafe3")
                                            .document("Cafe3_" + countNum.toString())
                                            .set(storeInfoMap)
                                        countNum++
                                        var countMap = hashMapOf(
                                            "count" to countNum,
                                            "Tcollect" to "Cafes",
                                            "Tname" to "뷰가 좋은 카페",
                                            "Tnum" to "Cafe3",
                                            "Timg" to "https://firebasestorage.googleapis.com/v0/b/mapsie-1b20c.appspot.com/o/cafe_view.png?alt=media&token=f7153b96-3ff8-438d-a665-5ae4bc2ab856"
                                        )
                                        db.collection("Cafes").document("Cafe3").set(countMap)

                                        // firebase All 전체 저장
                                        db.collection("All").document("All3").collection("All3")
                                            .document("All3_" + countNum.toString())
                                            .set(storeInfoMap)
                                    }
                                    allCountNum++
                                }
                                startActivity(mainpageintent)
                                false
                                Toast.makeText(this@AddActivity, "저장 완료!", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                        3 -> { //양식이 땡길 때 Food1
                            var countNum: Int = 0
                            var allCountNum: Int = 0
                            binding.mainLayout.saveBtn.setOnClickListener {
                                if (binding.mainLayout.addName.text.toString()
                                        .equals("") || binding.mainLayout.addAdress.text.toString()
                                        .equals("") || binding.mainLayout.addIntroduce.text.toString()
                                        .equals("")
                                ) {
                                    Toast.makeText(
                                        this@AddActivity,
                                        "모든 정보를 입력해주세요",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {

                                    db.collection("Foods").document("Food1").get().addOnSuccessListener {
                                        countNum = it["count"].hashCode()
                                        var storeInfoMap = hashMapOf(
                                            "address" to binding.mainLayout.addAdress.text.toString(),
                                            "name" to binding.mainLayout.addName.text.toString(),
                                            "introduce" to binding.mainLayout.addIntroduce.text.toString(),
                                            "storeNum" to "Food1_" + countNum.toString(),
                                            "Tname" to "양식이 땡길 때",
                                            "placeImage" to downloadUri.toString(),
                                            "x" to x1,
                                            "y" to y1,
                                            "Rcount" to 0,
                                            "Tcount" to countNum,
                                        )
                                        db.collection("Foods").document("Food1").collection("Food1")
                                            .document("Food1_" + countNum.toString())
                                            .set(storeInfoMap)
                                        countNum++

                                        var countMap = hashMapOf(
                                            "count" to countNum,
                                            "Tcollect" to "Foods",
                                            "Tname" to "양식이 땡길 때",
                                            "Tnum" to "Food1",
                                            "Timg" to "https://firebasestorage.googleapis.com/v0/b/mapsie-1b20c.appspot.com/o/food_steak.png?alt=media&token=eb550850-fcff-40b4-b32b-709c14916f69"
                                        )
                                        db.collection("Foods").document("Food1").set(countMap)

                                        // firebase All 전체 저장
                                        db.collection("All").document("All4").collection("All4")
                                            .document("All4_" + countNum.toString())
                                            .set(storeInfoMap)
                                    }
                                    allCountNum++
                                }
                                startActivity(mainpageintent)
                                false
                                Toast.makeText(this@AddActivity, "저장 완료!", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                        4 -> { //혼밥하기 좋은 곳 Food2
                            var countNum: Int = 0
                            var allCountNum: Int = 0
                            binding.mainLayout.saveBtn.setOnClickListener {
                                if (binding.mainLayout.addName.text.toString()
                                        .equals("") || binding.mainLayout.addAdress.text.toString()
                                        .equals("") || binding.mainLayout.addIntroduce.text.toString()
                                        .equals("")
                                ) {
                                    Toast.makeText(
                                        this@AddActivity,
                                        "모든 정보를 입력해주세요",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {

                                    db.collection("Foods").document("Food2").get().addOnSuccessListener {
                                        countNum = it["count"].hashCode()
                                        var storeInfoMap = hashMapOf(
                                            "address" to binding.mainLayout.addAdress.text.toString(),
                                            "name" to binding.mainLayout.addName.text.toString(),
                                            "introduce" to binding.mainLayout.addIntroduce.text.toString(),
                                            "storeNum" to "Food2_" + countNum.toString(),
                                            "Tname" to "혼밥하기 좋은 곳",
                                            "placeImage" to downloadUri.toString(),
                                            "x" to x1,
                                            "y" to y1,
                                            "Rcount" to 0,
                                            "Tcount" to countNum,
                                        )
                                        db.collection("Foods").document("Food2").collection("Food2")
                                            .document("Food2_" + countNum.toString())
                                            .set(storeInfoMap)

                                        countNum++

                                        var countMap = hashMapOf(
                                            "count" to countNum,
                                            "Tcollect" to "Foods",
                                            "Tname" to "혼밥하기 좋은 곳",
                                            "Tnum" to "Food2",
                                            "Timg" to "https://firebasestorage.googleapis.com/v0/b/mapsie-1b20c.appspot.com/o/food_alone.png?alt=media&token=ff9a5079-be82-4fed-84d4-5b6ff1bbe57c"
                                        )
                                        db.collection("Foods").document("Food2").set(countMap)

                                        // firebase All 전체 저장
                                        db.collection("All").document("All5").collection("All5")
                                            .document("All5_" + countNum.toString())
                                            .set(storeInfoMap)
                                    }
                                    allCountNum++
                                }
                                startActivity(mainpageintent)
                                false
                                Toast.makeText(this@AddActivity, "저장 완료!", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                        5 -> { //소개팅 할 때 추천 Food3
                            var countNum: Int = 0
                            var allCountNum: Int = 0
                            binding.mainLayout.saveBtn.setOnClickListener {
                                if (binding.mainLayout.addName.text.toString()
                                        .equals("") || binding.mainLayout.addAdress.text.toString()
                                        .equals("") || binding.mainLayout.addIntroduce.text.toString()
                                        .equals("")
                                ) {
                                    Toast.makeText(
                                        this@AddActivity,
                                        "모든 정보를 입력해주세요",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    db.collection("Foods").document("Food3").get().addOnSuccessListener {
                                        countNum = it["count"].hashCode()
                                        var storeInfoMap = hashMapOf(
                                            "address" to binding.mainLayout.addAdress.text.toString(),
                                            "name" to binding.mainLayout.addName.text.toString(),
                                            "introduce" to binding.mainLayout.addIntroduce.text.toString(),
                                            "storeNum" to "Food3_" + countNum.toString(),
                                            "Tname" to "소개팅할때 추천",
                                            "placeImage" to downloadUri.toString(),
                                            "x" to x1,
                                            "y" to y1,
                                            "Rcount" to 0,
                                            "Tcount" to countNum,
                                        )
                                        db.collection("Foods").document("Food3").collection("Food3")
                                            .document("Food3_" + countNum.toString())
                                            .set(storeInfoMap)
                                        countNum++

                                        var countMap = hashMapOf(
                                            "count" to countNum,
                                            "Tcollect" to "Foods",
                                            "Tname" to "소개팅할때 추천",
                                            "Tnum" to "Food3",
                                            "Timg" to "https://firebasestorage.googleapis.com/v0/b/mapsie-1b20c.appspot.com/o/food_blind_date.png?alt=media&token=8cb7eb29-aa2f-4ead-b0cc-d0df95561c0d"
                                        )
                                        db.collection("Foods").document("Food3").set(countMap)

                                        // firebase All 전체 저장
                                        db.collection("All").document("All6").collection("All6")
                                            .document("All6_" + countNum.toString())
                                            .set(storeInfoMap)
                                    }
                                    allCountNum++
                                }
                                startActivity(mainpageintent)
                                false
                                Toast.makeText(this@AddActivity, "저장 완료!", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                        6 -> { //산책하기 좋은 공원 Park1
                            var countNum: Int = 0
                            var allCountNum: Int = 0
                            binding.mainLayout.saveBtn.setOnClickListener {
                                if (binding.mainLayout.addName.text.toString()
                                        .equals("") || binding.mainLayout.addAdress.text.toString()
                                        .equals("") || binding.mainLayout.addIntroduce.text.toString()
                                        .equals("")
                                ) {
                                    Toast.makeText(
                                        this@AddActivity,
                                        "모든 정보를 입력해주세요",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    db.collection("Park").document("Park1").get().addOnSuccessListener {
                                        countNum = it["count"].hashCode()
                                        var storeInfoMap = hashMapOf(
                                            "address" to binding.mainLayout.addAdress.text.toString(),
                                            "name" to binding.mainLayout.addName.text.toString(),
                                            "introduce" to binding.mainLayout.addIntroduce.text.toString(),
                                            "storeNum" to "Park1_" + countNum.toString(),
                                            "Tname" to "산책하기 좋은 공원",
                                            "placeImage" to downloadUri.toString(),
                                            "x" to x1,
                                            "y" to y1,
                                            "Rcount" to 0,
                                            "Tcount" to countNum,
                                        )
                                        db.collection("Park").document("Park1").collection("Park1")
                                            .document("Park1_" + countNum.toString())
                                            .set(storeInfoMap)
                                        countNum++

                                        var countMap = hashMapOf(
                                            "count" to countNum,
                                            "Tcollect" to "Park",
                                            "Tname" to "산책하기 좋은 공원",
                                            "Tnum" to "Park1",
                                            "Timg" to "https://firebasestorage.googleapis.com/v0/b/mapsie-1b20c.appspot.com/o/park_dog_walking.png?alt=media&token=e7ae1cd9-9c27-4634-b5bf-aa58c2d5a087"
                                        )
                                        db.collection("Park").document("Park1").set(countMap)

                                        // firebase All 전체 저장
                                        db.collection("All").document("All7").collection("All7")
                                            .document("All7_" + countNum.toString())
                                            .set(storeInfoMap)
                                    }
                                    allCountNum++
                                }
                                startActivity(mainpageintent)
                                false
                                Toast.makeText(this@AddActivity, "저장 완료!", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                        7 -> { //런닝하기 좋은 공원 Park2
                            var countNum: Int = 0
                            var allCountNum: Int = 0
                            binding.mainLayout.saveBtn.setOnClickListener {
                                if (binding.mainLayout.addName.text.toString()
                                        .equals("") || binding.mainLayout.addAdress.text.toString()
                                        .equals("") || binding.mainLayout.addIntroduce.text.toString()
                                        .equals("")
                                ) {
                                    Toast.makeText(
                                        this@AddActivity,
                                        "모든 정보를 입력해주세요",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {

                                    db.collection("Park").document("Park2").get().addOnSuccessListener {
                                        countNum = it["count"].hashCode()
                                        var storeInfoMap = hashMapOf(
                                            "address" to binding.mainLayout.addAdress.text.toString(),
                                            "name" to binding.mainLayout.addName.text.toString(),
                                            "introduce" to binding.mainLayout.addIntroduce.text.toString(),
                                            "storeNum" to "Park2_" + countNum.toString(),
                                            "Tname" to "런닝하기 좋은 공원",
                                            "placeImage" to downloadUri.toString(),
                                            "x" to x1,
                                            "y" to y1,
                                            "Rcount" to 0,
                                            // position 넘겨오기 위해 집어넣음
                                            "Tcount" to countNum,
                                        )
                                        db.collection("Park").document("Park2").collection("Park2")
                                            .document("Park2_" + countNum.toString())
                                            .set(storeInfoMap)
                                        countNum++

                                        var countMap = hashMapOf(
                                            "count" to countNum,
                                            "Tcollect" to "Park",
                                            "Tname" to "런닝하기 좋은 공원",
                                            "Tnum" to "Park2",
                                            "Timg" to "https://firebasestorage.googleapis.com/v0/b/mapsie-1b20c.appspot.com/o/park_running.png?alt=media&token=d1a92e35-68b4-4322-b4a7-b920b7fb3111"
                                        )
                                        db.collection("Park").document("Park2").set(countMap)

                                        // firebase All 전체 저장
                                        db.collection("All").document("All8").collection("All8")
                                            .document("All8_" + countNum.toString())
                                            .set(storeInfoMap)
                                    }
                                    allCountNum++
                                }
                                startActivity(mainpageintent)
                                false
                                Toast.makeText(this@AddActivity, "저장 완료!", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                        8 -> { //꽃구경하기 좋은 공원 Park3
                            var countNum: Int = 0
                            var allCountNum: Int = 0
                            binding.mainLayout.saveBtn.setOnClickListener {
                                if (binding.mainLayout.addName.text.toString()
                                        .equals("") || binding.mainLayout.addAdress.text.toString()
                                        .equals("") || binding.mainLayout.addIntroduce.text.toString()
                                        .equals("")
                                ) {
                                    Toast.makeText(
                                        this@AddActivity,
                                        "모든 정보를 입력해주세요",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    db.collection("Park").document("Park3").get().addOnSuccessListener {
                                        countNum = it["count"].hashCode()
                                        var storeInfoMap = hashMapOf(
                                            "address" to binding.mainLayout.addAdress.text.toString(),
                                            "name" to binding.mainLayout.addName.text.toString(),
                                            "introduce" to binding.mainLayout.addIntroduce.text.toString(),
                                            "storeNum" to "Park3_" + countNum.toString(),
                                            "Tname" to "꽃구경하기 좋은 공원",
                                            "placeImage" to downloadUri.toString(),
                                            "x" to x1,
                                            "y" to y1,
                                            "Rcount" to 0,
                                            "Tcount" to countNum,
                                        )
                                        db.collection("Park").document("Park3").collection("Park3")
                                            .document("Park3_" + countNum.toString())
                                            .set(storeInfoMap)
                                        countNum++

                                        var countMap = hashMapOf(
                                            "count" to countNum,
                                            "Tcollect" to "Park",
                                            "Tname" to "꽃구경하기 좋은 공원",
                                            "Tnum" to "Park3",
                                            "Timg" to "https://firebasestorage.googleapis.com/v0/b/mapsie-1b20c.appspot.com/o/park_flower.png?alt=media&token=a5d207ec-1245-4d1f-9072-d233720958db"
                                        )
                                        db.collection("Park").document("Park3").set(countMap)

                                        // firebase All 전체 저장
                                        db.collection("All").document("All9").collection("All9")
                                            .document("All9_" + countNum.toString())
                                            .set(storeInfoMap)
                                    }
                                    allCountNum++
                                }
                                startActivity(mainpageintent)
                                false
                                Toast.makeText(this@AddActivity, "저장 완료!", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    }
                }
            }
        binding.mainLayout.addImg.setOnClickListener {
            openGallery()
        }
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
            R.id.mypage-> startActivity(Intent(this, MyPageActivity::class.java))
            R.id.guideline-> startActivity(Intent(this, GuideActivity::class.java))
            R.id.addPage -> startActivity(Intent(this, AddActivity::class.java))
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

    // 키워드 검색 함수
    private fun searchKeyword(keyword: String, page: Int) {
        val retrofit = Retrofit.Builder()          // Retrofit 구성
            .baseUrl(SearchActivity.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(KakaoAPI::class.java)            // 통신 인터페이스를 객체로 생성
        val call =
            api.getSearchKeyword(SearchActivity.API_KEY, "경기도 시흥시 $keyword", page)    // 검색 조건 입력

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

    //갤러리에서 사진 가져오기
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = MediaStore.Images.Media.CONTENT_TYPE
        intent.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        intent.type = "image/*"
        startActivityForResult(intent, OPEN_GALLERY)
    }

    //firebae storage 이미지 업로드
    private fun uploadImageTOFirebase(uri: Uri) {
        storage = FirebaseStorage.getInstance()   //FirebaseStorage 인스턴스 생성
        imagesRef =
            storage.reference.child("placeImg/").child(fileName)    //기본 참조 위치/placeImg/${fileName}
        //이미지 파일 업로드
        var uploadTask = imagesRef.putFile(uri)
        uploadTask.addOnSuccessListener { taskSnapshot ->
            Toast.makeText(this, "성공", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            println(it)
            Toast.makeText(this, "실패", Toast.LENGTH_SHORT).show()
        }

        val urlTask = uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            imagesRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                downloadUri = task.result
            } else {
                Toast.makeText(this, "다운로드 실패", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @Override
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == OPEN_GALLERY) {
                uploadImageTOFirebase(data?.data!!)
                try {
                    binding.mainLayout.placeImgAddress.text = data?.data.toString()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }


    // 자동으로 키보드 내리기
    fun softkeyboardHide() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(add_name.windowToken, 0)
    }
}

