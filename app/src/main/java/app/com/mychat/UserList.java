package app.com.mychat;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.ContactsContract;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class UserList extends Activity
{
    ListView list;
    CustomAdapter adapter;
    ArrayList<User> users;
    BroadcastReceiver receiver;
    String uid;
    SharedPreferences sharedpreferences;
    String  contactName;
    private Activity activity;
    Context context;
    ConnectionDetector con;
    private ProgressDialog progress;
    AsyncTask<Void, Void, Void> SendData;
    JSONObject response_json;
    ImageView btnSetting;
    private Pubnub pubnub;
    private String username;
    private String stdByChannel;
    private SharedPreferences mSharedPreferences;

    private static final String PUBLISH_KEY = "pub-c-60ab2436-b4ca-4921-bb3a-1b94cbd7b2ff";
    private static final String P_SECRET_KEY = "sec-c-NGVlYWRhODEtM2UyNS00YmZiLWFmMDctMzBmZWQyM2Q5YTNi";
    private static final String SUBSCRIBE_KEY = "sub-c-f917bc56-8154-11e6-8409-0619f8945a4f";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.userlist);
        list = (ListView) findViewById(R.id.listUser);
        btnSetting = (ImageView)findViewById(R.id.btnSetting);
        context = getApplicationContext();
        activity = this;
        sharedpreferences = getSharedPreferences("pref", Context.MODE_PRIVATE);
        this.mSharedPreferences = getSharedPreferences(Constants.SHARED_PREFS, MODE_PRIVATE);

        setListChat();

        File imgDir = new File(Environment.getExternalStorageDirectory().getPath()+"/Chat Messanger/Images");
        if (!imgDir.exists())
        {
            imgDir.mkdirs();
            sharedpreferences.edit().putString("ImageDir",imgDir.toString()).commit();
            File imgFolder = new File(Environment.getExternalStorageDirectory().getPath()+"/Chat Messanger/Images/Send");
            if(!imgFolder.exists())
            {
                imgFolder.mkdirs();
                String sendImgDir = imgFolder.toString();
                System.out.println("the  send image directory is :" + sendImgDir);
                sharedpreferences.edit().putString("SendImageDir",sendImgDir).commit();
            }
            else
            {

            }
        }
        else
        {
            System.out.println("the directory exist");
        }

        uid = sharedpreferences.getString("mymobile","").toString();

        this.username     = this.mSharedPreferences.getString(Constants.USER_NAME, "");
        this.stdByChannel = this.username + Constants.STDBY_SUFFIX;

            receiver = new BroadcastReceiver()
            {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                 Boolean success = intent.getBooleanExtra("success", false);
                if (!success)
                {
                    System.out.println("the Message service not started");
                }
            }
        };

        initPubNub();

        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter("app.com.mychat.UserList"));
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        callUserList();

        btnSetting.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent i = new Intent(UserList.this,ProfileActivity.class);
                startActivity(i);
            }
        });

       /* search.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3)
            {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3)
            {

            }
            @Override
            public void afterTextChanged(Editable editable)
            {
                String text = search.getText().toString().toLowerCase(Locale.getDefault());
                UserList.this.adapter.filter(text);
            }
        });*/
    }

    private void subscribeStdBy()
    {
        try {
            pubnub.subscribe("calling_channel", new Callback()
            {
                @Override
                public void successCallback(String channel, Object message)
                {
                    System.out.println("the message pubnub :" + message.toString());
                    if (!(message instanceof JSONObject)) return; // Ignore if not JSONObject
                    JSONObject jsonMsg = (JSONObject) message;
                    try
                    {
                        if (!jsonMsg.has(Constants.JSON_CALL_USER)) return;     //Ignore Signaling messages.
                        String user = jsonMsg.getString(Constants.JSON_CALL_USER);
                        // dispatchIncomingCall(user);
                    }
                    catch (JSONException e)
                    {
                        System.out.println("the message pubnub exe:" +e);
                    }
                }

                @Override
                public void connectCallback(String channel, Object message)
                {
                     System.out.println("the message pubnub connected:" +message.toString());
                     setUserStatus(Constants.STATUS_AVAILABLE);
                }

                @Override
                public void errorCallback(String channel, PubnubError error)
                {
                    System.out.println("the message pubnub erroe:" + error.toString());
                }
            });
        }
        catch (PubnubException e)
        {
             System.out.println("the message pubnub here:" + e);
        }
    }

    private void callUserList()
    {
        SendData = new AsyncTask<Void, Void, Void>()
        {
            @Override
            protected Void doInBackground(Void... voids)
            {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
                String serverUrl = StaticService.all_user;
                HttpClient client = new DefaultHttpClient();
                HttpPost post = new HttpPost(serverUrl);
                try
                {
                    /*

                    JSONObject jo = new JSONObject();
                    jo.put("phone",mobile);
                    System.out.println("the json object tag :" + jo);
                    StringEntity se = new StringEntity(jo.toString());
                    se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,"application/json"));
                    post.setEntity(se);

                    */

                    HttpResponse response = client.execute(post);
                    HttpEntity entity = response.getEntity();
                    String responseText = EntityUtils.toString(entity);
                    response_json = new JSONObject(responseText);

                }
                catch (Exception e)
                {
                     System.out.println("the exception in technique : " + e);
                }
                return null;
            }

            @Override
            protected void onPreExecute()
            {
                super.onPreExecute();
                con = new ConnectionDetector(getApplicationContext());
                if (con.isConnectingToInternet())
                {
                    /*
                    progress = new ProgressDialog(UserList.this);
                    progress.setMessage("Please wait...");
                    progress.setCanceledOnTouchOutside(false);
                    progress.show();
                    */
                }
                else
                {
                    SendData.cancel(true);
                    //setMessage("No internet connection","Please connect to internet. try again");
                }
            }

            @Override
            protected void onPostExecute(Void aVoid)
            {
                super.onPostExecute(aVoid);
                try
                {
                    if(response_json.getString("success").equals("1"))
                    {
                        File f = new File(context.getCacheDir(),"");
                        String p = f.toString()+File.separator +"cacheFileUser.srl";
                        System.out.println("the path is :" + p);

                        File file = new File(p);
                        boolean deleted = file.delete();

                        System.out.println("the deleted :" + deleted);

                        try
                        {
                            ObjectOutput out = new ObjectOutputStream(new FileOutputStream(new File(context.getCacheDir(), "") + File.separator +"cacheFileUser.srl"));
                            out.writeObject(response_json.toString());
                            out.close();
                        }
                        catch (Exception e)
                        {
                            System.out.println("the exe is :" + e);
                        }
                        setListChat();
                    }
                    else if(response_json.getString("success").equals("0"))
                    {

                    }
                    else
                    {
                       // callFunction("Something going wrong on server");
                    }
                }
                catch (Exception e)
                {
                    System.out.println("the technique exception is  "+e);
                }
            }
        };
        SendData.execute();
    }

    private void setListChat()
    {
        JSONObject myjson=null;
        try
        {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream
                    (new File(context.getCacheDir() + File.separator +"cacheFileUser.srl")));
            myjson = new JSONObject((String) in.readObject());

            System.out.println("the chat list inside method" + myjson);
            in.close();
        }
        catch (Exception e)
        {
            System.out.println("the exe :" + e);
        }
        try
        {
            users = new ArrayList<>();
            JSONArray j = myjson.getJSONArray("posts");
            for (int i = 0; i < j.length(); i++)
            {
                JSONObject jo = j.getJSONObject(i);
                try
                {
                    User use = new User();
                    String contact = jo.getString("Phone");
                    use.setUimage(jo.getString("Profile_img"));
                    use.setUid(jo.getString("Phone"));
                    use.setEmail(jo.getString("id"));

                    boolean exist = contactExists(getApplicationContext(), contact);
                    if (exist)
                    {
                        if (!contact.equals(uid))
                        {
                            use.setUname(contactName);
                            users.add(use);
                        } else
                            System.out.println("the contact " + contact + " my contact");
                    }
                    else
                    {
                        //System.out.println("the contact "+ contact + " not exist");
                    }
                }
                catch (Exception el)
                {
                    System.out.print("the Error" + el);
                }
            }
        }
        catch (Exception e)
        {
            System.out.print("the Error" +e);
        }
        try
        {
            if (users.size() > 0)
            {
                adapter = new CustomAdapter(UserList.this, users);
                list.setAdapter(adapter);
                //list.setTextFilterEnabled(true);
            }
            else
            {
                System.out.println("the no user found in this app");
            }
        }
        catch (Exception e)
        {
            System.out.println("the no user found in this app");
        }
    }

    public boolean contactExists(Context context, String number)
    {
        Uri lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        String[] mPhoneNumberProjection = { ContactsContract.PhoneLookup._ID, ContactsContract.PhoneLookup.NUMBER, ContactsContract.PhoneLookup.DISPLAY_NAME };
        Cursor cur = context.getContentResolver().query(lookupUri,mPhoneNumberProjection, null, null, null);
        try
        {
            if (cur.moveToFirst())
            {
                contactName = cur.getString(cur.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                return true;
            }
        }
        finally
        {
            if (cur != null)
                cur.close();
        }
        return false;
    }

   /* protected void onResume()
    {
        super.onResume();
        try
        {
            pubnub.subscribe("calling_channel", new Callback()
            {

            });
        }
        catch (PubnubException e)
        {
            Log.d("PubnubException",e.toString());
        }
    }*/


    @Override
    public void onPause()
    {
        super.onPause();
        pubnub.unsubscribe("calling_channel");
    }

    /*
    @Override
    protected void onStop()
     {
        super.onStop();
        pubnub.unsubscribe("calling_channel");
    }
    */

    @Override
    protected void onStop()
    {
        super.onStop();
        if(pubnub!=null)
        {
            pubnub.unsubscribeAll();
        }

    }

    @Override
    protected void onRestart()
    {
        super.onRestart();
        if(pubnub==null)
        {
            initPubNub();
        }
        else
        {
            subscribeStdBy();
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if(pubnub==null)
        {
            initPubNub();
        }
        else
        {
            subscribeStdBy();
        }
    }

    private void initPubNub()
    {
        pubnub = new Pubnub(PUBLISH_KEY,SUBSCRIBE_KEY);
        pubnub.setUUID(uid);
        subscribeStdBy();
    }

   /* public void dispatchCall(final String callNum)
   {
        final String callNumStdBy = callNum + Constants.STDBY_SUFFIX;
        pubnub.hereNow(callNumStdBy, new Callback() {
            @Override
            public void successCallback(String channel, Object message) {
                Log.d("MA-dC", "HERE_NOW: " +" CH - " + callNumStdBy + " " + message.toString());
                try {
                    int occupancy = ((JSONObject) message).getInt(Constants.JSON_OCCUPANCY);
                    if (occupancy == 0)
                    {
                        //showToast("User is not online!");
                        return;
                    }
                    JSONObject jsonCall = new JSONObject();
                    jsonCall.put(Constants.JSON_CALL_USER, username);
                    jsonCall.put(Constants.JSON_CALL_TIME, System.currentTimeMillis());
                    pubnub.publish(callNumStdBy, jsonCall, new Callback() {
                        @Override
                        public void successCallback(String channel, Object message)
                        {
                            Log.d("MA-dC", "SUCCESS: " + message.toString());
                            Intent intent = new Intent(MainActivityUsethis, VideoChatActivity.class);
                            intent.putExtra(Constants.USER_NAME, username);
                            intent.putExtra(Constants.CALL_USER, callNum);  // Only accept from this number?
                            startActivity(intent);
                        }
                    });
                }
                 catch (JSONException e)
                 {
                    e.printStackTrace();
                }
            }
        });
    }
*/
   /* private void dispatchIncomingCall(String userId)
    {
        showToast("Call from: " + userId);
        Intent intent = new Intent(UserList.this, IncomingCallActivity.class);
        intent.putExtra(Constants.USER_NAME, username);
        intent.putExtra(Constants.CALL_USER, userId);
        startActivity(intent);
    }*/

    private void getUserStatus(String userId)
    {
        String stdByUser = userId + Constants.STDBY_SUFFIX;
        pubnub.getState(stdByUser, userId, new Callback()
        {
            @Override
            public void successCallback(String channel, Object message)
            {
                System.out.println("the message pubnub user status:" + message.toString());
            }
        });
    }

    private void showToast(final String message)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                Toast.makeText(UserList.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setUserStatus(String status)
    {
        try
        {
            JSONObject state = new JSONObject();
            state.put(Constants.JSON_STATUS, status);
            pubnub.setState(this.stdByChannel, this.username, state, new Callback() {
                @Override
                public void successCallback(String channel, Object message)
                {
                    Log.d("MA-sUS","State Set: " + message.toString());
                    System.out.println("the state set :" + message.toString());
                }
            });
        }
        catch (JSONException e)
        {
            System.out.println("the state set exe :" + e);
        }
    }
}