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
import com.kakao.sdk.auth.LoginClient
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.AuthErrorCause
import com.kakao.sdk.common.util.Utility
import com.kakao.sdk.user.UserApiClient
import kr.ac.tukorea.mapsie.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    var db: FirebaseFirestore = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Firebase.auth.currentUser == null) {
            startActivity(
                Intent(this, LoginActivity::class.java)
            )
            finish()
        }

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

        db.collection("users").document(Firebase.auth.currentUser?.uid ?: "No User").get().addOnSuccessListener {
            binding.welcome.text = "닉네임: " + it["signName"].toString()
        }.addOnFailureListener {
            Toast.makeText(this, "내 정보 불러오기 실패. \n 다시 Login 해주세요.", Toast.LENGTH_SHORT).show()
        }
    }
}