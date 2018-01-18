package com.ymlion.smsidentify;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

/**
 * sms view holder
 *
 * Created by YMlion on 2018/1/16.
 */

class SmsViewHolder extends RecyclerView.ViewHolder {

    TextView addrTv;
    TextView dateTv;
    TextView bodyTv;
    CheckBox cb;

    SmsViewHolder(View itemView) {
        super(itemView);
        addrTv = itemView.findViewById(R.id.tv_address);
        dateTv = itemView.findViewById(R.id.tv_date);
        bodyTv = itemView.findViewById(R.id.tv_body);
        cb = itemView.findViewById(R.id.cb_select);
    }
}