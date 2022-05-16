package kr.ac.tukorea.mapsie

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kr.ac.tukorea.mapsie.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    // firestore 연결 위해 기입
    var db: FirebaseFirestore = Firebase.firestore

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

        // 로그아웃 버튼 누르면 dialog페이지로 예, 아니오 AlertDialog 나타남
        binding.buttonSignout.setOnClickListener {
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
                    setNegativeButton("아니요"){_,_,->
                        return@setNegativeButton
                    }
                    show()
                }


        }

        binding.btnMyPage.setOnClickListener{
            startActivity(
                Intent(this, MyPageActivity::class.java)
            )
        }

        //db에서 users 컬렉션에서 해당하는 닉네임 가져오는 부분
        db.collection("users").document(Firebase.auth.currentUser?.uid ?: "No User").get().addOnSuccessListener {
            binding.welcome.text = "닉네임: " + it["signName"].toString()
        }.addOnFailureListener {
            Toast.makeText(this, "내 정보 불러오기 실패. \n 다시 Login 해주세요.", Toast.LENGTH_SHORT).show()
        }
    }
}