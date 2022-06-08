package kr.ac.tukorea.mapsie

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kr.ac.tukorea.mapsie.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //binding으로 변경
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // 로그인 버튼 클릭 이벤트 -> doLogin(하단에 함수 있음)
        binding.login.setOnClickListener{
            val userEmail = binding.username.text.toString()
            val password = binding.password.text.toString()
            doLogin(userEmail, password)
        }

        // 회원가입 버튼 클릭 이벤트 -> 회원가입 페이지로 인텐트
        binding.signUp.setOnClickListener{
            startActivity(
                Intent(this, SignUpActivity::class.java)
            )
        }

        /*로그인 생략 지우면됨*/
        startActivity(
            Intent(this, MainActivity::class.java)
        )

    }

    // 로그인 함수, Firebase auth에 입력받은 userEmail과 password가 있다면 로그인 성공시켜서 MainActivity로 인텐트 시킴
    private fun doLogin(userEmail: String, password: String) {
        Firebase.auth.signInWithEmailAndPassword(userEmail, password)
            .addOnCompleteListener(this) { // it: Task<AuthResult!>
                if (it.isSuccessful) {
                    startActivity(
                        Intent(this, MainActivity::class.java)
                    )
                    finish()
                } else {
                    Log.w("LoginActivity", "signInWithEmail", it.exception)
                    Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }
}