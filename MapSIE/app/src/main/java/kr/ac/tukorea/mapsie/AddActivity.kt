package kr.ac.tukorea.mapsie

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Adapter
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.main_toolbar.*
import kr.ac.tukorea.mapsie.databinding.ActivityAddBinding

class AddActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
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
                            var allCountNum: Int = 0
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
                                    db.collection("Cafes").document("Cafe1").collection("Cafe1")
                                        .document("Cafe1_" + countNum.toString())
                                        .set(storeInfoMap)
                                    // 0 -> {...} 함수 내에서 count를 해줌으로 하나의 테마에 새로운 장소가 저장될 때마다 각각 1을 count 해줌
                                    countNum++

                                    // firebase All 전체 저장
                                    db.collection("All").document("All1").collection("All1").document("All1_" + countNum.toString())
                                        .set(storeInfoMap)
                                    allCountNum++
                                }
                                // db에 저장 완료 시 "저장완료" 토스트메시지로 출력
                                Toast.makeText(this@AddActivity, "저장 완료!", Toast.LENGTH_SHORT).show()

                            }
                        } // 이하 반복
                        1 -> { //디저트 맛집 Cafe2
                            var countNum: Int = 0
                            var allCountNum: Int = 0
                            binding.mainLayout.saveBtn.setOnClickListener {
                                if (binding.mainLayout.addName.text.toString().equals("") || binding.mainLayout.addAdress.text.toString().equals("") || binding.mainLayout.addIntroduce.text.toString().equals(""))
                                {Toast.makeText(this@AddActivity, "모든 정보를 입력해주세요", Toast.LENGTH_SHORT).show()
                                } else {
                                    var storeInfoMap = hashMapOf(
                                        "address" to binding.mainLayout.addAdress.text.toString(),
                                        "name" to binding.mainLayout.addName.text.toString(),
                                        "introduce" to binding.mainLayout.addIntroduce.text.toString()
                                    )
                                    db.collection("Cafes").document("Cafe2").collection("Cafe2")
                                        .document("Cafe2_" + countNum.toString())
                                        .set(storeInfoMap)
                                    countNum++

                                    // firebase All 전체 저장
                                    db.collection("All").document("All2").collection("All2").document("All2_" + countNum.toString())
                                        .set(storeInfoMap)
                                    allCountNum++
                                }
                                Toast.makeText(this@AddActivity, "저장 완료!", Toast.LENGTH_SHORT).show()
                            }
                        }
                        2 -> { //뷰가 좋은 카페 Cafe3
                            var countNum: Int = 0
                            var allCountNum: Int = 0
                            binding.mainLayout.saveBtn.setOnClickListener {
                                if (binding.mainLayout.addName.text.toString().equals("") || binding.mainLayout.addAdress.text.toString().equals("") || binding.mainLayout.addIntroduce.text.toString().equals(""))
                                {Toast.makeText(this@AddActivity, "모든 정보를 입력해주세요", Toast.LENGTH_SHORT).show()
                                } else {
                                    var storeInfoMap = hashMapOf(
                                        "address" to binding.mainLayout.addAdress.text.toString(),
                                        "name" to binding.mainLayout.addName.text.toString(),
                                        "introduce" to binding.mainLayout.addIntroduce.text.toString()
                                    )
                                    db.collection("Cafes").document("Cafe3").collection("Cafe3")
                                        .document("Cafe3_" + countNum.toString())
                                        .set(storeInfoMap)
                                    countNum++

                                    // firebase All 전체 저장
                                    db.collection("All").document("All3").collection("All3").document("All3_" + countNum.toString())
                                        .set(storeInfoMap)
                                    allCountNum++
                                }
                                Toast.makeText(this@AddActivity, "저장 완료!", Toast.LENGTH_SHORT).show()
                            }
                        }
                        3 -> { //양식이 땡길 때 Food1
                            var countNum: Int = 0
                            var allCountNum: Int = 0
                            binding.mainLayout.saveBtn.setOnClickListener {
                                if (binding.mainLayout.addName.text.toString().equals("") || binding.mainLayout.addAdress.text.toString().equals("") || binding.mainLayout.addIntroduce.text.toString().equals(""))
                                {Toast.makeText(this@AddActivity, "모든 정보를 입력해주세요", Toast.LENGTH_SHORT).show()
                                } else {
                                    var storeInfoMap = hashMapOf(
                                        "address" to binding.mainLayout.addAdress.text.toString(),
                                        "name" to binding.mainLayout.addName.text.toString(),
                                        "introduce" to binding.mainLayout.addIntroduce.text.toString()
                                    )
                                    db.collection("Foods").document("Food1").collection("Food1")
                                        .document("Food1_" + countNum.toString())
                                        .set(storeInfoMap)
                                    countNum++

                                    // firebase All 전체 저장
                                    db.collection("All").document("All4").collection("All4").document("All4_" + countNum.toString())
                                        .set(storeInfoMap)
                                    allCountNum++
                                }
                                Toast.makeText(this@AddActivity, "저장 완료!", Toast.LENGTH_SHORT).show()
                            }
                        }
                        4 -> { //혼밥하기 좋은 곳 Food2
                            var countNum: Int = 0
                            var allCountNum: Int = 0
                            binding.mainLayout.saveBtn.setOnClickListener {
                                if (binding.mainLayout.addName.text.toString().equals("") || binding.mainLayout.addAdress.text.toString().equals("") || binding.mainLayout.addIntroduce.text.toString().equals(""))
                                {Toast.makeText(this@AddActivity, "모든 정보를 입력해주세요", Toast.LENGTH_SHORT).show()
                                } else {
                                    var storeInfoMap = hashMapOf(
                                        "address" to binding.mainLayout.addAdress.text.toString(),
                                        "name" to binding.mainLayout.addName.text.toString(),
                                        "introduce" to binding.mainLayout.addIntroduce.text.toString()
                                    )
                                    db.collection("Foods").document("Food2").collection("Food2")
                                        .document("Food2_" + countNum.toString())
                                        .set(storeInfoMap)
                                    countNum++

                                    // firebase All 전체 저장
                                    db.collection("All").document("All5").collection("All5").document("All5_" + countNum.toString())
                                        .set(storeInfoMap)
                                    allCountNum++
                                }
                                Toast.makeText(this@AddActivity, "저장 완료!", Toast.LENGTH_SHORT).show()
                            }
                        }
                        5 -> { //소개팅 할 때 추천 Food3
                            var countNum: Int = 0
                            var allCountNum: Int = 0
                            binding.mainLayout.saveBtn.setOnClickListener {
                                if (binding.mainLayout.addName.text.toString().equals("") || binding.mainLayout.addAdress.text.toString().equals("") || binding.mainLayout.addIntroduce.text.toString().equals(""))
                                {Toast.makeText(this@AddActivity, "모든 정보를 입력해주세요", Toast.LENGTH_SHORT).show()
                                } else {
                                    var storeInfoMap = hashMapOf(
                                        "address" to binding.mainLayout.addAdress.text.toString(),
                                        "name" to binding.mainLayout.addName.text.toString(),
                                        "introduce" to binding.mainLayout.addIntroduce.text.toString()
                                    )
                                    db.collection("Foods").document("Food3").collection("Food3")
                                        .document("Food3_" + countNum.toString())
                                        .set(storeInfoMap)
                                    countNum++

                                    // firebase All 전체 저장
                                    db.collection("All").document("All6").collection("All6").document("All6_" + countNum.toString())
                                        .set(storeInfoMap)
                                    allCountNum++
                                }
                                Toast.makeText(this@AddActivity, "저장 완료!", Toast.LENGTH_SHORT).show()
                            }
                        }
                        6 -> { //산책하기 좋은 공원 Park1
                            var countNum: Int = 0
                            var allCountNum: Int = 0
                            binding.mainLayout.saveBtn.setOnClickListener {
                                if (binding.mainLayout.addName.text.toString().equals("") || binding.mainLayout.addAdress.text.toString().equals("") || binding.mainLayout.addIntroduce.text.toString().equals(""))
                                {Toast.makeText(this@AddActivity, "모든 정보를 입력해주세요", Toast.LENGTH_SHORT).show()
                                } else {
                                    var storeInfoMap = hashMapOf(
                                        "address" to binding.mainLayout.addAdress.text.toString(),
                                        "name" to binding.mainLayout.addName.text.toString(),
                                        "introduce" to binding.mainLayout.addIntroduce.text.toString()
                                    )
                                    db.collection("Park").document("Park1").collection("Park1")
                                        .document("Park1_"+ countNum.toString())
                                        .set(storeInfoMap)
                                    countNum++

                                    // firebase All 전체 저장
                                    db.collection("All").document("All7").collection("All7").document("All7_" + countNum.toString())
                                        .set(storeInfoMap)
                                    allCountNum++
                                }
                                Toast.makeText(this@AddActivity, "저장 완료!", Toast.LENGTH_SHORT).show()
                            }
                        }
                        7 -> { //런닝하기 좋은 공원 Park2
                            var countNum: Int = 0
                            var allCountNum: Int = 0
                            binding.mainLayout.saveBtn.setOnClickListener {
                                if (binding.mainLayout.addName.text.toString().equals("") || binding.mainLayout.addAdress.text.toString().equals("") || binding.mainLayout.addIntroduce.text.toString().equals(""))
                                {Toast.makeText(this@AddActivity, "모든 정보를 입력해주세요", Toast.LENGTH_SHORT).show()
                                } else {
                                    var storeInfoMap = hashMapOf(
                                        "address" to binding.mainLayout.addAdress.text.toString(),
                                        "name" to binding.mainLayout.addName.text.toString(),
                                        "introduce" to binding.mainLayout.addIntroduce.text.toString()
                                    )
                                    db.collection("Park").document("Park2").collection("Park2")
                                        .document("Park2_"+ countNum.toString())
                                        .set(storeInfoMap)
                                    countNum++

                                    // firebase All 전체 저장
                                    db.collection("All").document("All8").collection("All8").document("All8_" + countNum.toString())
                                        .set(storeInfoMap)
                                    allCountNum++
                                }
                                Toast.makeText(this@AddActivity, "저장 완료!", Toast.LENGTH_SHORT).show()
                            }
                        }
                        8 -> { //꽃구경하기 좋은 공원 Park3
                            var countNum: Int = 0
                            var allCountNum: Int = 0
                            binding.mainLayout.saveBtn.setOnClickListener {
                                if (binding.mainLayout.addName.text.toString().equals("") || binding.mainLayout.addAdress.text.toString().equals("") || binding.mainLayout.addIntroduce.text.toString().equals(""))
                                {Toast.makeText(this@AddActivity, "모든 정보를 입력해주세요", Toast.LENGTH_SHORT).show()
                                } else {
                                    var storeInfoMap = hashMapOf(
                                        "address" to binding.mainLayout.addAdress.text.toString(),
                                        "name" to binding.mainLayout.addName.text.toString(),
                                        "introduce" to binding.mainLayout.addIntroduce.text.toString()
                                    )
                                    db.collection("Park").document("Park3").collection("Park3")
                                        .document("Park3_"+ countNum.toString())
                                        .set(storeInfoMap)
                                    countNum++

                                    // firebase All 전체 저장
                                    db.collection("All").document("All9").collection("All9").document("All9_" + countNum.toString())
                                        .set(storeInfoMap)
                                    allCountNum++
                                }
                                Toast.makeText(this@AddActivity, "저장 완료!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
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
}
