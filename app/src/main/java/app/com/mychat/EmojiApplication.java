package app.com.mychat;

import android.app.Application;
import android.content.Context;
import android.os.StrictMode;
import android.support.multidex.MultiDex;

public class EmojiApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG)
        {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().build());
        }

       // LeakCanary.install(this);

    }
    @Override
    protected void attachBaseContext(Context base)
    {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}