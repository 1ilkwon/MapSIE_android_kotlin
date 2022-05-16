package kr.ac.tukorea.mapsie

import android.app.Application
import com.kakao.sdk.common.KakaoSdk

class GlobalApplication: Application() {
    override fun onCreate() {
        super.onCreate()
// 네이티브 앱 키
        KakaoSdk.init(this, "c58b4187a45508c35d8fcb2bb5f40aa5")
    }
}