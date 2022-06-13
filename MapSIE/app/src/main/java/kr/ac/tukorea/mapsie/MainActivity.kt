package kr.ac.tukorea.mapsie

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.main_drawer_header.*
import kotlinx.android.synthetic.main.main_drawer_header.member_icon
import kotlinx.android.synthetic.main.main_drawer_header.member_nickname
import kotlinx.android.synthetic.main.main_toolbar.*
import kotlinx.android.synthetic.main.review_item.*
import kr.ac.tukorea.mapsie.SearchPage.SearchActivity
import kr.ac.tukorea.mapsie.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityMainBinding
    // firestore 연결 위해 기입
    var db: FirebaseFirestore = Firebase.firestore

    var pos = 0
    var i : Int = 1
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


    //recyclerview를 위한 코themeAdapter드
    lateinit var themeAdapter: ThemeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //binding 방식으로 변경
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)



        //만약 firebase auth에 현재 user가 비어있으면 시작 페이지를 LoginActivity로 하고 auth가 있으면(즉, 로그인 되어있으면) MainActivity로 연결
        if (Firebase.auth.currentUser == null) {
            startActivity(
                Intent(this, LoginActivity::class.java)
            )
            finish()
        }
        setSupportActionBar(toolbar)

        //테마검색
        binding.mainLayout.searchTheme.setOnQueryTextListener(object: SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                themeAdapter.getFilter().filter(newText)
                return false
            }
        })

        supportActionBar?.setDisplayHomeAsUpEnabled(true) //왼쪽에 뒤로가기버튼생성
        supportActionBar?.setDisplayShowTitleEnabled(false) // 툴바에 타이틀 안보이게
        toolbar.title = "MapSIE"
        binding.navigationView.setNavigationItemSelectedListener(this)

        binding.mainLayout.searchTheme.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }

        db.collection("users").document(Firebase.auth.currentUser?.uid ?: "No User").get().addOnSuccessListener {
            member_nickname.text = it["signName"].toString()
            Glide.with(this)
                .load(it["signImg"])
                .override(60, 60)
                .error(R.drawable.ic_baseline_account_circle_24)    //에러가 났을 때
                .fallback(R.drawable.ic_baseline_account_circle_24) //signImg값이 없다면 기본 사진 출력
                .into(member_icon)
        }.addOnFailureListener {
            Toast.makeText(this, ".", Toast.LENGTH_SHORT).show()
        }


        initRecycler()  //recyclerview 보여주는 메서드
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)       //툴바 메뉴
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {   //툴바에 있는 메뉴 누를 때
        when(item.itemId){
            android.R.id.home-> {   //뒤로가기 버튼
                finish()
                return true
            }
            R.id.toolbar_menu->{ // 메뉴 버튼
                drawer_layout.openDrawer(GravityCompat.END)    // 네비게이션 드로어 열기(오른쪽에서 왼쪽으로)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {  //기본 폰에 내장되어 있는 ◀뒤로가기 누르면
        if(drawer_layout.isDrawerOpen(GravityCompat.END)){
            drawer_layout.closeDrawers()
            // 테스트를 위해 뒤로가기 버튼시 Toast 메시지
            Toast.makeText(this,"뒤로가기버튼 테스트",Toast.LENGTH_SHORT).show()
        } else{
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {    //메뉴바 클릭 시 실행하는 메서드
        when(item.itemId){
            R.id.home -> Toast.makeText(this, "홈화면 실행", Toast.LENGTH_SHORT).show()
            R.id.mypage-> startActivity(Intent(this, MyPageActivity::class.java))
            R.id.guideline-> startActivity(Intent(this, GuideActivity::class.java))
            R.id.addPage -> startActivity(Intent(this, AddActivity::class.java))
            R.id.logout->{
                val builder = AlertDialog.Builder(this)
                    .apply {
                        setTitle("알림")
                        setMessage("로그아웃 하시겠습니까?")
                        setPositiveButton("네") { _, _ ->
                            FirebaseAuth.getInstance().signOut()
                            Handler().postDelayed({
                                ActivityCompat.finishAffinity(this@MainActivity)
                                System.runFinalization()
                                System.exit(0)
                            }, 1000)
                        }
                        setNegativeButton("아니요"){ _, _ ->
                            return@setNegativeButton
                        }
                        show()
                    }
            }
            R.id.addPage -> startActivity(Intent(this, AddActivity::class.java))
        }
        return false
    }
    private fun initRecycler(){
        // **<생각해 보니 테마까지 db에서 가져올 필요 없을 수도 있을 것 같음> => 수정 필요
        // 각각의 리사이클러뷰 아이템 클릭하면 지도 떠야함 => 연결 필요
        // All에 넣는게 더 효율적을 것으로 보이긴 함
        var themeList = arrayListOf<ThemeData>()
        db.collection("Cafes").get().addOnSuccessListener { result ->
            for (document in result) {
                var Tname = document.data?.get("Tname").toString()
                var Timg = document["Timg"].toString()
                var Tnum = document["Tnum"].toString()
                var Tcollect = document["Tcollect"].toString()
                themeList.add(
                    ThemeData(Timg, Tname, Tnum, Tcollect)
                )
                themeAdapter = ThemeAdapter(this,themeList)
                binding.mainLayout.themeRecycler.adapter = themeAdapter
                themeAdapter.notifyDataSetChanged()
            }
        }
        db.collection("Foods").get().addOnSuccessListener { result ->
            for (document in result) {
                var Tname = document.data?.get("Tname").toString()
                var Timg = document["Timg"].toString()
                var Tnum = document["Tnum"].toString()
                var Tcollect = document["Tcollect"].toString()
                themeList.add(
                    ThemeData(Timg, Tname,Tnum, Tcollect)
                )
                themeAdapter = ThemeAdapter(this,themeList)
                binding.mainLayout.themeRecycler.adapter = themeAdapter
                themeAdapter.notifyDataSetChanged()
            }
        }
        db.collection("Park").get().addOnSuccessListener { result ->
            for (document in result) {
                var Tname = document.data?.get("Tname").toString()
                var Timg = document["Timg"].toString()
                var Tnum = document["Tnum"].toString()
                var Tcollect = document["Tcollect"].toString()
                themeList.add(
                    ThemeData(Timg, Tname,Tnum, Tcollect)
                )
                themeAdapter = ThemeAdapter(this,themeList)
                binding.mainLayout.themeRecycler.adapter = themeAdapter
                themeAdapter.notifyDataSetChanged()
            }
        }
        // 첫 페이지에서 All 눌렀을 때
        // (차후에 firebase에 'All' Collection 만들어서 나누는게 효율적일지 고민 중)
        binding.mainLayout.all.setOnClickListener {
        var themeList = arrayListOf<ThemeData>()
            db.collection("Cafes").get().addOnSuccessListener { result ->
                for (document in result) {
                    var Tname = document.data?.get("Tname").toString()
                    var Timg = document["Timg"].toString()
                    var Tnum = document["Tnum"].toString()
                    var Tcollect = document["Tcollect"].toString()
                    themeList.add(
                        ThemeData(Timg, Tname,Tnum, Tcollect)
                    )
                    themeAdapter = ThemeAdapter(this,themeList)
                    binding.mainLayout.themeRecycler.adapter = themeAdapter
                    themeAdapter.notifyDataSetChanged()
            }
                }
                db.collection("Foods").get().addOnSuccessListener { result ->
                    for (document in result) {
                        var Tname = document.data?.get("Tname").toString()
                        var Timg = document["Timg"].toString()
                        var Tnum = document["Tnum"].toString()
                        var Tcollect = document["Tcollect"].toString()
                        themeList.add(
                            ThemeData(Timg, Tname,Tnum, Tcollect)
                        )
                        themeAdapter = ThemeAdapter(this,themeList)
                        binding.mainLayout.themeRecycler.adapter = themeAdapter
                        themeAdapter.notifyDataSetChanged()
                    }
                }
                db.collection("Park").get().addOnSuccessListener { result ->
                    for (document in result) {
                        var Tname = document.data?.get("Tname").toString()
                        var Timg = document["Timg"].toString()
                        var Tnum = document["Tnum"].toString()
                        var Tcollect = document["Tcollect"].toString()
                        themeList.add(
                            ThemeData(Timg, Tname,Tnum,Tcollect)
                        )
                        themeAdapter = ThemeAdapter(this,themeList)
                        binding.mainLayout.themeRecycler.adapter = themeAdapter
                        themeAdapter.notifyDataSetChanged()
                    }
                }
            }


        binding.mainLayout.cafe.setOnClickListener {
            var themeList = arrayListOf<ThemeData>()
            db.collection("Cafes").get().addOnSuccessListener { result ->
                for (document in result) {
                    var Tname = document.data?.get("Tname").toString()
                    var Timg = document["Timg"].toString()
                    var Tnum = document["Tnum"].toString()
                    var Tcollect = document["Tcollect"].toString()
                    themeList.add(
                        ThemeData(Timg, Tname,Tnum, Tcollect)
                    )
                    themeAdapter = ThemeAdapter(this,themeList)
                    binding.mainLayout.themeRecycler.adapter = themeAdapter
                    themeAdapter.notifyDataSetChanged()
                }
            }
        }
        binding.mainLayout.food.setOnClickListener {
            var themeList = arrayListOf<ThemeData>()
            db.collection("Foods").get().addOnSuccessListener { result ->
                for (document in result) {
                    var Tname = document.data?.get("Tname").toString()
                    var Timg = document["Timg"].toString()
                    var Tnum = document["Tnum"].toString()
                    var Tcollect = document["Tcollect"].toString()
                    themeList.add(
                        ThemeData(Timg, Tname,Tnum, Tcollect)
                    )
                    themeAdapter = ThemeAdapter(this,themeList)
                    binding.mainLayout.themeRecycler.adapter = themeAdapter
                    themeAdapter.notifyDataSetChanged()
                }
            }
        }
        binding.mainLayout.park.setOnClickListener {
            var themeList = arrayListOf<ThemeData>()
            db.collection("Park").get().addOnSuccessListener { result ->
                for (document in result) {
                    var Tname = document.data?.get("Tname").toString()
                    var Timg = document["Timg"].toString()
                    var Tnum = document["Tnum"].toString()
                    var Tcollect = document["Tcollect"].toString()
                    themeList.add(
                        ThemeData(Timg, Tname, Tnum,Tcollect)
                    )
                    themeAdapter = ThemeAdapter(this,themeList)
                    binding.mainLayout.themeRecycler.adapter = themeAdapter
                    themeAdapter.notifyDataSetChanged()
                }
            }
        }
    }
}


