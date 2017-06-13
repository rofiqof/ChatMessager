package app.com.mychat;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import com.sinch.android.rtc.AudioController;
import com.sinch.android.rtc.ClientRegistration;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.SinchClientListener;
import com.sinch.android.rtc.SinchError;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallClient;
import com.sinch.android.rtc.calling.CallClientListener;
import com.sinch.android.rtc.messaging.MessageClient;
import com.sinch.android.rtc.messaging.MessageClientListener;
import com.sinch.android.rtc.messaging.WritableMessage;
import com.sinch.android.rtc.video.VideoController;

import java.util.Map;

public class MessageService extends Service implements SinchClientListener
{
    private static final String APP_KEY = "d97c04f8-1e88-462c-99b2-253d3e0b02d3";
    private static final String APP_SECRET = "Wz3d+EanfUurmsSSxfzfDg==";
    private static final String ENVIRONMENT = "sandbox.sinch.com";
    public static final String CALL_ID = "CALL_ID";
    private final MessageServiceInterface serviceInterface = new MessageServiceInterface();
    private Intent broadcastIntent = new Intent("app.com.mychat.UserList");
    private LocalBroadcastManager broadcaster;
    private SinchClient sClient = null;
    private MessageClient messageClient = null;
    private String currentUserId;
    SharedPreferences sharedpreferences;
    private SinchServiceInterface mSinchServiceInterface = new SinchServiceInterface();
    private StartFailedListener mListener;
    private String mUserId;
    static final String TAG = MessageService.class.getSimpleName();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        System.out.println("the on Start command :" + APP_KEY);
        //get the current user id from Parse
        sharedpreferences = getSharedPreferences("pref", Context.MODE_PRIVATE);
        currentUserId = sharedpreferences.getString("mymobile","");
        broadcaster = LocalBroadcastManager.getInstance(this);

        if (currentUserId != null && !isSinchClientStarted())
        {
            startSinchClient(currentUserId);
        }
       // return super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }
    public void startSinchClient(String username)
    {
        sClient = Sinch.getSinchClientBuilder()
                .context(this)
                .userId(username)
                .applicationKey(APP_KEY)
                .applicationSecret(APP_SECRET)
                .environmentHost(ENVIRONMENT)
                .build();

        //this client listener requires that you define
        //a few methods below

        sClient.addSinchClientListener(this);
        sClient.setSupportMessaging(true);
        sClient.setSupportCalling(true);
        sClient.setSupportManagedPush(true);
        sClient.setSupportActiveConnectionInBackground(true);
        sClient.startListeningOnActiveConnection();
        sClient.getCallClient().addCallClientListener(new SinchCallClientListener());
        sClient.checkManifest();
        sClient.start();
    }
    private boolean isSinchClientStarted()
    {
        System.out.println("the on Sich start" );
        return sClient != null && sClient.isStarted();
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return serviceInterface;
    }

    @Override
   public void onClientStarted(SinchClient sinchClient)
    {
        System.out.println("the onClient start \n");
        sinchClient.startListeningOnActiveConnection();
        messageClient = sinchClient.getMessageClient();
        broadcastIntent.putExtra("success", true);
        broadcaster.sendBroadcast(broadcastIntent);
        ((MyWTF) this.getApplication()).setSomeVariable(sinchClient);
    }

    @Override
    public void onClientStopped(SinchClient sinchClient)
    {
        sClient=null;
    }

    @Override
    public void onClientFailed(SinchClient sinchClient, SinchError sinchError)
    {
        System.out.println("the onClient Faild");
        broadcastIntent.putExtra("success", false);
        broadcaster.sendBroadcast(broadcastIntent);
        sClient = null;
    }

