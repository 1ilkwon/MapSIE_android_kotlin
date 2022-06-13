package kr.ac.tukorea.mapsie.MapPage

import android.hardware.Camera
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.SearchView
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

        //테마검색
        binding.searchThemePlace.setOnQueryTextListener(object: SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                themePlaceAdapter.getFilter().filter(newText)
                return false
            }
        })
        initRecycler()
    }

    private fun initRecycler(){
        var modelList = ArrayList<ThemePlaceList>()

        //Intent로 값 가져오기
        Log.d("Theme1", Tvalue)
        Log.d("Theme2", TCollect)

        db.collection(TCollect).document(Tvalue)
            .collection(Tvalue)
            .get().addOnSuccessListener { result ->
                for (document in result) {
                    var name = document.data?.get("name").toString()
                    var address = document["address"].toString()
                    var theme = document["Tname"].toString()
                    var storeNum = document["storeNum"].toString()
                    var placeImage = document["placeImage"].toString()
                    //modelList.add(ThemePlaceList(name,address))
                    modelList.add(ThemePlaceList(name, address, placeImage, theme, storeNum))    //이미지까지

                    themePlaceAdapter = ThemePlaceAdapter(this,modelList)
                    binding.re.adapter = themePlaceAdapter

                    val layout = LinearLayoutManager(this)
                    binding.re.layoutManager = layout
                    binding.re.setHasFixedSize(true)
                }
            }
    }
}

