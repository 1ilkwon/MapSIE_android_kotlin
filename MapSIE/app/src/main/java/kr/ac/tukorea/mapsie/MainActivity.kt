package kr.ac.tukorea.mapsie

import android.os.Bundle
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import com.naver.maps.map.NaverMapSdk
import com.naver.maps.map.NaverMapSdk.NaverCloudPlatformClient


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var actionBar : ActionBar?
        actionBar = supportActionBar
        actionBar?.hide()

        NaverMapSdk.getInstance(this).client = NaverCloudPlatformClient("1lidcv9gdv")
    }
}