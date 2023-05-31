package com.example.p2pconnectionexample

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.p2pconnectionexample.databinding.P2plistRecyclerviewThemeBinding

data class P2pDevice (val name: String, val address: String)

class P2pListAdapter(private val context: Context, private val list: ArrayList<P2pDevice>)
    : RecyclerView.Adapter<P2pListAdapter.MainViewHolder>(){

    private lateinit var binding: P2plistRecyclerviewThemeBinding
    private var mItemClickListener: MyItemClickListener ?= null

    interface MyItemClickListener{
        fun onItemClick(pos: Int, name: String?)
    }

    fun setMyItemClickListener(listener: MyItemClickListener) {
        this.mItemClickListener = listener
    }

    inner class MainViewHolder(private val binding: P2plistRecyclerviewThemeBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: P2pDevice) {
            binding.model = item
            if(item.name == SharedPreference.latest_p2p_device) {
                Log.d(TAG, "최근에 업데이트한 내용이 있음.[${item.name}]")
                //binding.icConn.visibility = View.VISIBLE
                binding.mP2pList.setTypeface(null, Typeface.BOLD)
            }
            Log.d(TAG, "item = ${item.name}")

            this.itemView.setOnClickListener {
                Log.d(TAG, "SetOnClick pos = $adapterPosition")
                this.itemView.setBackgroundColor(Color.parseColor("#fbfffb"))
                mItemClickListener?.onItemClick(adapterPosition, item.name)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        Log.d(TAG, "onCreateViewHolder()")
        binding = P2plistRecyclerviewThemeBinding.inflate(LayoutInflater.from(context), parent, false)

        return MainViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        Log.d(TAG, "onBindViewHolder()")

        val item = list[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        Log.d(TAG, "getItemCount()")
        return list.size
    }

    companion object {
        private val TAG = P2pListAdapter::class.java.simpleName
    }
}