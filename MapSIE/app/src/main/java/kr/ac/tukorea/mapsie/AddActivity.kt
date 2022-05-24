package kr.ac.tukorea.mapsie

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kr.ac.tukorea.mapsie.databinding.ActivityAddBinding
import kr.ac.tukorea.mapsie.databinding.ActivityMainBinding
import kr.ac.tukorea.mapsie.databinding.AddBodyBinding

class AddActivity : AppCompatActivity() {
    private lateinit var binding: AddBodyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)

        //binding 방식으로 변경
        binding = AddBodyBinding.inflate(layoutInflater)
        setContentView(binding.root)


    }
}