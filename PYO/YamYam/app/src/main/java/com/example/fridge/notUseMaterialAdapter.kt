package com.example.fridge

/* 안씀
*10.07 notUseMaterialAdapter 정의
* 일단 name 과 image 만 intent 에 담아서 넘기도록 정의했는데 필요에 따라 늘릴 수 있음

* */

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.example.yamyam.MainActivity
import com.example.yamyam.R
import kotlinx.android.synthetic.main.entry_material.view.*



class notUseMaterialAdapter : BaseAdapter {
    var materialsList : java.util.ArrayList<Material>  = ArrayList<Material>()
    var context: Context? = null

    constructor(context: Context?, materialList: java.util.ArrayList<Material> ) : super() {
        this.context = context
        this.materialsList = materialList
    }

    override fun getCount(): Int {
        return materialsList.size
    }
    override fun getItem(position: Int): Any {
        return materialsList[position]
    }
    override fun getItemId(position: Int): Long{
        return position.toLong()
    }
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val material = this.materialsList[position]

        var inflator = context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        var materialView = inflator.inflate(R.layout.entry_material, null)

        materialView.imgMaterial.setImageResource(material.image!!)
        materialView.nameMaterial.text = material.name!!

        return materialView
    }

    fun removeItem(position : Int){
        materialsList.removeAt(position)
        notifyDataSetChanged()
    }
}
