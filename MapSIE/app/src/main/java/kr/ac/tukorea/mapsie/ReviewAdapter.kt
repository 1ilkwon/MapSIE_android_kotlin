package kr.ac.tukorea.mapsie;

import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteMisuseException
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.kakao.sdk.common.KakaoSdk.init
import kotlinx.android.synthetic.main.review_item.view.*
import kotlinx.android.synthetic.main.theme_item.view.*

public class ReviewAdapter (private val context:Context, private val reviewList: ArrayList<ReviewData>):
        RecyclerView.Adapter<ReviewAdapter.ItemViewHolder>() {

        inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
                private var view: View = itemView

                fun bind(listener: View.OnClickListener, item: ReviewData) {
                        view.member_icon.setImageDrawable(item.icon)
                        view.member_nickname.text = item.nickname
                        view.review_content.text = item.content
                        view.setOnClickListener(listener)
                }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewAdapter.ItemViewHolder {
                val view = LayoutInflater.from(context).inflate(R.layout.review_item, parent, false)
                return ItemViewHolder(view)
        }

        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
                val item = reviewList[position]
                val listener = View.OnClickListener { it ->
                        Toast.makeText(it.context, "title : ${item.content}", Toast.LENGTH_SHORT).show()
                }
                holder.apply {
                        bind(listener, item)
                        itemView.tag = item
                }
        }

        override fun getItemCount(): Int {
                return reviewList.size
        }
}