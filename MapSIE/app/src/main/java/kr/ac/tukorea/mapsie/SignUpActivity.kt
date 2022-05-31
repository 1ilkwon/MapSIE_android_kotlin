package kr.ac.tukorea.mapsie

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kr.ac.tukorea.mapsie.databinding.ActivitySignUpBinding

// 회원가입 관련 Activity
class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //binding으로 변경
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //FireStore 쓰기위해 사용
        var db: FirebaseFirestore = Firebase.firestore
        // 회원가입 버튼 누르면 나타나는 이벤트
        binding.SignUpBtn.setOnClickListener {
            // 입력칸들 중 하나라도 비어있다면 -> 모든 정보를 입력해주세요
            if(binding.signName.text.toString().equals("")
                ||binding.signID.text.toString().equals("")
                ||binding.passwdText.text.toString().equals("")
                ||binding.passwdCkText.text.toString().equals("")
                ||binding.addressText.text.toString().equals("")
                ||binding.introduceText.text.toString().equals("")
            ){
                Toast.makeText(this, "모든 정보를 입력해주세요", Toast.LENGTH_SHORT).show()
            }
            // 비밀번호와 비밀번호 확인이 일치하지 않으면 -> 두 비밀번호가 일치하지 않습니다
            else if(!binding.passwdText.text.toString().equals(binding.passwdCkText.text.toString())){
                Toast.makeText(this, "두 비밀번호가 일치하지 않습니다!\n", Toast.LENGTH_SHORT).show()
            }
            // 위 두 경우가 아니면 Firebase auth에 Email과 Password를 담아 회원을 생성해 줌
            else{
                Firebase.auth.createUserWithEmailAndPassword(
                    binding.signID.text.toString(),
                    binding.passwdText.text.toString()
                ).addOnCompleteListener(this){
                    if (it.isSuccessful) {
                        // userMap에 입력받은 정보들을 담음
                        val userMap = hashMapOf(
                            "signName" to binding.signName.text.toString(),
                            "signID" to binding.signID.text.toString(),
                            "passwd" to binding.passwdText.text.toString(),
                            "passwdCk" to binding.passwdCkText.text.toString(),
                            "address" to binding.addressText.text.toString(),
                            "introduce" to binding.introduceText.text.toString(),
                        )
                        // db안에 있는 users 컬렉션에 위 userMap에서 담은 정보를 넣어줌
                        db.collection("users")
                            .document(Firebase.auth.currentUser?.uid ?: "No User")
                            .set(userMap)
                        //회원가입 완료 후에는 LoginActivity로 인텐트
                        startActivity(
                            Intent(this, LoginActivity::class.java)
                        )
                        finish()
                    } // 위에서 말한 모든 경우에 해당하지 않고 회원가입 실패 -> 회원가입 실패
                    else{
                        Toast.makeText(this, "회원가입 실패", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
