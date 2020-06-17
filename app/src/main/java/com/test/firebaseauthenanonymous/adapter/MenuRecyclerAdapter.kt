package com.test.firebaseauthenanonymous.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.test.firebaseauthenanonymous.databinding.ViewHolderMenuBinding
import com.test.firebaseauthenanonymous.model.MenuModel

/**
 * Created by PrewSitthirat on 17/6/2020 AD.
 */

class MenuRecyclerAdapter : RecyclerView.Adapter<MenuRecyclerAdapter.MenuViewHolder>() {

    var menuList: ArrayList<MenuModel>? = arrayListOf()
    var listener: ((String) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val binding = ViewHolderMenuBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MenuViewHolder(binding.root)
    }

    override fun getItemCount(): Int {
        return if (menuList.isNullOrEmpty()) {
            0
        } else {
            if (menuList!!.size > 4) {
                4
            } else {
                menuList!!.size
            }
        }
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        holder.bindView(menuList!![position])
    }

    inner class MenuViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val binding = ViewHolderMenuBinding.bind(itemView)

        fun bindView(menuModel: MenuModel) {
            binding.btnMenu.text = menuModel.title?.get("th") ?: ""
            binding.btnMenu.setOnClickListener {
                menuModel.key?.let {
                    listener?.invoke(it)
                }
            }
        }
    }
}