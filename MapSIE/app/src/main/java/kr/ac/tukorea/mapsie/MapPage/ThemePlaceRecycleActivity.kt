package kr.ac.tukorea.mapsie.MapPage

import android.content.Intent
import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_my_page.*
import kr.ac.tukorea.mapsie.DetailActivity
import kr.ac.tukorea.mapsie.MapActivity.Companion.TCollect
import kr.ac.tukorea.mapsie.MapActivity.Companion.Tvalue
import kr.ac.tukorea.mapsie.R
import kr.ac.tukorea.mapsie.databinding.ActivityMapBinding
import kr.ac.tukorea.mapsie.databinding.ActivityThemePlaceRecycleBinding

class ThemePlaceRecycleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityThemePlaceRecycleBinding

    var db: FirebaseFirestore = Firebase.firestore
    lateinit var themePlaceAdapter: ThemePlaceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityThemePlaceRecycleBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        initRecycler()

        }

    private fun initRecycler(){
        var modelList = ArrayList<ThemePlaceList>()

        //Intent로 값 가져오기
        Log.d("Theme1", Tvalue)
        Log.d("Theme2", TCollect.toString())

        db.collection(TCollect.toString()).document(Tvalue.toString())
            .collection(Tvalue.toString())
            .get().addOnSuccessListener { result ->
                for (document in result) {
                    var name = document.data?.get("name").toString()
                    var address = document["address"].toString()
                    //modelList.add(ThemePlaceList(name,address))
                    modelList.add(ThemePlaceList(name, address))

                    //Detail 으로 값 넘겨주기
                    val detailIntent = Intent(this, DetailActivity::class.java)
                    detailIntent.putExtra("Sname", name)
                    detailIntent.putExtra("Saddress",address)

                    val rAdapter = ThemePlaceAdapter(this,modelList)
                    binding.re.adapter = rAdapter

                    val layout = LinearLayoutManager(this)
                    binding.re.layoutManager = layout
                    binding.re.setHasFixedSize(true)
                }
            }
    }
}

