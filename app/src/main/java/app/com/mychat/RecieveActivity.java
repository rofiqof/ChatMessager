package app.com.mychat;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sinch.android.rtc.AudioController;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallClient;
import com.sinch.android.rtc.calling.CallListener;
import com.sinch.android.rtc.video.VideoController;
import com.sinch.android.rtc.video.VideoScalingType;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.concurrent.ExecutionException;

import mehdi.sakout.fancybuttons.FancyButton;

public class RecieveActivity extends BaseActivity implements HeadsetEvent.OnCustomStateListener
{
    FancyButton btncall,btnEnd,btnEnd2;
    SinchClient sinchClient;
    CallClient callClient;
    String id;
    Call call;
    MediaPlayer mp;
    PowerManager.WakeLock screenLock;
    Context context;
    RelativeLayout rrOne,rrTwo;
    TextView callerName,txtState;
    ImageView imgcall,imgcall2;
    AudioManager audioManager;

   // boolean mute=false,speker=false;

    Button btnMute,btnSpeker,btnAdd;

    HeadsetPlugReceiver headsetPlugReceiver;
    boolean modelState;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recieve_call);

        btncall = (FancyButton) findViewById(R.id.btnCall);
        btnEnd = (FancyButton) findViewById(R.id.btnend);
        btnEnd2 = (FancyButton) findViewById(R.id.btnCallend2);
        callerName = (TextView)findViewById(R.id.txtCallerName);
        txtState = (TextView)findViewById(R.id.txtState);
        rrOne = (RelativeLayout)findViewById(R.id.rrOne);
        rrTwo = (RelativeLayout)findViewById(R.id.rrTwo);

        btnMute = (Button)findViewById(R.id.btnMute);
        btnSpeker = (Button)findViewById(R.id.btnLaudspeker);
     //   btnAdd = (Button)findViewById(R.id.btnAdd);

        imgcall = (ImageView)findViewById(R.id.imgCallImage);
        imgcall2 = (ImageView)findViewById(R.id.imgCallImage2);

        context = this.getApplicationContext();

        audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);

        btncall.setIconResource(R.drawable.img_call);

        screenLock = ((PowerManager)getSystemService(POWER_SERVICE)).newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "TAG");
        screenLock.acquire();

        headsetPlugReceiver = new HeadsetPlugReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.HEADSET_PLUG");
        registerReceiver(headsetPlugReceiver, intentFilter);
        HeadsetEvent.getInstance().setListener(this);
        final boolean modelState = HeadsetEvent.getInstance().getState();
        System.out.println("the Current state: " + String.valueOf(modelState));

        try
        {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            mp = MediaPlayer.create(getApplicationContext(), notification);

            /*
            mp.reset();
            mp.prepare();
            */

            mp.start();
        }
        catch (Exception e)
        {
            System.out.println("the exe in call :" + e);
        }
        sinchClient = ((MyWTF) this.getApplication()).getSomeVariable();
        callClient = sinchClient.getCallClient();
        call = ((MyWTF)getApplication()).getCall();


        /*btnAdd.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                System.out.println("the add click");
                call = callClient.callUser("9879767397");

            }
        });*/

        System.out.println("the incomming call "+ call.getRemoteUserId());

        txtState.setText("Incoming call..");

        String name = getContactName(getApplicationContext(), call.getRemoteUserId());
        callerName.setText(""+name);

        rrTwo.setVisibility(View.GONE);

        String im = getIntent().getStringExtra("img_call");
        System.out.println("the user image :" + im);

        Picasso.with(context).load(im).transform(new CircleTransform()).placeholder(R.drawable.img_pic).into(imgcall);
        Picasso.with(context).load(im).transform(new CircleTransform()).placeholder(R.drawable.img_pic).into(imgcall2);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        call.addCallListener(new SinchCallListener());

        btncall.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                try
                {
                    if (mp.isPlaying())
                    {
                        mp.stop();
                    }
                }
                catch (Exception e)
                {
                    System.out.println("the exe in ringing");
                }
                try
                {
                    call.answer();
                    /*
                    try
                    {
                        Thread.sleep(500);
                    }
                    catch (InterruptedException e)
                    {

                    }
                    */
                   // audioManager.setMode(AudioManager.MODE_IN_CALL);
                   // audioManager.setSpeakerphoneOn(false);

                    rrOne.setVisibility(View.GONE);
                    rrTwo.setVisibility(View.VISIBLE);
                }
                catch (Exception e)
                {
                    System.out.println("the exe in ringing" + e);
                }
            }
        });

        btnEnd.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                callEnd();
            }
        });

        btnEnd2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                callEnd();
            }
        });

        btnMute.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(audioManager.isMicrophoneMute())
                {
                    System.out.println("the mute true");
                    audioManager.setMicrophoneMute(false);
                    audioManager.setMode(AudioManager.ADJUST_UNMUTE);
                    btnMute.setCompoundDrawablesWithIntrinsicBounds(0,R.drawable.no_micro_white,0,0);
                }
                else
                {
                    System.out.println("the mute false");
                    audioManager.setMicrophoneMute(true);
                    audioManager.setMode(AudioManager.ADJUST_MUTE);
                    btnMute.setCompoundDrawablesWithIntrinsicBounds(0,R.drawable.micro,0,0);
                }
            }
        });

        btnSpeker.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (audioManager.isSpeakerphoneOn())
                {
                    System.out.println("the speaker false");
                    btnSpeker.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.yes_audio_white, 0, 0);
                    try
                    {
                        Thread.sleep(500);
                    }
                    catch (InterruptedException e)
                    {

                    }
                    AudioManager audioManager = (AudioManager)
                            getSystemService(Context.AUDIO_SERVICE);
                    audioManager.setMode(AudioManager.MODE_IN_CALL);
                    audioManager.setSpeakerphoneOn(false);
                }
                else
                {
                    System.out.println("the speaker true");

                    btnSpeker.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.yes_audio, 0, 0);
                    try
                    {
                        Thread.sleep(500); // Delay 0,5 seconds to handle better turning on
                    }
                    catch (InterruptedException e)
                    {

                    }
                    AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
                    audioManager.setMode(AudioManager.MODE_IN_CALL);
                    audioManager.setSpeakerphoneOn(true);
                }
                if(audioManager.isWiredHeadsetOn())
                {
                    if (audioManager.isSpeakerphoneOn())
                    {
                        audioManager.setWiredHeadsetOn(false);
                    }
                    else
                    {
                        audioManager.setWiredHeadsetOn(true);
                    }
                }
                else
                {
                    System.out.println("the not handset available");
                }
            }
        });
    }

    private void callEnd()
    {
        try
        {
            if (mp.isPlaying())
            {
                mp.stop();
            }
        }
        catch (Exception e)
        {
            System.out.println("the exe in ringing");
        }
        call.hangup();
        finish();
    }

    private class SinchCallListener implements CallListener
    {
        @Override
        public void onCallProgressing(Call call)
        {
            System.out.println("the call ringing");
            audioManager.setMode(audioManager.MODE_RINGTONE);
        }
        @Override
        public void onCallEstablished(Call call)
        {
            // audioManager.setSpeakerphoneOn(false);
            txtState.setText("Establish");
            audioManager.setSpeakerphoneOn(false);
            setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);

            /*
            setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
            AudioController audioController = getSinchServiceInterface().getAudioController();
            audioController.disableSpeaker();
            */

            try
            {
                if (mp.isPlaying())
                {
                    mp.stop();
                }
                else
                {

                }
            }
            catch (Exception e)
            {
                System.out.println("the exe in call conne");
            }
        }

        @Override
        public void onCallEnded(Call call)
        {
            setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
            audioManager.setMode(AudioManager.MODE_NORMAL);
            finish();
        }
        @Override
        public void onShouldSendPushNotification(Call call, List<PushPair> list)
        {

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
    protected void onDestroy()
    {
        super.onDestroy();
        audioManager.setMode(AudioManager.MODE_NORMAL);
        if (headsetPlugReceiver != null)
        {
            unregisterReceiver(headsetPlugReceiver);
            headsetPlugReceiver = null;
        }
        try
        {
            if (mp.isPlaying())
            {
                mp.stop();
            }
        }
        catch (Exception e)
        {
            System.out.println("the exe in ringing");
        }
    }

    public void stateChanged()
    {
        modelState = HeadsetEvent.getInstance().getState();
        //audioManager.setMode(AudioManager.MODE_IN_CALL);
        if(modelState)
        {
            System.out.println("the change state true ");
            //audioManager.setSpeakerphoneOn(false);
        }
        else
        {
            System.out.println("the change state false");
           // audioManager.setSpeakerphoneOn(true);
        }
    }
}