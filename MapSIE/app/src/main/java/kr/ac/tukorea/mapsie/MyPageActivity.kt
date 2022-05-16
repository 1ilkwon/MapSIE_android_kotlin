package kr.ac.tukorea.mapsie

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kr.ac.tukorea.mapsie.databinding.ActivityMainBinding
import kr.ac.tukorea.mapsie.databinding.ActivityMyPageBinding

class MyPageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMyPageBinding
    // firestore 연결 위해 기입
    var db: FirebaseFirestore = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //db에서 값 꺼내오는 파트
        db.collection("users").document(Firebase.auth.currentUser?.uid?: "No User").get().addOnSuccessListener {
            binding.signName.text = it["signName"].toString()
            binding.address.text = it["address"].toString()
            binding.introduce.text = it["introduce"].toString()
        }.addOnFailureListener{
            Toast.makeText(this, "실패\n", Toast.LENGTH_SHORT).show()
        }
    }
}