package kr.ac.tukorea.mapsie

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kr.ac.tukorea.mapsie.MapPage.ThemePlaceList

class ThemeDialogActivity : AppCompatActivity() {

    //데이터를 담을 배열
    var themePlaceList = ArrayList<ThemePlaceList>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_theme_dialog)


        //10번 반복
        for (i in 1..10){
            var myModel = ThemePlaceList(placename = "서운칼국수", placeadress = "경기도 시흥시 능곡로10" )
        }
    }
}