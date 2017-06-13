package app.com.mychat;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

public class GCMRegistrationIntentService extends IntentService
{
    //Constants for success and errors

    SharedPreferences sharedpreferences;

    public static final String REGISTRATION_SUCCESS = "RegistrationSuccess";
    public static final String REGISTRATION_ERROR = "RegistrationError";

    //Class constructor

    public GCMRegistrationIntentService()
    {
        super("");
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        //Registering gcm to the device
        registerGCM();
    }

    private void registerGCM()
    {
        //Registration complete intent initially null
        Intent registrationComplete = null;
        //Register token is also null
        //we will get the token on successfull registration
        String token = null;
        try
        {
            //Creating an instanceid
            sharedpreferences = getSharedPreferences("pref", Context.MODE_PRIVATE);

            InstanceID instanceID = InstanceID.getInstance(getApplicationContext());
            //Getting the token from the instance id
            token = instanceID.getToken(getString(R.string.gcm_projectnumber), GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            //Displaying the token in the log so that we can copy it to send push notification
            //You can also extend the app by storing the token in to your server
            Log.w("the GCMRegIntentService", "token:" + token);
            System.out.println("the inside token GCM :" + token);

            sharedpreferences.edit().putString("tk",token).commit();
            //on registration complete creating intent with success
            registrationComplete = new Intent(REGISTRATION_SUCCESS);
            //Putting the token to the intent
            registrationComplete.putExtra("token", token);
        }
        catch (Exception e)
        {
            Log.w("the GCMRegIntentService", "Registration error");
            System.out.println("the inside token GCM Err : "+ e);
            registrationComplete = new Intent(REGISTRATION_ERROR);
        }
        //Sending the broadcast that registration is completed
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }
}
