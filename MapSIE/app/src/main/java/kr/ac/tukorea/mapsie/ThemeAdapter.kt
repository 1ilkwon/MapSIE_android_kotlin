package kr.ac.tukorea.mapsie;

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.kakao.sdk.common.KakaoSdk.init
import kotlinx.android.synthetic.main.theme_item.view.*
import kr.ac.tukorea.mapsie.MapPage.ThemePlaceRecycleActivity

class ThemeAdapter(private val context:Context, private val themeList: ArrayList<ThemeData>):
    RecyclerView.Adapter<ThemeAdapter.ItemViewHolder>(){
        inner class ItemViewHolder(itemView:View): RecyclerView.ViewHolder(itemView){
            private var view:View = itemView

            fun bind(listener: View.OnClickListener, item:ThemeData) {
                view.theme_title.text = item.title
                Glide.with(itemView).load(item.img).into(view.theme_img)
                view.setOnClickListener(listener)
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThemeAdapter.ItemViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.theme_item, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = themeList[position]
        val listener = View.OnClickListener { it ->
            //Toast.makeText(it.context, "title : ${item.title}", Toast.LENGTH_SHORT).show()
            val intent1 = Intent(context, MapActivity::class.java)

            intent1.putExtra("ThemeName", item.num)
            intent1.putExtra("ThemeCollection", item.collect)
            intent1.run { context.startActivity(this) }

            val reintent = Intent(context, ThemePlaceRecycleActivity::class.java)
//
//            reintent.putExtra("ThemeName", item.num)
//            reintent.putExtra("ThemeCollection", item.collect)

        }

        holder.apply {
            bind(listener, item)
            itemView.tag = item
        }
    }

    override fun getItemCount(): Int {
        return themeList.size
    }
}
