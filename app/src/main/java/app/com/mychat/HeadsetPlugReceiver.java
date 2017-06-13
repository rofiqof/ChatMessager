package app.com.mychat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class HeadsetPlugReceiver extends BroadcastReceiver
{
    boolean connectedMicrophone;
    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (!intent.getAction().equals(Intent.ACTION_HEADSET_PLUG))
        {
            return;
        }

        boolean connectedHeadphones = (intent.getIntExtra("state", 0) == 1);
        connectedMicrophone = (intent.getIntExtra("microphone", 0) == 1) && connectedHeadphones;
        String headsetName = intent.getStringExtra("name");
        System.out.println("the headset plug :" + headsetName + " Microphone :" + connectedMicrophone);
        HeadsetEvent.getInstance().changeState(connectedMicrophone);
    }
}
