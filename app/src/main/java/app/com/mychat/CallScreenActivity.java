package app.com.mychat;

import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallClient;
import com.sinch.android.rtc.calling.CallEndCause;
import com.sinch.android.rtc.calling.CallState;
import com.sinch.android.rtc.video.VideoCallListener;
import com.sinch.android.rtc.video.VideoController;
import com.squareup.picasso.Picasso;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.Ringtone;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;



/*
{"posts":[{"fromid":"7405738334","message":"hii","image":"http:\/\/www.pretty-talk.com\/chat_messenger\/user\/7405738334\/images\/","message_id":"be2bb993-0636-44a2-804c-ca1045ad01e4","time":"22-Dec-2016 20:19:35","type":"image","toid":"8401470162"},{"fromid":"8401470162","message":"hello","image":"http:\/\/www.pretty-talk.com\/chat_messenger\/user\/7405738334\/images\/","message_id":"af4f2aeb-38e2-460a-bc7f-12a6e216ccac","time":"22-Dec-2016 20:20:15","type":"image","toid":"7405738334"}],"success":"1"}
*/





import mehdi.sakout.fancybuttons.FancyButton;

public class CallScreenActivity extends BaseActivity
{
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
    boolean mute=false;
    ImageButton btnMute;
    Call call;
    boolean add = false;
    LinearLayout wait;
   // ImageView imgWiat;
    Context context;
    String img;

    private class UpdateCallDurationTask extends TimerTask
    {
        @Override
        public void run()
        {
            CallScreenActivity.this.runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
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
        context = getApplicationContext();

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
        wait = (LinearLayout) findViewById(R.id.WaitVideo);
     //   imgWiat = (ImageView)findViewById(R.id.imgWait);
        btnMute = (ImageButton)findViewById(R.id.btnMute);

        mCallDuration.setVisibility(View.GONE);

        btnMute.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(mute)
                {
                    System.out.println("the mute false");
                    btnMute.setImageResource(R.drawable.no_micro_white);
                    mute=false;
                    mAudioPlayer.UnMute();
                }
                else
                {
                    System.out.println("the mute true");
                    btnMute.setImageResource(R.drawable.micro);
                    mute=true;
                    mAudioPlayer.Mute();
                }
            }
        });

        FancyButton endCallButton = (FancyButton) findViewById(R.id.hangupButton);
        endCallButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                endCall();
            }
        });

        mCallId = getIntent().getStringExtra(MessageService.CALL_ID);

       // img = getIntent().getStringExtra("img");
       // Picasso.with(context).load(img).placeholder(R.drawable.img_pic).transform(new CircleTransform()).into(imgWiat);

        if (savedInstanceState == null)
        {
            mCallStart = System.currentTimeMillis();
        }
    }

    @Override
    public void onServiceConnected()
    {
        call = getSinchServiceInterface().getCall(mCallId);
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
            Log.e(TAG, "the Started with invalid callId, aborting.");
            finish();
        }
        updateUI();
    }

    private void updateUI()
    {
        if (getSinchServiceInterface() == null)
        {
            return;
        }
        Call call = getSinchServiceInterface().getCall(""+mCallId);
        if (call != null)
        {
            mCallerName.setText(""+call.getRemoteUserId().toString());
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


           /* if (call.getState() == CallState.ESTABLISHED)
            {
                 wait.setVisibility(View.GONE);

                *//*if(add)
                {
                    System.out.println("the video added");
                }
                else
                {
                    System.out.println("the video establish");
                    add = true;
                    removeLocalView();
                    addVideoViews();
                }
                *//*

            }
            else
            {
                wait.setVisibility(View.VISIBLE);
                //mCallDuration.setVisibility(View.GONE);
                //addWaitVideoView();
            }*/
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
        call = null;
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
        //  mAudioPlayer.stopProgressTone();
        mAudioPlayer.stopRingtone();
        //call = getSinchServiceInterface().getCall(mCallId);
        if (call != null)
        {
            call.hangup();
            call = null;
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
            mCallDuration.setText(""+formatTimespan(System.currentTimeMillis() - mCallStart));
        }
    }

    private void addVideoViews()
    {
        mCallDuration.setVisibility(View.VISIBLE);

        if (mVideoViewsAdded || getSinchServiceInterface() == null)
        {
            return;
        }

        final VideoController vc = getSinchServiceInterface().getVideoController();
        try
        {
            if (vc != null)
            {
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
                LinearLayout view = (LinearLayout) findViewById(R.id.remoteVideo);
                view.addView(vc.getRemoteView());
                mVideoViewsAdded = true;
            }
        }
        catch (Exception e)
        {
            System.out.println("the exe is video add : " + e);
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
            //mAudioPlayer.stopProgressTone();

            mAudioPlayer.stopRingtone();
            setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
            String endMsg = "Call ended: " + call.getDetails().toString();
            System.out.println("the end call : " + endMsg);
            endCall();
        }

        @Override
        public void onCallEstablished(Call call)
        {
            System.out.println("the call establish");
              // mAudioPlayer.stopProgressTone();
            mAudioPlayer.stopRingtone();
            mAudioPlayer.AudouType(1);
            mCallState.setText(call.getState().toString());
            setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);

            mCallStart = System.currentTimeMillis();
        }
        @Override
        public void onCallProgressing(Call call)
        {
            System.out.println("the Video proccessing");
            addWaitVideoView();
            try
            {
                mAudioPlayer.playRingtone2();
                System.out.println("the Video proccessing play");
            }
            catch (Exception e)
            {
                System.out.println("the Video proccessing play exe " + e);
            }
            mAudioPlayer.AudouType(2);
        }
        @Override
        public void onShouldSendPushNotification(Call call, List<PushPair> pushPairs)
        {

        }
        @Override
        public void onVideoTrackAdded(Call call)
        {
            removeLocalView();
            addVideoViews();

            // wait.setVisibility(View.GONE);
             // addVideoViews();

             /*
             if(add)
             {
                System.out.println("the video added track");
             }
             else
             {
                 System.out.println("the video track");
                 add = true;
               //removeLocalView();
                 addVideoViews();
            }
            */
        }
    }

    private void removeLocalView()
    {
        if (getSinchServiceInterface() == null)
        {
            return;
        }
        try
        {
            VideoController vc = getSinchServiceInterface().getVideoController();
            if (vc != null)
            {
                LinearLayout view = (LinearLayout) findViewById(R.id.WaitVideo);
                view.removeView(vc.getLocalView());

              //RelativeLayout localView = (RelativeLayout) findViewById(R.id.localVideo);
              //localView.removeView(vc.getLocalView());

                mVideoViewsAdded = false;
            }
        }
        catch (Exception E)
        {
            System.out.println("the exe remove view :" + E);
        }
    }

    private void addWaitVideoView()
    {
        final VideoController vb  = getSinchServiceInterface().getVideoController();
        if (vb != null)
        {
            try
            {
                LinearLayout wait = (LinearLayout) findViewById(R.id.WaitVideo);
                if(wait.getParent()!=null)
                wait.addView(vb.getLocalView());
                wait.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        vb.toggleCaptureDevicePosition();
                    }
                });
            }
            catch (Exception e)
            {
                System.out.println("the exe is add wait:" + e);
            }
        }
    }
}
