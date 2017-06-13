package app.com.mychat;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.calling.Call;

public class MyWTF extends Application
{
    @Override
    public void onCreate()
    {
        super.onCreate();
       /* Parse.enableLocalDatastore(this);
        Parse.initialize(this, "hQzrQWCYP51TVT271bc2oREPwnhLf9HJGINOMc2e", "P6YlKUzO0ziaQ6iW3RBymE5bd1NWoKXOu2WAGeKg");
        ParseUser.enableAutomaticUser();
        ParseACL defaultACL = new ParseACL();
        // If you would like all objects to be private by default, remove this
        // line.
        defaultACL.setPublicReadAccess(true);
        ParseACL.setDefaultACL(defaultACL, true);
  */  }
    @Override
    protected void attachBaseContext(Context base)
    {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    SinchClient someVariable;
    Call call;

    boolean micro;

    public boolean isMicro()
    {
        return micro;
    }

    public void setMicro(boolean micro)
    {
        this.micro = micro;
    }

    public Call getCall()
    {
        return call;
    }

    public void setCall(Call call)
    {
        this.call = call;
    }

    public SinchClient getSomeVariable()
    {
        return someVariable;
    }

    public void setSomeVariable(SinchClient someVariable) {
        this.someVariable = someVariable;
    }
}
