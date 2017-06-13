package app.com.mychat;

import com.sinch.android.rtc.AudioController;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallEndCause;
import com.sinch.android.rtc.calling.CallState;
import com.sinch.android.rtc.video.VideoCallListener;
import com.sinch.android.rtc.video.VideoController;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import mehdi.sakout.fancybuttons.FancyButton;

public class SendingCallScreenActivity extends BaseActivity {

    static final String TAG = CallScreenActivity.class.getSimpleName();
    static final String CALL_START_TIME = "callStartTime";
    static final String ADDED_LISTENER = "addedListener";

    private AudioPlayer mAudioPlayer;
    private Timer mTimer;
    private UpdateCallDurationTask mDurationTask;

    private String mCallId;
    private long mCallStart = 0;
    private boolean mAddedListener = false;
    private boolean mVideoViewsAdded = false;

    private TextView mCallDuration;
    private TextView mCallState;
    private TextView mCallerName;
    PowerManager.WakeLock screenLock;
    LinearLayout wait;
    VideoController c;

    private class UpdateCallDurationTask extends TimerTask {

        @Override
        public void run()
        {
            SendingCallScreenActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateCallDuration();
                }
            });
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState)
    {
        savedInstanceState.putLong(CALL_START_TIME, mCallStart);
        savedInstanceState.putBoolean(ADDED_LISTENER, mAddedListener);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        mCallStart = savedInstanceState.getLong(CALL_START_TIME);
        mAddedListener = savedInstanceState.getBoolean(ADDED_LISTENER);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.callscreen);

        screenLock = ((PowerManager)getSystemService(POWER_SERVICE)).newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "TAG");
        screenLock.acquire();

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        mAudioPlayer = new AudioPlayer(this);
        mCallDuration = (TextView) findViewById(R.id.callDuration);
        mCallerName = (TextView) findViewById(R.id.remoteUser);
        mCallState = (TextView) findViewById(R.id.callState);
        FancyButton endCallButton = (FancyButton) findViewById(R.id.hangupButton);

        endCallButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                endCall();
            }
        });

        mCallId = getIntent().getStringExtra(MessageService.CALL_ID);
        if (savedInstanceState == null)
        {
            mCallStart = System.currentTimeMillis();
        }
    }

    @Override
    public void onServiceConnected()
    {
        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null)
        {
            if (!mAddedListener)
            {
                call.addCallListener(new SinchCallListener());
                mAddedListener = true;
            }
        }
        else
        {
            Log.e(TAG, "Started with invalid callId, aborting.");
            finish();
        }
        updateUI();
    }

    private void updateUI()
    {
        System.out.println("the update ui");

        if (getSinchServiceInterface() == null)
        {
            System.out.println("the update ui");
            return;
        }

        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null)
        {
            mCallerName.setText(call.getRemoteUserId());
            try
            {
                String name = getContactName(getApplicationContext(), call.getRemoteUserId());
                mCallerName.setText(""+name);
            }
            catch (Exception e)
            {
                mCallerName.setText(call.getRemoteUserId());
            }
            mCallState.setText(call.getState().toString());

            System.out.println("the call state :" + call.getState().toString());

            if (call.getState() == CallState.ESTABLISHED)
            {
                Intent intent = new Intent(getApplicationContext(), CallScreenActivity.class);
                intent.putExtra(MessageService.CALL_ID, mCallId);
                startActivity(intent);
            }
            addVideoViews();
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

    @Override
    public void onStop()
    {
        super.onStop();
        mDurationTask.cancel();
        mTimer.cancel();
        removeVideoViews();
    }

    @Override
    public void onStart()
    {
        super.onStart();
        mTimer = new Timer();
        mDurationTask = new UpdateCallDurationTask();
        mTimer.schedule(mDurationTask, 0, 500);
        updateUI();
    }

    @Override
    public void onBackPressed()
    {
    }

    private void endCall()
    {
        mAudioPlayer.stopProgressTone();
        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null)
        {
            call.hangup();
        }
        finish();
    }

    private String formatTimespan(long timespan)
    {
        long totalSeconds = timespan / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format(Locale.US, "%02d:%02d", minutes, seconds);
    }

    private void updateCallDuration()
    {
        if (mCallStart > 0)
        {
            mCallDuration.setText(formatTimespan(System.currentTimeMillis() - mCallStart));
        }
    }

    private void addVideoViews()
    {
       if (getSinchServiceInterface() == null)
        {
            return;
        }
        final VideoController vc = getSinchServiceInterface().getVideoController();
        if (vc != null)
        {
            /*
            RelativeLayout localView = (RelativeLayout) findViewById(R.id.localVideo);
            localView.addView(vc.getLocalView());
            localView.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    vc.toggleCaptureDevicePosition();
                }
            });
            */
            try
            {
                LinearLayout view = (LinearLayout) findViewById(R.id.remoteVideo);
                view.addView(vc.getLocalView());
                mVideoViewsAdded = true;
            }
            catch (Exception e)
            {
                System.out.println("the exe in add view");
            }
        }
    }

    private void removeVideoViews()
    {
        if (getSinchServiceInterface() == null)
        {
            return;
        }
        VideoController vc = getSinchServiceInterface().getVideoController();
        if (vc != null)
        {
            LinearLayout view = (LinearLayout) findViewById(R.id.remoteVideo);
            view.removeView(vc.getRemoteView());
            RelativeLayout localView = (RelativeLayout) findViewById(R.id.localVideo);
            localView.removeView(vc.getLocalView());
            mVideoViewsAdded = false;
        }
    }

    private class SinchCallListener implements VideoCallListener
    {
        @Override
        public void onCallEnded(Call call)
        {
           System.out.println("the Video end");
            CallEndCause cause = call.getDetails().getEndCause();
            mAudioPlayer.stopProgressTone();
            setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
            String endMsg = "Call ended: " + call.getDetails().toString();
            System.out.println("the end call :" + endMsg);
            endCall();

        }

        @Override
        public void onCallEstablished(Call call)
        {
            System.out.println("the call establish:");
            mCallState.setText(call.getState().toString());

           mAudioPlayer.stopProgressTone();

            setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
            AudioController audioController = getSinchServiceInterface().getAudioController();
            audioController.enableSpeaker();
       //
          mCallStart = System.currentTimeMillis();

              Log.d(TAG, "the Call offered video: " + call.getDetails().isVideoOffered());
        }

        @Override
        public void onCallProgressing(Call call)
        {
            System.out.println("the Video proccessing");
            mAudioPlayer.playProgressTone();
        }

        @Override
        public void onShouldSendPushNotification(Call call, List<PushPair> pushPairs)
        {
        }

        @Override
        public void onVideoTrackAdded(Call call)
        {

           // removeVideoViews();
            System.out.println("the Video track added");
            /*Intent intent = new Intent(getApplicationContext(), CallScreenActivity.class);
            intent.putExtra(MessageService.CALL_ID, mCallId);
            startActivity(intent);*/
            addVideoViews();
        }
    }
}
