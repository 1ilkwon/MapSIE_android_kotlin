package kr.ac.tukorea.mapsie

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.main_drawer_header.*
import kr.ac.tukorea.mapsie.databinding.ActivityAddBinding
import kr.ac.tukorea.mapsie.databinding.ActivityReviewBinding

class ReviewActivity : AppCompatActivity() {
    //프로필사진 가져오기 위해서
    var downloadUri: Uri? = null
    private lateinit var binding: ActivityReviewBinding
    // firestore 연결 위해 기입
    var db: FirebaseFirestore = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db.collection("users").document(Firebase.auth.currentUser?.uid ?: "No User").get().addOnSuccessListener {
            binding.memberNickname.text = it["signName"].toString()
            Glide.with(this)
                .load(it["signImg"])
                .override(60, 60)
                .error(R.drawable.ic_baseline_account_circle_24)    //에러가 났을 때
                .fallback(R.drawable.ic_baseline_account_circle_24) //signImg값이 없다면 기본 사진 출력
                .into(binding.memberIcon)
        }.addOnFailureListener {
            Toast.makeText(this, ".", Toast.LENGTH_SHORT).show()
        }

        //취소버튼 눌렀을 때
        binding.cancelReview.setOnClickListener{
            startActivity(Intent(this, DetailActivity::class.java))
        }
        //완료버튼 눌렀을 때
        binding.writeReview.setOnClickListener{

        }


    }
}