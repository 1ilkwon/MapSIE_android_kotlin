package kr.ac.tukorea.mapsie

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_sign_up.*
import kr.ac.tukorea.mapsie.databinding.ActivitySignUpBinding
import java.text.SimpleDateFormat
import java.util.*

// 회원가입 관련 Activity
class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var imagesRef: StorageReference

    private val OPEN_GALLERY = 1111
    var fileName: String = SimpleDateFormat("yyyymmdd_HHmmss").format(Date())
    var downloadUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //binding으로 변경
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //FireStore 쓰기위해 사용
        var db: FirebaseFirestore = Firebase.firestore
        storage = FirebaseStorage.getInstance()
        db = FirebaseFirestore.getInstance()

        binding.signImg.setOnClickListener {
            openGallery()
        }

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
                            "count" to 0,
                            "signImg" to downloadUri.toString()
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

    //갤러리에서 사진 가져오기
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = MediaStore.Images.Media.CONTENT_TYPE
        intent.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        intent.type = "image/*"
        startActivityForResult(intent, OPEN_GALLERY)
    }

    //firebae storage 이미지 업로드
    private fun uploadImageTOFirebase(uri: Uri) {
        storage = FirebaseStorage.getInstance()   //FirebaseStorage 인스턴스 생성
        imagesRef = storage.reference.child("profileImg/").child(fileName)    //기본 참조 위치/profileImg/${fileName}
        //이미지 파일 업로드
        var uploadTask = imagesRef.putFile(uri)
        uploadTask.addOnSuccessListener { taskSnapshot ->
            Toast.makeText(this, "성공", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            println(it)
            Toast.makeText(this, "실패", Toast.LENGTH_SHORT).show()
        }

        val urlTask = uploadTask.continueWithTask { task->
            if(!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            imagesRef.downloadUrl
        }.addOnCompleteListener{ task->
            if(task.isSuccessful){
                downloadUri = task.result
            } else{
                Toast.makeText(this, "다운로드 실패", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @Override
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if( resultCode == Activity.RESULT_OK) {
            if( requestCode ==  OPEN_GALLERY) {
                uploadImageTOFirebase(data?.data!!)
                try {
                    Glide.with(this)
                    .load(data?.data!!)
                    .override(60, 60)
                    .error(R.drawable.ic_baseline_account_circle_24)
                    .fallback(R.drawable.ic_baseline_add_a_photo_24)
                    .into(signImg)
                 }
                catch (e:Exception)
                {
                    e.printStackTrace()
                }
            }
        }
    }


}
