package kr.ac.tukorea.mapsie

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.main_toolbar.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar) // 툴바를 액티비티의 앱바로 지정
        supportActionBar?.setDisplayHomeAsUpEnabled(true) //왼쪽에 뒤로가기버튼생성
        //supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_new_24) // 홈버튼 이미지 변경
        supportActionBar?.setDisplayShowTitleEnabled(false) // 툴바에 타이틀 안보이게
        toolbar.title = "MapSIE"

        navigationView.setNavigationItemSelectedListener(this) //navigation 리스너
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home-> {
                finish()
                return true
            }
            R.id.toolbar_menu->{ // 메뉴 버튼
                drawer_layout.openDrawer(GravityCompat.END)    // 네비게이션 드로어 열기
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if(drawer_layout.isDrawerOpen(GravityCompat.END)){
            drawer_layout.closeDrawers()
            // 테스트를 위해 뒤로가기 버튼시 Toast 메시지
            Toast.makeText(this,"back btn clicked",Toast.LENGTH_SHORT).show()
        } else{
            super.onBackPressed()
        }
    }
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.home1-> Toast.makeText(this,"menu_item1 실행",Toast.LENGTH_SHORT).show()
            R.id.mypage-> Toast.makeText(this,"menu_item2 실행",Toast.LENGTH_SHORT).show()
            R.id.guideline-> Toast.makeText(this,"menu_item3 실행",Toast.LENGTH_SHORT).show()
        }
        return false
    }
}