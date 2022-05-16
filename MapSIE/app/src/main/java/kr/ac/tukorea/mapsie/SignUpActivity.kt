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

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        var db: FirebaseFirestore = Firebase.firestore
        binding.SignUpBtn.setOnClickListener {
            if(binding.signName.text.toString().equals("")
                ||binding.signID.text.toString().equals("")
                ||binding.passwdText.text.toString().equals("")
                ||binding.passwdCkText.text.toString().equals("")
            ){
                Toast.makeText(this, "모든 정보를 입력해주세요", Toast.LENGTH_SHORT).show()
            }
            else if(!binding.passwdText.text.toString().equals(binding.passwdCkText.text.toString())){
                Toast.makeText(this, "두 비밀번호가 일치하지 않습니다!\n", Toast.LENGTH_SHORT).show()
            }else{
                Firebase.auth.createUserWithEmailAndPassword(
                    binding.signID.text.toString(),
                    binding.passwdText.text.toString()
                ).addOnCompleteListener(this){
                    if (it.isSuccessful) {
                        val userMap = hashMapOf(
                            "signName" to binding.signName.text.toString(),
                            "signID" to binding.signID.text.toString(),
                            "passwd" to binding.passwdText.text.toString(),
                            "passwdCk" to binding.passwdCkText.text.toString(),
                        )
                        db.collection("users")
                            .document(Firebase.auth.currentUser?.uid ?: "No User")
                            .set(userMap)
                        startActivity(
                            Intent(this, LoginActivity::class.java)
                        )
                        finish()
                    }else{
                        Toast.makeText(this, "회원가입 실패", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    }
