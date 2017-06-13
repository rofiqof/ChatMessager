package app.com.mychat;

import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallEndCause;
import com.sinch.android.rtc.video.VideoCallListener;
import com.squareup.picasso.Picasso;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import mehdi.sakout.fancybuttons.FancyButton;

public class IncomingCallScreenActivity extends BaseActivity {

    static final String TAG = IncomingCallScreenActivity.class.getSimpleName();
    private String mCallId;
    private AudioPlayer mAudioPlayer;
    PowerManager.WakeLock screenLock;
    ImageView imgWiat;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.incoming);

        context = getApplicationContext();

        screenLock = ((PowerManager)getSystemService(POWER_SERVICE)).newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "TAG");
        screenLock.acquire();

        imgWiat = (ImageView)findViewById(R.id.imgPic);

        FancyButton answer = (FancyButton) findViewById(R.id.answerButton);
        answer.setOnClickListener(mClickListener);
        FancyButton decline = (FancyButton) findViewById(R.id.declineButton);
        decline.setOnClickListener(mClickListener);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        mAudioPlayer = new AudioPlayer(this);
        mAudioPlayer.playRingtone();
        mCallId = getIntent().getStringExtra(MessageService.CALL_ID);
        String img = getIntent().getStringExtra("img_call");
        Picasso.with(context).load(img).placeholder(R.drawable.img_pic).transform(new CircleTransform()).into(imgWiat);
    }

    @Override
    protected void onServiceConnected()
    {
        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null)
        {
            call.addCallListener(new SinchCallListener());
            TextView remoteUser = (TextView) findViewById(R.id.remoteUser);
            remoteUser.setText(call.getRemoteUserId());

            try
            {
                String name = getContactName(getApplicationContext(), call.getRemoteUserId());
                remoteUser.setText(""+name);
            }
            catch (Exception e)
            {
                remoteUser.setText(call.getRemoteUserId());
            }
        }
        else
        {
            Log.e(TAG, "Started with invalid callId, aborting");
            finish();
        }
    }

    private void answerClicked()
    {
        mAudioPlayer.stopRingtone();
        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null)
        {
            call.answer();
            Intent intent = new Intent(this, CallScreenActivity.class);
            intent.putExtra(MessageService.CALL_ID, mCallId);
            startActivity(intent);
        }
        else
        {
            finish();
        }
    }

    private void declineClicked()
    {
        mAudioPlayer.stopRingtone();
        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null)
        {
            call.hangup();
        }
        finish();
    }

    private class SinchCallListener implements VideoCallListener
    {
        @Override
        public void onCallEnded(Call call)
        {
            CallEndCause cause = call.getDetails().getEndCause();
            Log.d(TAG, "Call ended, cause: " + cause.toString());
            mAudioPlayer.stopRingtone();
            finish();
        }

        @Override
        public void onCallEstablished(Call call)
        {
            Log.d(TAG, "Call established");
        }

        @Override
        public void onCallProgressing(Call call)
        {
            Log.d(TAG, "Call progressing");
        }

        @Override
        public void onShouldSendPushNotification(Call call, List<PushPair> pushPairs)
        {
            // Send a push through your push provider here, e.g. GCM
        }

        @Override
        public void onVideoTrackAdded(Call call)
        {
            // Display some kind of icon showing it's a video call
        }
    }

    private String getContactName(Context context, String phoneNumber)
    {
        ContentResolver cr = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = cr.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
        if (cursor == null)
        {
            return null;
        }
        String contactName = null;
        if(cursor.moveToFirst())
        {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
        }

        if(cursor != null && !cursor.isClosed())
        {
            cursor.close();
        }
        return contactName;
    }

    private OnClickListener mClickListener = new OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            switch (v.getId())
            {
                case R.id.answerButton:
                    answerClicked();
                    break;
                case R.id.declineButton:
                    declineClicked();
                    break;
            }
        }
    };
}
