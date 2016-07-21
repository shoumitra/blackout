package com.bitsplease.blackout_demo;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class FacebookService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_FOO = "com.bitsplease.blackout_demo.action.FOO";
    private static final String ACTION_SMS = "com.bitsplease.blackout_demo.action.SMS";
    private static final String ACTION_FACEBOOK = "com.bitsplease.blackout_demo.action.FACEBOOK";
    public static final String NOTIFICATION = "com.bitsplease.blackout_demo.service.receiver";
    public static final String NOTIFICATION_SMS = "com.bitsplease.blackout_demo.service.receiver_sms";


    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "com.bitsplease.blackout_demo.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "com.bitsplease.blackout_demo.extra.PARAM2";

    public FacebookService() {
        super("FacebookService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionFoo(Context context, String param1, String param2) {
        Intent intent = new Intent(context, FacebookService.class);
        intent.setAction(ACTION_FOO);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_FOO.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionFoo(param1, param2);
            } else if (ACTION_SMS.equals(action)) {
                final int hours = Integer.parseInt(intent.getStringExtra(EXTRA_PARAM1));
                handleActionSMS(hours);
            }
            else if (ACTION_FACEBOOK.equals(action)) {
                handleActionFacebook();
            }
        }
    }

    private void handleActionFacebook() {
        Bundle params = new Bundle();
        params.putString("limit",  "5");

        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/feed",
                params,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        response.getJSONArray();
                        publishResultsFacebook();
                    }
                }
        ).executeAsync();
    }

    private void handleActionSMS(int hours) {
        List<DisplayObject> smsList = new ArrayList<DisplayObject>();
        Uri message = Uri.parse("content://sms/inbox");
        ContentResolver cr = getContentResolver();
        long currentTime = System.currentTimeMillis();
        long range = TimeUnit.HOURS.toMillis(hours);

        Cursor c = cr.query(message, null, "date >= ?", new String[] { Long.toString(currentTime - range)}, null);
        int totalSMS = c.getCount();

        if (c.moveToFirst()) {
            for (int i = 0; i < totalSMS; i++) {
                DisplayObject sms = new DisplayObject("SMS",
                        c.getString(c.getColumnIndexOrThrow("date")),c.getString(c.getColumnIndexOrThrow("body")), R.drawable.sms);
                smsList.add(sms);
                c.moveToNext();
            }
        }
        c.close();
        publishResultsSMS(smsList);
    }

    private void publishResultsFacebook() {
        Intent intent = new Intent(NOTIFICATION);
        intent.putExtra("DisplayList","Something Something");
        sendBroadcast(intent);
    }

    private void publishResultsSMS(List<DisplayObject> smsInRange){
        Intent intent = new Intent(NOTIFICATION_SMS);
        intent.putExtra("DisplayList",(Serializable)smsInRange);
        sendBroadcast(intent);
        }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(String param1, String param2) {
        // TODO: Handle action Foo
        throw new UnsupportedOperationException("Not yet implemented");
    }
}