    @Override
    public void onRegistrationCredentialsRequired(SinchClient sinchClient, ClientRegistration clientRegistration)
    {
    }
    @Override
    public void onLogMessage(int i, String s, String s2)
    {
    }
    public void sendMessage(String recipientUserId, String types)
    {
        if (messageClient != null)
        {
            WritableMessage message = new WritableMessage(recipientUserId, types);
            messageClient.send(message);
        }
        else
        {
           System.out.println("the Message Client null");
        }
    }
    public void addMessageClientListener(MessageClientListener listener)
    {
        if (messageClient != null)
        {
            messageClient.addMessageClientListener(listener);
        }
    }
    public void removeMessageClientListener(MessageClientListener listener)
    {
        if (messageClient != null)
        {
            messageClient.removeMessageClientListener(listener);
        }
    }
    @Override
    public void onDestroy()
    {
        //sClient.stopListeningOnActiveConnection();
        //sClient.terminate();
        System.out.println("the destroy service is called");
    }
    //public interface for ListUsersActivity & MessagingActivity
    public class MessageServiceInterface extends Binder
    {
        public void sendMessage(String recipientUserId, String types)
        {
              MessageService.this.sendMessage(recipientUserId, types);
              types="";
              recipientUserId="";
        }
        public void addMessageClientListener(MessageClientListener listener)
        {
            MessageService.this.addMessageClientListener(listener);
        }
        public void removeMessageClientListener(MessageClientListener listener)
        {
            MessageService.this.removeMessageClientListener(listener);
        }
        public boolean isSinchClientStarted()
        {
            return MessageService.this.isSinchClientStarted();
        }

        public Call callUserVideo(String userId, Map<String, String> var2)//String var1, Map<String, String> var2
        {
            return sClient.getCallClient().callUserVideo(userId,var2);
        }

        public String getUserName()
        {
            return mUserId;
        }

        public boolean isStarted()
        {
            return MessageService.this.isSinchClientStarted();
        }

        public void startClient(String userName) {
            startSinchClient(userName);
        }

        public void stopClient() {
            stop();
        }

        public void setStartListener(StartFailedListener listener) {
            mListener = listener;
        }

        public Call getCall(String callId)
        {
            return sClient.getCallClient().getCall(callId);
        }

        public Call callUser(String userId, Map<String, String> headers)
        {
            return sClient.getCallClient().callUser(userId, headers);
        }

        public VideoController getVideoController()
        {
            if (!isStarted())
            {
                return null;
            }
            return sClient.getVideoController();
        }

        public AudioController getAudioController()
        {
            if (!isStarted())
            {
                return null;
            }
            return sClient.getAudioController();
        }
    }

    private class SinchCallClientListener implements CallClientListener
    {
        @Override
        public void onIncomingCall(CallClient callClient, Call call)
        {
            if(call.getDetails().isVideoOffered())
            {
                //System.out.println("the user image :" + call.getHeaders().get("img_call"));

                Intent intent = new Intent(MessageService.this, IncomingCallScreenActivity.class);
                intent.putExtra(CALL_ID, call.getCallId());
                intent.putExtra("img_call",call.getHeaders().get("img_call"));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                MessageService.this.startActivity(intent);
            }
            else
            {
                ((MyWTF) getApplication()).setCall(call);
                Intent i = new Intent(getApplicationContext(), RecieveActivity.class);
                i.putExtra("img_call",call.getHeaders().get("img_call"));
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        }
    }

    private void stop()
    {
        if (sClient != null)
        {
            System.out.println("the stop service is called");
           // sClient.terminate();
           // sClient = null;
        }
    }

   public  class SinchServiceInterface extends Binder
    {
       /* public Call callUserVideo(String userId)
        {
            return sClient.getCallClient().callUserVideo(userId);
        }

        public String getUserName()
        {
            return mUserId;
        }

        public boolean isStarted()
        {
            return MessageService.this.isSinchClientStarted();
        }

        public void startClient(String userName) {
            startSinchClient(userName);
        }

        public void stopClient() {
            stop();
        }

        public void setStartListener(StartFailedListener listener) {
            mListener = listener;
        }

        public Call getCall(String callId)
        {
            return sClient.getCallClient().getCall(callId);
        }

        public VideoController getVideoController()
        {
            if (!isStarted())
            {
                return null;
            }
            return sClient.getVideoController();
        }

        public AudioController getAudioController()
        {
            if (!isStarted())
            {
                return null;
            }
            return sClient.getAudioController();
        }
*/

    }
    public interface StartFailedListener
    {
        void onStartFailed(SinchError error);
        void onStarted();
    }
}

