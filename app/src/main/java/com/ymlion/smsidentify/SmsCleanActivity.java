package com.ymlion.smsidentify;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.List;

/**
 * 短信清理
 */
public class SmsCleanActivity extends AppCompatActivity {

    private static final String TAG = "SmsCleanActivity";
    private static final String[] TITLE = { "验证码", "106短信", "其他" };
    private static final int N = 3;
    private List<SMSMessage> smsList;
    private TabLayout typeTab;
    private ViewPager smsVp;
    private RecyclerView[] rvs;
    private List<List<SMSMessage>> smsLists;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_clean);
        initView();
        initData();
    }

    private void initView() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        typeTab = findViewById(R.id.tab_type);
        smsVp = findViewById(R.id.vp_sms);
        typeTab.setupWithViewPager(smsVp);
        typeTab.setTabMode(TabLayout.MODE_SCROLLABLE);
        rvs = new RecyclerView[N];
        setRefreshLayout();
    }

    private void setRefreshLayout() {
        mSwipeRefreshLayout = findViewById(R.id.srl_sms);
        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            mSwipeRefreshLayout.setRefreshing(false);
            // TODO: 2018/1/16 refresh
        });
    }

    private void initData() {
        smsVp.setAdapter(new SmsPagerAdapter());
        smsLists = new ArrayList<>();
        for (int i = 0; i < N; i++) {
            smsLists.add(new ArrayList<>());
        }
        checkPermission();
    }

    private void checkPermission() {
        if (PackageManager.PERMISSION_DENIED == checkSelfPermission(Manifest.permission.READ_SMS)) {
            requestPermissions(new String[] {
                    Manifest.permission.READ_SMS, Manifest.permission.READ_CONTACTS
            }, 0x1);
        } else {
            new Thread(this::queryMessage).start();
        }
    }

    private void queryMessage() {
        smsList = new ArrayList<>();
        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(Telephony.Sms.CONTENT_URI, new String[] {
                Telephony.Sms._ID, Telephony.Sms.ADDRESS, Telephony.Sms.BODY, Telephony.Sms.DATE,
                Telephony.Sms.READ, Telephony.Sms.STATUS, Telephony.Sms.TYPE,
        }, null, null, "date DESC");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                SMSMessage message = new SMSMessage();
                message.id = cursor.getInt(0);
                message.address = cursor.getString(1);
                message.body = cursor.getString(2);
                message.date = cursor.getLong(3);
                message.read = cursor.getInt(4);
                message.status = cursor.getInt(5);
                message.type = cursor.getInt(6);
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
                if (sms.isReceived()) {
                    if (sms.body.contains("验证码")) {
                        codeCount++;
                        Log.i(TAG, "message : " + sms.toString());
                        smsLists.get(0).add(sms);
                    } else if (sms.address.startsWith("106")) {
                        smsLists.get(1).add(sms);
                    } else {
                        smsLists.get(2).add(sms);
                    }
                }
            }
            runOnUiThread(() -> {
                int i = 0;
                for (RecyclerView rv : rvs) {
                    if (rv != null) {
                        rv.getAdapter().notifyDataSetChanged();
                    }
                    typeTab.getTabAt(i).setText(TITLE[i] + "(" + smsLists.get(i).size() + ")");
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

    @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        if (requestCode == 0x1 && grantResults[0] == 0) {
            new Thread(this::queryMessage).start();
        }
    }

    private class SmsPagerAdapter extends PagerAdapter {

        @Override public int getCount() {
            return N;
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
