package kr.ac.tukorea.mapsie

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.detail_body.*
import kotlinx.android.synthetic.main.main_body.*
import kotlinx.android.synthetic.main.main_drawer_header.*
import kotlinx.android.synthetic.main.main_toolbar.*
import kr.ac.tukorea.mapsie.databinding.ActivityDetailBinding
import kr.ac.tukorea.mapsie.databinding.ActivityMainBinding

class DetailActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityDetailBinding
    // firestore 연결 위해 기입
    var db: FirebaseFirestore = Firebase.firestore

    //recyclerview를 위한 코드
    lateinit var reviewAdapter: ReviewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) //왼쪽에 뒤로가기버튼생성
        supportActionBar?.setDisplayShowTitleEnabled(false) // 툴바에 타이틀 안보이게
        toolbar.title = "MapSIE"
        binding.navigationView.setNavigationItemSelectedListener(this)

        db.collection("users").document(Firebase.auth.currentUser?.uid ?: "No User").get().addOnSuccessListener {
            member_nickname.text = it["signName"].toString()
        }.addOnFailureListener {
            Toast.makeText(this, ".", Toast.LENGTH_SHORT).show()
        }

        initRecycler()  //recyclerview 보여주는 메서드
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.detail_toolbar_menu, menu)       //툴바 메뉴
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {   //툴바에 있는 메뉴 누를 때
        when(item.itemId){
            android.R.id.home-> {   //뒤로가기 버튼
                finish()
                return true
            }
            R.id.heart->{ // 하트 버튼
                Toast.makeText(this,"저장완료" , Toast.LENGTH_SHORT).show()
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
            R.id.home -> Toast.makeText(this,"홈화면 실행",Toast.LENGTH_SHORT).show()
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
                                ActivityCompat.finishAffinity(this@DetailActivity)
                                System.runFinalization()
                                System.exit(0)
                            }, 1000)
                        }
                        setNegativeButton("아니요"){_,_,->
                            return@setNegativeButton
                        }
                        show()
                    }
            }
        }
        return false
    }


    private fun initRecycler(){
        var reviewList = arrayListOf<ReviewData>( //리뷰 추가

        )
        reviewAdapter = ReviewAdapter(this, reviewList)
        binding.mainLayout.reviewRecycler.adapter = reviewAdapter

        reviewAdapter.notifyDataSetChanged()
    }
}