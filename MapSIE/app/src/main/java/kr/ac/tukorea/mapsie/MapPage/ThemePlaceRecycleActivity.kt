package kr.ac.tukorea.mapsie.MapPage

import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import kr.ac.tukorea.mapsie.R
import kr.ac.tukorea.mapsie.ThemeAdapter
import kr.ac.tukorea.mapsie.ThemeData

class ThemePlaceRecycleActivity : AppCompatActivity() {

    //데이터를 담을 배열
    var modelList = ArrayList<ThemePlaceList>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_theme_place_recycle)


        for (i in 1..10) {
            var place1 = ThemePlaceList(
                placename = "서운 칼국수",
                placeaddress = "경기도 시흥시 능곡동",
                placeimage =
            )
            this.modelList.add(place1)
        }
        initRecycler()
    }

}