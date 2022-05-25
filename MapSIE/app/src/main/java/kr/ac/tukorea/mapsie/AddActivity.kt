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
import androidx.core.view.GravityCompat
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.add_body.*
import kotlinx.android.synthetic.main.main_drawer_header.*
import kotlinx.android.synthetic.main.main_toolbar.*
import android.widget.ArrayAdapter
import kr.ac.tukorea.mapsie.databinding.ActivityAddBinding
import kr.ac.tukorea.mapsie.databinding.ActivityMainBinding
import kr.ac.tukorea.mapsie.databinding.AddBodyBinding

class AddActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
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

        db.collection("users").document(Firebase.auth.currentUser?.uid ?: "No User").get().addOnSuccessListener {
            member_nickname.text = it["signName"].toString()
        }.addOnFailureListener {
            Toast.makeText(this, ".", Toast.LENGTH_SHORT).show()
        }

        binding.mainLayout.saveBtn.setOnClickListener{
            Toast.makeText(this, "저장되었습니다.", Toast.LENGTH_SHORT).show()
        }

        themeSpinner() //스피너메서드

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
            R.id.home -> startActivity(Intent(this, MainActivity::class.java))
            R.id.mypage-> startActivity(Intent(this, MyPageActivity::class.java))
            R.id.guideline-> startActivity(Intent(this, GuideActivity::class.java))
            R.id.addPage -> Toast.makeText(this,"추가화면 실행",Toast.LENGTH_SHORT).show()
            R.id.logout->{
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
                        setNegativeButton("아니요"){_,_,->
                            return@setNegativeButton
                        }
                        show()
                    }
            }
        }
        return false
    }

    private fun themeSpinner(){ //테마스피너 항목 추가 및 어댑터
        var themeList = arrayOf("카공하기 좋은 카페", "혼밥하기 좋은 식당", "꽃구경 가기 좋은 공원")

        var adapter:ArrayAdapter<String>
        adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, themeList)
        binding.mainLayout.addTheme.adapter = adapter

    }
}