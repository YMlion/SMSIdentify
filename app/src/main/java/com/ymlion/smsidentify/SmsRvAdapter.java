package com.ymlion.smsidentify;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.List;

/**
 * Created by YMlion on 2018/1/16.
 */

public class SmsRvAdapter extends RecyclerView.Adapter<SmsViewHolder> {

    private Context mContext;
    private List<SMSMessage> mData;

    SmsRvAdapter(Context context, List<SMSMessage> list) {
        mContext = context;
        mData = list;
    }

    @Override public SmsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_sms, parent, false);
        return new SmsViewHolder(view);
    }

    @Override public void onBindViewHolder(SmsViewHolder holder, int position) {
        SMSMessage sms = mData.get(position);
        holder.addrTv.setText(getPerson(sms.address));
        holder.dateTv.setText(SMSMessage.formatDate(sms.date));
        holder.bodyTv.setText(sms.body);
        holder.cb.setTag(sms.id + "");
        holder.cb.setChecked(sms.checked);
        holder.cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Object tag = buttonView.getTag();
            if (tag.equals(mData.get(position).id + "")) {
                mData.get(position).checked = isChecked;
            }
        });
    }

    @Override public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    private String getPerson(String address) {
        try {
            ContentResolver resolver = mContext.getContentResolver();
            Uri uri =
                    Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, address);
            Cursor cursor;
            cursor = resolver.query(uri, new String[] { ContactsContract.PhoneLookup.DISPLAY_NAME },
                    null, null, null);
            if (cursor != null) {
                try {
                    if (cursor.getCount() != 0) {
                        cursor.moveToFirst();
                        String name = cursor.getString(0);
                        return name;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    cursor.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return address;
    }
}
