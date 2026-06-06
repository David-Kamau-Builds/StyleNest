package com.app.stylenest.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ImageView
import android.widget.TextView
import com.app.stylenest.R

class SidebarExpandableAdapter(
    private val context: Context,
    private val groupList: List<String>,
    private val childList: Map<String, List<String>>
) : BaseExpandableListAdapter() {

    override fun getGroupCount(): Int = groupList.size
    override fun getChildrenCount(groupPosition: Int): Int = childList[groupList[groupPosition]]?.size ?: 0
    override fun getGroup(groupPosition: Int): Any = groupList[groupPosition]
    override fun getChild(groupPosition: Int, childPosition: Int): Any = childList[groupList[groupPosition]]!![childPosition]
    override fun getGroupId(groupPosition: Int): Long = groupPosition.toLong()
    override fun getChildId(groupPosition: Int, childPosition: Int): Long = childPosition.toLong()
    override fun hasStableIds(): Boolean = false
    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean = true

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
        var view = convertView
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_category_group, parent, false)
        }
        val groupName = getGroup(groupPosition) as String
        val tvGroup = view!!.findViewById<TextView>(R.id.tvGroupName)
        val ivIndicator = view.findViewById<ImageView>(R.id.ivIndicator)
        
        tvGroup.text = groupName
        
        // Hide arrow for "All" category since it has no children
        if (groupName == "All") {
            ivIndicator.visibility = View.GONE
        } else {
            ivIndicator.visibility = View.VISIBLE
            ivIndicator.rotation = if (isExpanded) 180f else 0f
        }
        
        return view
    }

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
        var view = convertView
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_category_child, parent, false)
        }
        val childName = getChild(groupPosition, childPosition) as String
        val tvChild = view!!.findViewById<TextView>(R.id.tvChildName)
        tvChild.text = childName
        return view
    }
}
