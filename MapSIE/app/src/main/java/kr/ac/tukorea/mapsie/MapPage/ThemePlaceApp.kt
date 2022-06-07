package kr.ac.tukorea.mapsie.MapPage

import android.app.Application

class ThemePlaceApp :Application() {

    companion object {
        lateinit var instance: ThemePlaceApp
        private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

}