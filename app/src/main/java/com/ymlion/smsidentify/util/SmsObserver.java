package com.ymlion.smsidentify.util;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.Telephony;
import android.util.Log;

import static com.ymlion.smsidentify.util.SmsUtil.copyCode;
import static com.ymlion.smsidentify.util.SmsUtil.findCode;

/**
 * Created by YMlion on 2017/9/29.
 */

public class SmsObserver extends ContentObserver {

    public static final String SMS_URI_INBOX = "content://sms/inbox";

    private Context context;

    /**
     * Creates a content observer.
     *
     * @param handler The handler to run {@link #onChange} on, or null if none.
     */
    public SmsObserver(Handler handler, Context context) {
        super(handler);
        this.context = context;
    }

    @Override public void onChange(boolean selfChange) {
        Cursor cursor = context.getContentResolver().query(Uri.parse(SMS_URI_INBOX), new String[] {
                Telephony.Sms._ID, Telephony.Sms.BODY, Telephony.Sms.ADDRESS, Telephony.Sms.DATE
        }, Telephony.Sms.READ + "=?", new String[] { "0" }, Telephony.Sms.DEFAULT_SORT_ORDER);
        if (cursor == null) {
            return;
        }
        if (cursor.moveToFirst()) {
            String smsBody = cursor.getString(cursor.getColumnIndex(Telephony.Sms.BODY));
            Log.d("TAG", "onChange: " + smsBody);
            copyCode(context, findCode(smsBody));
        }
    }
}
