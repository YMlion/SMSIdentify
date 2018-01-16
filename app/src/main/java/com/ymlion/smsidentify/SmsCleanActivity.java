package com.ymlion.smsidentify;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

/**
 * 短信清理
 */
public class SmsCleanActivity extends Activity {

    private static final String TAG = "SmsCleanActivity";
    private static final String[] TITLE = { "验证码", "其他" };
    private List<SMSMessage> smsList;
    private TabLayout typeTab;
    private ViewPager smsVp;
    private RecyclerView[] rvs;
    private List<List<SMSMessage>> smsLists;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_clean);
        rvs = new RecyclerView[2];
        smsLists = new ArrayList<>();
        smsLists.add(new ArrayList<>());
        smsLists.add(new ArrayList<>());
        initView();
        new Thread(this::queryMessageLog).start();
    }

    private void initView() {
        typeTab = findViewById(R.id.tab_type);
        smsVp = findViewById(R.id.vp_sms);
        smsVp.setAdapter(new SmsPagerAdapter());
        typeTab.setupWithViewPager(smsVp);
        typeTab.setTabMode(TabLayout.MODE_SCROLLABLE);
        setRefreshLayout();
    }

    private void setRefreshLayout() {
        mSwipeRefreshLayout = findViewById(R.id.srl_sms);
        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            mSwipeRefreshLayout.setRefreshing(false);
            // TODO: 2018/1/16 refresh
        });
    }

    private void queryMessageLog() {
        smsList = new ArrayList<>();
        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(Telephony.Sms.CONTENT_URI, new String[] {
                Telephony.Sms.ADDRESS,   //
                Telephony.Sms.BODY, Telephony.Sms.DATE, Telephony.Sms.READ, Telephony.Sms.STATUS,
                Telephony.Sms.TYPE,
        }, null, null, "date DESC");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                SMSMessage message = new SMSMessage();
                message.address = cursor.getString(0);
                message.body = cursor.getString(1);
                message.date = cursor.getLong(2);
                message.read = cursor.getInt(3);
                message.status = cursor.getInt(4);
                message.type = cursor.getInt(5);
                //Log.i(TAG, "message : " + message.toString());
                smsList.add(message);
            }
            cursor.close();
            int sentCount = 0, readCount = 0;
            int codeCount = 0;
            for (SMSMessage sms : smsList) {
                if (sms.isSent()) {
                    sentCount++;
                }
                if (sms.isRead()) {
                    readCount++;
                }
                if (sms.body.contains("验证码")) {
                    codeCount++;
                    Log.i(TAG, "message : " + sms.toString());
                    smsLists.get(0).add(sms);
                } else if (sms.address.startsWith("106")) {
                    smsLists.get(1).add(sms);
                }
            }
            runOnUiThread(() -> {
                int i = 0;
                for (RecyclerView rv : rvs) {
                    if (rv != null) {
                        rv.getAdapter().notifyDataSetChanged();
                        typeTab.getTabAt(i).setText(TITLE[i] + "(" + smsLists.get(i).size() + ")");
                    }
                    i++;
                }
            });
            Log.d(TAG, "总共 "
                    + smsList.size()
                    + " 条短信，接收"
                    + (smsList.size() - sentCount)
                    + "条，发送"
                    + sentCount
                    + "条；已读"
                    + readCount
                    + "条，未读"
                    + (smsList.size() - readCount)
                    + "条，验证码共"
                    + codeCount
                    + "条");
        }
    }

    private class SmsPagerAdapter extends PagerAdapter {

        @Override public int getCount() {
            return 2;
        }

        @Override public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @NonNull @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            if (rvs[position] != null) {
                container.addView(rvs[position]);
                return rvs[position];
            }
            RecyclerView rv = new RecyclerView(SmsCleanActivity.this);
            rv.setLayoutParams(new ViewGroup.LayoutParams(-1, -1));
            rv.setLayoutManager(new LinearLayoutManager(SmsCleanActivity.this));
            rv.addItemDecoration(new DividerItemDecoration(SmsCleanActivity.this,
                    DividerItemDecoration.VERTICAL));
            rv.setAdapter(new SmsRvAdapter(SmsCleanActivity.this, smsLists.get(position)));
            rvs[position] = rv;
            container.addView(rv);
            return rv;
        }

        @Override public void destroyItem(@NonNull ViewGroup container, int position,
                @NonNull Object object) {
            container.removeView((View) object);
        }

        @Nullable @Override public CharSequence getPageTitle(int position) {
            return TITLE[position];
        }
    }
}
