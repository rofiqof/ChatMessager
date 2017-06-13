package app.com.mychat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;

import mehdi.sakout.fancybuttons.FancyButton;

public class FirstActivity extends AppCompatActivity
{
    EditText edtMobile,txtOtp;
    ProgressDialog progressDialog;
    String strUserName;
    SharedPreferences sharedpreferences;
    String otpPass="",newUser="";
    ConnectionDetector con;
    private ProgressDialog progress;
    AsyncTask<Void, Void, Void> SendData;
    JSONObject response_json;
    FancyButton btnGetOtp,gonext ;
    String token;

    private static final int PERMISSION_REQUEST_CODE_CONTACT = 1;
    private static final int PERMISSION_REQUEST_CODE_PHONE = 2;
    private static final int PERMISSION_REQUEST_CODE_CAMERA = 3;
    private static final int PERMISSION_REQUEST_CODE_GALLERY = 4;
    private static final int PERMISSION_REQUEST_CODE_AUDIO = 5;
    private static final int PERMISSION_REQUEST_CODE_WAKE = 6;

    private Activity activity;
    Context context;
    BroadcastReceiver mRegistrationBroadcastReceiver;

   /*
    Intent i = new Intent(FirstActivity.this,UserList.class);
    final Intent serviceIntent = new Intent(getApplicationContext(), MessageService.class);
    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    startService(serviceIntent);
    startActivity(i);
    System.out.println("the user old");
    */

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.first_activity);

        sharedpreferences = getSharedPreferences("pref", Context.MODE_PRIVATE);
        if(sharedpreferences.getString("mymobile","").toString().equals(""))
        {
            getToken();
        }
        else
        {
           // con = new ConnectionDetector(FirstActivity.this);
            //if(con.isConnectingToInternet())
            {
                Intent intent = new Intent(getApplicationContext(), UserList.class);
                final Intent serviceIntent = new Intent(getApplicationContext(), MessageService.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startService(serviceIntent);
                startActivity(intent);
            }
           // else
             //   callFunction("No internet connection");
        }

        context = getApplicationContext();
        activity = this;

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        StrictMode.ThreadPolicy p = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(p);

        edtMobile = (EditText)findViewById(R.id.edtMobile);
        gonext = (FancyButton)findViewById(R.id.btnNextFirst);
        btnGetOtp = (FancyButton)findViewById(R.id.btnOTP);
        txtOtp = (EditText)findViewById(R.id.edtOTP);
        gonext.setVisibility(View.GONE);
        txtOtp.setVisibility(View.GONE);

        btnGetOtp.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(edtMobile.getText().toString().trim().length()<=5)
                {
                    edtMobile.setError("Invalid mobile number");
                }
                else
                {
                    /*

                    txtOtp.setText("");
                    otpPass = "";
                    edtMobile.setFocusable(false);
                    btnGetOtp.setEnabled(false);

                    txtOtp.setVisibility(View.VISIBLE);
                    gonext.setVisibility(View.VISIBLE);

                    Random r = new Random();

                    for (int i = 0; i < 6; i++)
                    {
                        otpPass += r.nextInt(9) + "";
                        txtOtp.setText("" + otpPass);
                        gonext.setVisibility(View.VISIBLE);
                    }

                    */

                    con = new ConnectionDetector(FirstActivity.this);
                    if(con.isConnectingToInternet())
                    {
                        checkFirst(edtMobile.getText().toString().trim());
                    }
                    else
                        callFunction("No internet connection");
                }
            }
        });

        gonext.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                con = new ConnectionDetector(FirstActivity.this);
                if(con.isConnectingToInternet())
                {
                    gonext.setEnabled(false);
                    strUserName = txtOtp.getText().toString().trim();
                    if(!strUserName.equals(otpPass))
                    {
                        gonext.setEnabled(true);
                        txtOtp.setError("Invalid OTP");
                        return;
                    }
                    else
                    {
                        if(newUser.equals("yes"))
                        {
                            Intent i = new Intent(FirstActivity.this,SecondActivity.class);
                            i.putExtra("mobile",edtMobile.getText().toString().trim());
                            i.putExtra("token",sharedpreferences.getString("tk",""));
                            startActivity(i);
                        }
                        else
                        {
                            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                            {
                                if (checkPermissionCamera())
                                {
                                    if(checkPermissionGallery())
                                    {
                                        if(checkPermissionAudio())
                                        {
                                            if(checkPermisionPhone())
                                            {
                                                if(checkPermissionContact())
                                                {
                                                    if(checkPermissionWake())
                                                    {
                                                        Intent i = new Intent(FirstActivity.this, UserList.class);
                                                        final Intent serviceIntent = new Intent(getApplicationContext(), MessageService.class);
                                                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                        startService(serviceIntent);
                                                        startActivity(i);

                                                        System.out.println("the user old");
                                                    }
                                                    else
                                                    {
                                                        gonext.setEnabled(true);
                                                        requestPermissionWake();
                                                    }
                                                }
                                                else
                                                {
                                                    gonext.setEnabled(true);
                                                    requestPermissionContact();
                                                }
                                            }
                                            else
                                            {
                                                gonext.setEnabled(true);
                                                requestPermissionPhone();
                                            }
                                        }
                                        else
                                        {
                                            gonext.setEnabled(true);
                                            requestPermisionAudio();
                                        }
                                    }
                                    else
                                    {
                                        gonext.setEnabled(true);
                                        requestPermissionGallery();
                                    }
                                }
                                else
                                {
                                    gonext.setEnabled(true);
                                    requestPermission();
                                }
                            }
                            else
                            {
                                Intent i = new Intent(FirstActivity.this,UserList.class);
                                final Intent serviceIntent = new Intent(getApplicationContext(), MessageService.class);
                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startService(serviceIntent);
                                startActivity(i);

                                System.out.println("the user old");
                            }
                        }
                    }
                }
                else
                    callFunction("No internet connection");
            }
        });
    }

    private void getToken()
    {
        con = new ConnectionDetector(FirstActivity.this);
        if (con.isConnectingToInternet())
        {
            System.out.println("the inside token ");
            mRegistrationBroadcastReceiver = new BroadcastReceiver()
            {
                @Override
                public void onReceive(Context context, Intent intent)
                {
                    //If the broadcast has received with success
                    //that means device is registered successfully
                    if (intent.getAction().equals(GCMRegistrationIntentService.REGISTRATION_SUCCESS))
                    {
                        //Getting the registration token from the intent
                        System.out.println("the inside token 1 ");
                        token = intent.getStringExtra("token");
                        System.out.println("the got token :" + token);

                    }
                    else if (intent.getAction().equals(GCMRegistrationIntentService.REGISTRATION_ERROR))
                    {
                        System.out.println("the registration error");
                        Toast.makeText(getApplicationContext(), "GCM registration error!", Toast.LENGTH_LONG).show();
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(), "Error occurred", Toast.LENGTH_LONG).show();
                        System.out.println("the inside token 3");
                    }
                }
            };
        }
        else
        {
            Toast.makeText(getApplicationContext(), "Please switch on the internet connection..", Toast.LENGTH_LONG).show();
        }
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());

        //if play service is not available
        if (ConnectionResult.SUCCESS != resultCode)
        {
            //If play service is supported but not installed
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode))
            {
                //Displaying message that play service is not installed
                System.out.println("the inside token 4 ");

                Toast.makeText(getApplicationContext(), "Google Play Service is not install/enabled in this device!", Toast.LENGTH_LONG).show();
                GooglePlayServicesUtil.showErrorNotification(resultCode, getApplicationContext());
            }
            else
            {
                Toast.makeText(getApplicationContext(), "This device does not support for Google Play Service!", Toast.LENGTH_LONG).show();
            }
            //If play service is available
        }
        else
        {
            //Starting intent to register device
            System.out.println("the inside token 6");
            Intent itent = new Intent(FirstActivity.this, GCMRegistrationIntentService.class);
            startService(itent);
        }

    }

    private void requestPermissionWake()
    {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WAKE_LOCK))
        {
            final Dialog dialog = new Dialog(activity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_design);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.show();
            TextView tvTitle = (TextView) dialog.findViewById(R.id.txtHeader);
            tvTitle.setText("Allow Permission");
            TextView tvText = (TextView) dialog.findViewById(R.id.txtBody);
            tvText.setText("This permission allows us to Wake Lock. Please allow in App Settings for additional functionality.");
            Button buttonDialogYes = (Button) dialog.findViewById(R.id.btnDialogok);
            buttonDialogYes.setOnClickListener(new View.OnClickListener()
            {
                public void onClick(View v)
                {
                    dialog.dismiss();
                    ActivityCompat.requestPermissions(activity,
                            new String[]{Manifest.permission.WAKE_LOCK},
                            PERMISSION_REQUEST_CODE_WAKE);
                }
            });
            //  Toast.makeText(context,"This permission allows us to GET TASK. Please allow in App Settings for additional functionality.",Toast.LENGTH_LONG).show();
        }
        else
        {
            ActivityCompat.requestPermissions(activity,new String[]{Manifest.permission.WAKE_LOCK},PERMISSION_REQUEST_CODE_WAKE);
        }
    }

    private void requestPermissionGallery()
    {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE))
        {
            final Dialog dialog = new Dialog(activity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_design);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.show();
            TextView tvTitle = (TextView) dialog.findViewById(R.id.txtHeader);
            tvTitle.setText("Allow Permission");
            TextView tvText = (TextView) dialog.findViewById(R.id.txtBody);
            tvText.setText("This permission allows us to Gallery. Please allow in App Settings for additional functionality.");
            Button buttonDialogYes = (Button) dialog.findViewById(R.id.btnDialogok);
            buttonDialogYes.setOnClickListener(new View.OnClickListener()
            {
                public void onClick(View v)
                {
                    dialog.dismiss();
                    ActivityCompat.requestPermissions(activity,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            PERMISSION_REQUEST_CODE_GALLERY);
                }
            });
            //  Toast.makeText(context,"This permission allows us to GET TASK. Please allow in App Settings for additional functionality.",Toast.LENGTH_LONG).show();
        }
        else
        {
            ActivityCompat.requestPermissions(activity,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},PERMISSION_REQUEST_CODE_GALLERY);
        }
    }

    private void requestPermisionAudio()
    {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity,Manifest.permission.RECORD_AUDIO))
        {
            final Dialog dialog = new Dialog(activity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_design);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.show();
            TextView tvTitle = (TextView) dialog.findViewById(R.id.txtHeader);
            tvTitle.setText("Allow Permission");
            TextView tvText = (TextView) dialog.findViewById(R.id.txtBody);
            tvText.setText("This permission allows us to Record Audio. Please allow in App Settings for additional functionality.");
            Button buttonDialogYes = (Button) dialog.findViewById(R.id.btnDialogok);
            buttonDialogYes.setOnClickListener(new View.OnClickListener()
            {
                public void onClick(View v)
                {
                    dialog.dismiss();
                    ActivityCompat.requestPermissions(activity,
                            new String[]{Manifest.permission.RECORD_AUDIO},
                            PERMISSION_REQUEST_CODE_AUDIO);
                }
            });
            //  Toast.makeText(context,"This permission allows us to GET TASK. Please allow in App Settings for additional functionality.",Toast.LENGTH_LONG).show();
        }
        else
        {
            ActivityCompat.requestPermissions(activity,new String[]{Manifest.permission.RECORD_AUDIO},PERMISSION_REQUEST_CODE_AUDIO);
        }
    }

    private boolean checkPermissionGallery()
    {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    private boolean checkPermissionAudio()
    {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        if (result == PackageManager.PERMISSION_GRANTED)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    private boolean checkPermissionWake()
    {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WAKE_LOCK);
        if (result == PackageManager.PERMISSION_GRANTED)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    private boolean checkPermissionCamera()
    {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA);
        if (result == PackageManager.PERMISSION_GRANTED)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    private void requestPermission()
    {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity,Manifest.permission.CAMERA))
        {
            final Dialog dialog = new Dialog(activity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_design);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.show();
            TextView tvTitle = (TextView) dialog.findViewById(R.id.txtHeader);
            tvTitle.setText("Allow Permission");
            TextView tvText = (TextView) dialog.findViewById(R.id.txtBody);
            tvText.setText("This permission allows us to CAMERA. Please allow in App Settings for additional functionality.");
            Button buttonDialogYes = (Button) dialog.findViewById(R.id.btnDialogok);
            buttonDialogYes.setOnClickListener(new View.OnClickListener()
            {
                public void onClick(View v)
                {
                    dialog.dismiss();
                    ActivityCompat.requestPermissions(activity,
                            new String[]{Manifest.permission.CAMERA},
                            PERMISSION_REQUEST_CODE_CAMERA);
                }
            });

            //  Toast.makeText(context,"This permission allows us to GET TASK. Please allow in App Settings for additional functionality.",Toast.LENGTH_LONG).show();
        }
        else
        {
            ActivityCompat.requestPermissions(activity,new String[]{Manifest.permission.CAMERA},PERMISSION_REQUEST_CODE_CAMERA);
        }
    }

    private void requestPermissionPhone()
    {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity,Manifest.permission.READ_PHONE_STATE))
        {
            final Dialog dialog = new Dialog(activity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_design);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.show();
            TextView tvTitle = (TextView) dialog.findViewById(R.id.txtHeader);
            tvTitle.setText("Allow Permission");
            TextView tvText = (TextView) dialog.findViewById(R.id.txtBody);
            tvText.setText("This permission allows us to READ_PHONE. Please allow in App Settings for additional functionality.");
            Button buttonDialogYes = (Button) dialog.findViewById(R.id.btnDialogok);
            buttonDialogYes.setOnClickListener(new View.OnClickListener()
            {
                public void onClick(View v)
                {
                    dialog.dismiss();
                    ActivityCompat.requestPermissions(activity,
                            new String[]{Manifest.permission.READ_PHONE_STATE},
                            PERMISSION_REQUEST_CODE_PHONE);
                }
            });
            //  Toast.makeText(context,"This permission allows us to GET TASK. Please allow in App Settings for additional functionality.",Toast.LENGTH_LONG).show();
        }
        else
        {
            ActivityCompat.requestPermissions(activity,new String[]{Manifest.permission.READ_PHONE_STATE},PERMISSION_REQUEST_CODE_PHONE);
        }
    }

    private boolean checkPermisionPhone()
    {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE);
        if (result == PackageManager.PERMISSION_GRANTED)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    private boolean checkPermissionContact()
    {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_CONTACTS);
        if (result == PackageManager.PERMISSION_GRANTED)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    private void requestPermissionContact()
    {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity,Manifest.permission.READ_CONTACTS))
        {
            final Dialog dialog = new Dialog(activity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_design);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.show();
            TextView tvTitle = (TextView) dialog.findViewById(R.id.txtHeader);
            tvTitle.setText("Allow Permission");
            TextView tvText = (TextView) dialog.findViewById(R.id.txtBody);
            tvText.setText("This permission allows us to READ_CONTACTS. Please allow in App Settings for additional functionality.");
            Button buttonDialogYes = (Button) dialog.findViewById(R.id.btnDialogok);
            buttonDialogYes.setOnClickListener(new View.OnClickListener()
            {
                public void onClick(View v)
                {
                    dialog.dismiss();
                    ActivityCompat.requestPermissions(activity,
                            new String[]{Manifest.permission.READ_CONTACTS},
                            PERMISSION_REQUEST_CODE_CONTACT);
                }
            });
            //  Toast.makeText(context,"This permission allows us to GET TASK. Please allow in App Settings for additional functionality.",Toast.LENGTH_LONG).show();
        }
        else
        {
            ActivityCompat.requestPermissions(activity,new String[]{Manifest.permission.READ_CONTACTS},PERMISSION_REQUEST_CODE_CONTACT);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        switch (requestCode)
        {
            case PERMISSION_REQUEST_CODE_CAMERA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    System.out.println("the permission granted camera");
                }
                else
                {
                    Toast.makeText(context,"Permission Denied",Toast.LENGTH_SHORT).show();
                }
                break;
            case PERMISSION_REQUEST_CODE_GALLERY:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    System.out.println("the permission granted gallery");
                }
                else
                {
                    Toast.makeText(context,"Permission Denied",Toast.LENGTH_SHORT).show();
                }
                break;
            case PERMISSION_REQUEST_CODE_AUDIO:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    System.out.println("the permission granted audio");
                }
                else
                {
                    Toast.makeText(context,"Permission Denied",Toast.LENGTH_SHORT).show();
                }
                break;
            case PERMISSION_REQUEST_CODE_PHONE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    System.out.println("the permission granted phone");
                }
                else
                {
                    Toast.makeText(context,"Permission Denied",Toast.LENGTH_SHORT).show();
                }
                break;
            case PERMISSION_REQUEST_CODE_CONTACT:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    System.out.println("the permission granted contact");
                }
                else
                {
                    Toast.makeText(context,"Permission Denied",Toast.LENGTH_SHORT).show();
                }
                break;
            case PERMISSION_REQUEST_CODE_WAKE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    System.out.println("the permission granted Wake");
                }
                else
                {
                    Toast.makeText(context,"Permission Denied",Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void checkFirst(final String mobile)
    {
       SendData = new AsyncTask<Void, Void, Void>()
        {
            @Override
            protected Void doInBackground(Void... voids)
            {
                String serverUrl = StaticService.login;
                System.out.println("the url is :" + serverUrl);
                HttpClient client = new DefaultHttpClient();
                HttpPost post = new HttpPost(serverUrl);
                try
                {
                    JSONObject jo = new JSONObject();
                    token = sharedpreferences.getString("tk","");
                    System.out.println("the token is :" + token);
                    jo.put("phone",mobile);
                    jo.put("token",token);
                    System.out.println("the json object tag :" + jo);
                    StringEntity se = new StringEntity(jo.toString());
                    se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,"application/json"));
                    post.setEntity(se);
                    HttpResponse response = client.execute(post);
                    HttpEntity entity = response.getEntity();
                    String responseText = EntityUtils.toString(entity);
                    response_json = new JSONObject(responseText);
                    System.out.println("the json response login:"+ response_json);
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
                con = new ConnectionDetector(FirstActivity.this);
                if (con.isConnectingToInternet())
                {
                    progress = new ProgressDialog(FirstActivity.this);
                    progress.setMessage("Please wait...");
                    progress.setCanceledOnTouchOutside(false);
                    progress.show();
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
                progress.dismiss();
                try
                {
                    if(response_json.getString("success").equals("1"))
                    {
                        JSONArray j = response_json.getJSONArray("posts");
                        JSONObject jo = j.getJSONObject(0);

                        System.out.println("the regisre mobile :" + jo.getString("Phone"));
                        sharedpreferences.edit().putString("mymobile",jo.getString("Phone")).commit();
                        sharedpreferences.edit().putString("myImage",jo.getString("Profile_Image")).commit();

                        txtOtp.setText(jo.getString("Otp"));
                        newUser="";
                        otpPass = jo.getString("Otp");
                        edtMobile.setFocusable(false);
                        btnGetOtp.setEnabled(false);
                        txtOtp.setVisibility(View.VISIBLE);
                        gonext.setVisibility(View.VISIBLE);
                    }
                    else if(response_json.getString("success").equals("2"))
                    {
                        txtOtp.setText(response_json.getString("Otp"));
                        newUser="yes";
                        otpPass = response_json.getString("Otp");
                        edtMobile.setFocusable(false);
                        btnGetOtp.setEnabled(false);
                        txtOtp.setVisibility(View.VISIBLE);
                        gonext.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        callFunction("Something going wrong on server");
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

    private void callFunction(String s)
    {
        LayoutInflater li = LayoutInflater.from(FirstActivity.this);
        View promptsView = li.inflate(R.layout.dialogloerror, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(FirstActivity.this);
        final TextView text = (TextView) promptsView
                .findViewById(R.id.textError);
        text.setText(""+s);
        text.setTypeface(Typeface.DEFAULT);
        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);
        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setNeutralButton("OK", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        Log.w("the MainActivity", "onResume");
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(GCMRegistrationIntentService.REGISTRATION_SUCCESS));
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(GCMRegistrationIntentService.REGISTRATION_ERROR));
    }

    //Unregistering receiver on activity paused

    @Override
    protected void onPause()
    {
        super.onPause();
        Log.w("MainActivity", "onPause");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
    }
}
