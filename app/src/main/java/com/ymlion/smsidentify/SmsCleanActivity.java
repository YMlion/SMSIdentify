package com.ymlion.smsidentify;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

/**
 * 短信清理
 */
public class SmsCleanActivity extends AppCompatActivity
        implements CompoundButton.OnCheckedChangeListener, ViewPager.OnPageChangeListener,
        View.OnClickListener {

    private static final String TAG = "SmsCleanActivity";
    private static final String[] TITLE = { "验证码", "订阅", "106", "未读", "已发送", "其他" };

    private static final int INDEX_CODE = 0;
    private static final int INDEX_SUB = 1;
    private static final int INDEX_106 = 2;
    private static final int INDEX_UNREAD = 3;
    private static final int INDEX_SENT = 4;
    private static final int INDEX_OTHERS = 5;

    private static final int NUM_TYPE = 6;

    private TabLayout typeTab;
    private ViewPager smsVp;
    private RecyclerView[] rvs;
    private List<List<SMSMessage>> smsLists;
    private boolean[] listCheckState;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private CheckBox listCheckBox;

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
        smsVp.addOnPageChangeListener(this);
        typeTab.setTabMode(TabLayout.MODE_SCROLLABLE);
        rvs = new RecyclerView[NUM_TYPE];
        setRefreshLayout();
        listCheckBox = findViewById(R.id.cb_list_select);
        listCheckBox.setOnCheckedChangeListener(this);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(this);
    }

    private void setRefreshLayout() {
        mSwipeRefreshLayout = findViewById(R.id.srl_sms);
        mSwipeRefreshLayout.setOnRefreshListener(
                () -> new Thread(() -> queryMessage(true)).start());
    }

    private void initData() {
        smsVp.setAdapter(new SmsPagerAdapter());
        smsLists = new ArrayList<>();
        listCheckState = new boolean[NUM_TYPE];
        for (int i = 0; i < NUM_TYPE; i++) {
            smsLists.add(new ArrayList<>());
        }
        if (Build.VERSION.SDK_INT > 22) {
            checkPermission();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M) private void checkPermission() {
        if (PackageManager.PERMISSION_DENIED == checkSelfPermission(Manifest.permission.READ_SMS)) {
            requestPermissions(new String[] {
                    Manifest.permission.READ_SMS, Manifest.permission.READ_CONTACTS
            }, 0x1);
        } else {
            new Thread(this::queryMessage).start();
        }
    }

    private void queryMessage() {
        queryMessage(false);
    }

    private void queryMessage(boolean refresh) {
        int total = 0;
        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(Telephony.Sms.CONTENT_URI, new String[] {
                Telephony.Sms._ID, Telephony.Sms.ADDRESS, Telephony.Sms.BODY, Telephony.Sms.DATE,
                Telephony.Sms.READ, Telephony.Sms.STATUS, Telephony.Sms.TYPE,
        }, null, null, "date DESC");
        if (cursor != null) {
            if (refresh) {
                for (List<SMSMessage> list : smsLists) {
                    list.clear();
                }
            }
            while (cursor.moveToNext()) {
                SMSMessage message = new SMSMessage();
                message.id = cursor.getInt(0);
                message.address = cursor.getString(1);
                message.body = cursor.getString(2);
                message.date = cursor.getLong(3);
                message.read = cursor.getInt(4);
                message.status = cursor.getInt(5);
                message.type = cursor.getInt(6);
                classifySms(message);
                total++;
            }
            cursor.close();
            runOnUiThread(() -> updateSmsList(refresh));
            Log.d(TAG, "总共 "
                    + total
                    + " 条短信，接收"
                    + (total - smsLists.get(INDEX_SENT).size())
                    + "条，发送"
                    + smsLists.get(INDEX_SENT).size()
                    + "条；已读"
                    + (total - smsLists.get(INDEX_UNREAD).size())
                    + "条，未读"
                    + smsLists.get(INDEX_UNREAD).size()
                    + "条，验证码共"
                    + smsLists.get(INDEX_CODE).size()
                    + "条");
        }
    }

    private void classifySms(SMSMessage sms) {
        if (sms.isReceived()) {
            if (sms.body.contains("验证码")) {
                Log.i(TAG, "message : " + sms.toString());
                smsLists.get(INDEX_CODE).add(sms);
            } else if (sms.body.contains("退订")) {
                Log.i(TAG, "message : " + sms.toString());
                smsLists.get(INDEX_SUB).add(sms);
            } else if (sms.address.startsWith("106")) {
                smsLists.get(INDEX_106).add(sms);
            } else {
                smsLists.get(INDEX_OTHERS).add(sms);
            }
        } else {
            smsLists.get(INDEX_SENT).add(sms);
        }
        if (sms.isUnread()) {
            smsLists.get(INDEX_UNREAD).add(sms);
        }
    }

    private void updateSmsList(boolean refresh) {
        int i = 0;
        for (RecyclerView rv : rvs) {
            if (rv != null) {
                rv.getAdapter().notifyDataSetChanged();
            }
            int size = smsLists.get(i).size();
            if (size > 0) {
                TabLayout.Tab tab = typeTab.getTabAt(i);
                if (tab != null) {
                    tab.setText(TITLE[i] + "(" + size + ")");
                }
            }
            i++;
        }
        if (refresh) {
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        if (requestCode == 0x1 && grantResults[0] == 0) {
            new Thread(this::queryMessage).start();
        }
    }

    @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int index = smsVp.getCurrentItem();
        if (listCheckState[index] == isChecked) {
            return;
        }
        listCheckState[index] = isChecked;
        List<SMSMessage> list = smsLists.get(index);
        if (list.isEmpty()) {
            return;
        }
        for (SMSMessage smsMessage : list) {
            smsMessage.checked = isChecked;
        }
        rvs[index].getAdapter().notifyDataSetChanged();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override public void onPageSelected(int position) {
        listCheckBox.setChecked(listCheckState[position]);
    }

    @Override public void onPageScrollStateChanged(int state) {
    }

    @Override public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
                int index = smsVp.getCurrentItem();
                List<SMSMessage> list = smsLists.get(index);
                deleteSms(list);
                rvs[index].getAdapter().notifyDataSetChanged();
                break;
        }
    }

    private void deleteSms(List<SMSMessage> list) {
        int preSize = list.size();
        for (int i = 0; i < list.size(); i++) {
            SMSMessage smsMessage = list.get(i);
            if (smsMessage.checked) {
                int rows = getContentResolver().delete(Uri.parse("content://sms/" + smsMessage.id),
                        "date=?", new String[] { smsMessage.date + "" });
                if (rows > 0) {
                    Log.d(TAG, "deleted msg is: " + smsMessage.body);
                    list.remove(i);
                    i--;
                } else {
                    Log.w(TAG, "delete sms failure");
                }
            }
        }
        int count = preSize - list.size();
        if (count > 0) {
            Toast.makeText(this, "成功删除" + count + "条", Toast.LENGTH_SHORT).show();
        }
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        Toast.makeText(this, "请开启SMS Cleaner的验证码自动识别服务", Toast.LENGTH_SHORT).show();
        return super.onOptionsItemSelected(item);
    }

    private class SmsPagerAdapter extends PagerAdapter {

        @Override public int getCount() {
            return NUM_TYPE;
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
