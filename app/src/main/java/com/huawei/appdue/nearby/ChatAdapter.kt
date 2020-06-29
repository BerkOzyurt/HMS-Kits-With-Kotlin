package com.huawei.appdue.nearby

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.huawei.appdue.R
import java.util.ArrayList


class ChatAdapter: BaseAdapter {


    private var mContext: Context? = null
    private var msgList: List<MessageBean>? = null

    constructor(
        mContext: Context?,
        msgList: List<MessageBean>?
    ) {
        this.mContext = mContext
        this.msgList = msgList
    }

    override fun getCount(): Int {
        return msgList!!.size
    }

    override fun getItem(position: Int): Any? {
        return msgList!![position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        var convertView: View? = convertView
        val holder: ViewHolder
        val item = msgList!![position]
        if (convertView == null) {
            convertView =
                LayoutInflater.from(mContext).inflate(R.layout.message_list_item, parent, false)
            holder = ViewHolder()
            holder.sendTv = convertView.findViewById(R.id.tv_send)
            holder.receiveTv = convertView.findViewById(R.id.tv_receive)
            convertView.setTag(holder)
        } else {
            holder = (if (convertView.getTag() is ViewHolder) {
                convertView.getTag()
            } else {
                ViewHolder()
            }) as ViewHolder
        }
        if (item.isSend) {
            holder.sendTv!!.setVisibility(View.VISIBLE)
            holder.receiveTv!!.setVisibility(View.GONE)
            holder.sendTv!!.setText(item.msg)
        } else {
            holder.sendTv!!.setVisibility(View.GONE)
            holder.receiveTv!!.setVisibility(View.VISIBLE)
            holder.receiveTv!!.setText(item.msg)
        }
        return convertView
    }

    internal class ViewHolder {
        var sendTv: TextView? = null
        var receiveTv: TextView? = null
    }

    fun refreshData(list: List<MessageBean?>?) {
        msgList = list as List<MessageBean>?
        notifyDataSetChanged()
    }

}