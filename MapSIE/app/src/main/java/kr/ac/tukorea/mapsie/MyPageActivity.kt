package kr.ac.tukorea.mapsie

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_my_page.*
import kr.ac.tukorea.mapsie.MapPage.ThemePlaceAdapter
import kr.ac.tukorea.mapsie.databinding.ActivityMyPageBinding

class MyPageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMyPageBinding
    // firestore 연결 위해 기입
    var db: FirebaseFirestore = Firebase.firestore

    lateinit var themePlaceAdapter: ThemePlaceAdapter

    var heartList = arrayListOf<Heart>(
        Heart("이름 테스트1", "주소 테스트1"),
        Heart("이름 테스트2", "주소 테스트2"),
        Heart("이름 테스트3", "주소 테스트3"),
        Heart("이름 테스트4", "주소 테스트4"),
        Heart("이름 테스트5", "주소 테스트5"),
        Heart("이름 테스트6", "주소 테스트6"),
        Heart("이름 테스트7", "주소 테스트7")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val heartAdapter = HeartListAdapter(this, heartList)
        heart_ListView.adapter = heartAdapter

        //db에서 값 꺼내오는 파트
        db.collection("users").document(Firebase.auth.currentUser?.uid?: "No User").get().addOnSuccessListener {
            Glide.with(this)
                .load(it["signImg"])
                .override(60, 60)
                .error(R.drawable.ic_baseline_account_circle_24)    //에러가 났을 때
                .fallback(R.drawable.ic_baseline_account_circle_24) //signImg값이 없다면 기본 사진 출력
//                .into(binding.signImg)
            binding.signName.text = it["signName"].toString()
            binding.address.text = it["address"].toString()
            binding.introduce.text = it["introduce"].toString()
        }.addOnFailureListener{
            Toast.makeText(this, "실패\n", Toast.LENGTH_SHORT).show()
        }
    }
}