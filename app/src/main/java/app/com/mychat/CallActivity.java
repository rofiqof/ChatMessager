package app.com.mychat;

import android.content.ContentResolver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallClient;
import com.sinch.android.rtc.calling.CallListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


import mehdi.sakout.fancybuttons.FancyButton;

public class CallActivity extends BaseActivity implements HeadsetEvent.OnCustomStateListener
{
    TelephonyManager manager;
   // StatePhoneReceiver myPhoneStateListener;

    PowerManager.WakeLock screenLock;
    FancyButton btncall;
    SinchClient sinchClient;
    CallClient callClient;
    String id;
    Call call;
    final Context context = this;
    ImageView imgPic;
    TextView callerName,txtState;
    SharedPreferences sharedpreferences;
    AudioManager audioManager;
    boolean mute=false,speker=false;
    Button btnMute,btnSpeker,btnAdd;
    HeadsetPlugReceiver headsetPlugReceiver;
    boolean modelState;
    MediaPlayer mp;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);
        btncall = (FancyButton) findViewById(R.id.btnCall);
        imgPic = (ImageView)findViewById(R.id.imgCallImage);//
        callerName = (TextView)findViewById(R.id.txtCallerName);
        txtState = (TextView)findViewById(R.id.txtState);

        btnMute = (Button)findViewById(R.id.btnMute);
        btnSpeker = (Button)findViewById(R.id.btnLaudspeker);
        btnAdd = (Button)findViewById(R.id.btnAdd);

        audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);

        // audioController = getSinchServiceInterface().getAudioController();

        screenLock = ((PowerManager)getSystemService(POWER_SERVICE)).newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "TAG");
        screenLock.acquire();

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        txtState.setText("Connecting..");
        sharedpreferences = getSharedPreferences("pref", Context.MODE_PRIVATE);

        id = getIntent().getStringExtra("call_id");
        String img =getIntent().getStringExtra("call_img");
        String s = getIntent().getStringExtra("rec");

        Picasso.with(context).load(img).transform(new CircleTransform()).into(imgPic);

        sinchClient = ((MyWTF) this.getApplication()).getSomeVariable();
        btncall.setIconResource(R.drawable.img_endcall);

        String sh = (sharedpreferences.getString("myImage",""));
        System.out.println("the user img :" + sh);

        Map<String, String> headers = new HashMap<String, String>();
        headers.put("img_call", ""+sh);

        callClient = sinchClient.getCallClient();
        call = callClient.callUser(id,headers);
        headsetPlugReceiver = new HeadsetPlugReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.HEADSET_PLUG");
        registerReceiver(headsetPlugReceiver, intentFilter);
        HeadsetEvent.getInstance().setListener(this);
        final boolean modelState = HeadsetEvent.getInstance().getState();
        System.out.println("the Current state: " + String.valueOf(modelState));

        String name = getContactName(getApplicationContext(), call.getRemoteUserId());
        callerName.setText(""+name);

        try
        {
          //  Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);

            mp = MediaPlayer.create(getApplicationContext(), R.raw.beep_beep);
            mp.setLooping(true);

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

        callListner();


       /* btnAdd.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                System.out.println("the add click");
                call = callClient.callUser("9879767397");
            }
        });*/

        btncall.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (call == null)
                {
                    call = callClient.callUser(id);
                    btncall.setIconResource(R.drawable.img_endcall);
                    audioManager.setMode(AudioManager.MODE_IN_CALL);
                }
                else
                {
                    call.hangup();
                    call = null;
                    btncall.setIconResource(R.drawable.img_call);
                    audioManager.setMode(AudioManager.MODE_NORMAL);
                    audioManager.setSpeakerphoneOn(false);
                    finish();
                }
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

    private void callListner()
    {
        try
        {
            call.addCallListener(new CallListener()
            {
                @Override
                public void onCallProgressing(Call call)
                {
                    audioManager.setMode(AudioManager.MODE_RINGTONE);
                    System.out.println("the call ringing");
                    btncall.setIconResource(R.drawable.img_endcall);
                    txtState.setText("Ringing..");
                }
                @Override
                public void onCallEstablished(Call call)
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

                    System.out.println("the call connected " + modelState);
                    audioManager.setSpeakerphoneOn(false);
                    setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
                    btncall.setIconResource(R.drawable.img_endcall);
                    txtState.setText("Establish");
                }

                @Override
                public void onCallEnded(Call call)
                {
                    System.out.println("the call end");
                    audioManager.setMode(AudioManager.MODE_NORMAL);
                    btncall.setIconResource(R.drawable.img_call);
                    //onBackPressed();
                    finish();
                }

                @Override
                public void onShouldSendPushNotification(Call call, List<PushPair> list)
                {

                }
            });
        }
        catch (Exception e)
        {
            System.out.println("the exe :" + e);
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
    protected void onDestroy()
    {
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
        super.onDestroy();
    }

    @Override
    public void stateChanged()
    {
        modelState = HeadsetEvent.getInstance().getState();
        if(modelState)
        {
            System.out.println("the change state true ");
            audioManager.setSpeakerphoneOn(false);
        }
        else
        {
            System.out.println("the change state false ");
            audioManager.setSpeakerphoneOn(true);
        }
    }
}
