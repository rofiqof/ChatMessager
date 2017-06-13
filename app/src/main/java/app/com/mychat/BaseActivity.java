package app.com.mychat;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

public abstract class BaseActivity extends Activity implements ServiceConnection {

    private MessageService.MessageServiceInterface mSinchServiceInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getApplicationContext().bindService(new Intent(this, MessageService.class), this,
                BIND_AUTO_CREATE);
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder)
    {
        if (MessageService.class.getName().equals(componentName.getClassName()))
        {
            mSinchServiceInterface = (MessageService.MessageServiceInterface) iBinder;
            onServiceConnected();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        if (MessageService.class.getName().equals(componentName.getClassName())) {
            mSinchServiceInterface = null;
            onServiceDisconnected();
        }
    }

    protected void onServiceConnected() {
        // for subclasses
    }

    protected void onServiceDisconnected() {
        // for subclasses
    }

    protected MessageService.MessageServiceInterface getSinchServiceInterface() {
        return mSinchServiceInterface;
    }

}
