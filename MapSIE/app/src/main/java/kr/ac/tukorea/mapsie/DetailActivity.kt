package kr.ac.tukorea.mapsie

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.activity_sign_up.*
import kotlinx.android.synthetic.main.detail_body.*
import kotlinx.android.synthetic.main.main_body.*
import kotlinx.android.synthetic.main.main_drawer_header.*
import kotlinx.android.synthetic.main.main_toolbar.*
import kr.ac.tukorea.mapsie.MapActivity.Companion.TCollect
import kr.ac.tukorea.mapsie.MapActivity.Companion.Tvalue
import kr.ac.tukorea.mapsie.databinding.ActivityDetailBinding
import kr.ac.tukorea.mapsie.databinding.ActivityMainBinding

class DetailActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityDetailBinding
    // firestore 연결 위해 기입
    var db: FirebaseFirestore = Firebase.firestore


    //recyclerview를 위한 코드
    lateinit var reviewAdapter: ReviewAdapter
    //intent로 받기


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var sImage = intent.getStringExtra("Simage")
        Glide.with(this).load(sImage)
            .error(R.drawable.ic_baseline_image_24)
            .fallback(R.drawable.ic_baseline_image_24)
            .into(place_image)

        var sName = intent.getStringExtra("Sname")
        Log.d("sname",sName.toString())
        var sAddress = intent.getStringExtra("Saddress")
        var sTheme = intent.getStringExtra("Stheme")
        var Position = intent.getStringExtra("Position")
        var introduce = intent.getStringExtra("Introduce")

        //var sStoreNum = intent.getStringExtra("SstoreName")
        Log.d("snamead", sAddress.toString())
        Log.d("position", Position.toString())
        binding.mainLayout.placeName.text = sName.toString()
        binding.mainLayout.placeAddress.text = sAddress.toString()
        binding.mainLayout.placeTheme.text = sTheme.toString()
        binding.mainLayout.placeIntroduce.text = introduce.toString()

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) //왼쪽에 뒤로가기버튼생성
        supportActionBar?.setDisplayShowTitleEnabled(false) // 툴바에 타이틀 안보이게
        toolbar.title = "MapSIE"
        binding.navigationView.setNavigationItemSelectedListener(this)

        db.collection("users").document(Firebase.auth.currentUser?.uid ?: "No User").get().addOnSuccessListener {
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

        //리뷰 쓰는 버튼 누르면 review페이지로 화면전환
        binding.mainLayout.writeReview.setOnClickListener{
            val intent = Intent(this, ReviewActivity::class.java)
            intent.putExtra("position", Position)
            intent.run{
                startActivity(this)
            }
//            startActivity(
//                Intent(this, ReviewActivity::class.java)
//
            //)
        }

        initRecycler()  //recyclerview 보여주는 메서드
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.detail_toolbar_menu, menu)       //툴바 메뉴
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {   //툴바에 있는 메뉴 누를 때
        var sStoreNum = intent.getStringExtra("SstoreName")
        var sName = intent.getStringExtra("Sname")
        var sTheme = intent.getStringExtra("Stheme")
        var countNum : Int
        when(item.itemId){
            android.R.id.home-> {   //뒤로가기 버튼
                finish()
                return true
            }
            R.id.heart->{ // 하트 버튼
                //Toast.makeText(this,"저장완료" , Toast.LENGTH_SHORT).show()
                db.collection("users").document(Firebase.auth.currentUser?.uid?:"No User")
                    .get().addOnSuccessListener {
                        countNum = it["count"].hashCode()
                        var heartMap = hashMapOf(
                            "name" to sName,
                            "Tname" to sTheme,
                        )
                        db.collection("users").document(Firebase.auth.currentUser?.uid?:"No User")
                            .collection("hearts").document("hearts_" + countNum.toString())
                            .set(heartMap)
                        countNum++

                        // count 하나씩 올려줌
                        db.collection("users").document(Firebase.auth.currentUser?.uid?:"No User")
                            .update("count", FieldValue.increment(1))
                        Toast.makeText(this,"즐겨찾기 완료", Toast.LENGTH_SHORT).show()
                    }


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
        } else{
            super.onBackPressed()
        }
    }
    override fun onNavigationItemSelected(item: MenuItem): Boolean {    //메뉴바 클릭 시 실행하는 메서드
        when(item.itemId){
            R.id.home -> startActivity(Intent(this, MainActivity::class.java))
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
//        var reviewList = arrayListOf<ReviewData>( //리뷰 추가
//        )
        var reviewList = ArrayList<ReviewData>()
        var Position = intent.getStringExtra("Position")

        db.collection(TCollect).document(Tvalue).collection(Tvalue)
            .document(Tvalue + "_" + Position).collection("review")
            .get().addOnSuccessListener { result ->
                for(document in result) {
                    var content = document["context"].toString()
                    var nickname = document["userName"].toString()
                    var icon = document["userIcon"].toString()
                    // var nickname
                    reviewList.add(ReviewData(icon,nickname,content))

                    reviewAdapter = ReviewAdapter(this, reviewList)
                    binding.mainLayout.reviewRecycler.adapter = reviewAdapter

                    val layout = LinearLayoutManager(this)
                    binding.mainLayout.reviewRecycler.layoutManager = layout
                    binding.mainLayout.reviewRecycler.setHasFixedSize(true)
                }
            }
//        reviewAdapter = ReviewAdapter(this, reviewList)
//        binding.mainLayout.reviewRecycler.adapter = reviewAdapter

//        reviewAdapter.notifyDataSetChanged()
    }
}