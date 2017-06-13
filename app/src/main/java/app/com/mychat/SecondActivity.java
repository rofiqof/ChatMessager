package app.com.mychat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import mehdi.sakout.fancybuttons.FancyButton;

public class SecondActivity extends AppCompatActivity
{
    ImageView imgPic;
    EditText edtName;
    AlertDialog.Builder builder;
    String mobile;
    SharedPreferences sharedpreferences;

    private static final int PERMISSION_REQUEST_CODE_CONTACT = 1;
    private static final int PERMISSION_REQUEST_CODE_PHONE = 2;
    private static final int PERMISSION_REQUEST_CODE_CAMERA = 3;
    private static final int PERMISSION_REQUEST_CODE_GALLERY = 4;
    private static final int PERMISSION_REQUEST_CODE_AUDIO = 5;
    private static final int PERMISSION_REQUEST_CODE_WAKE = 6;

    private Activity activity;
    Context context;
    ConnectionDetector con;
    private ProgressDialog progress;
    AsyncTask<Void, Void, Void> SendData;
    JSONObject response_json;
    FancyButton btnGo;
    String encodedImage,token;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.second_activity);
        context = getApplicationContext();
        activity = this;
        imgPic = (ImageView)findViewById(R.id.imgPic);
        edtName = (EditText)findViewById(R.id.edtUsername);
        mobile = getIntent().getStringExtra("mobile");
        token = getIntent().getStringExtra("token");
        System.out.println("the token no is :" + token);
        btnGo = (FancyButton)findViewById(R.id.btnGo);

        sharedpreferences = getSharedPreferences("pref",Context.MODE_PRIVATE);

        btnGo.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                con = new ConnectionDetector(SecondActivity.this);
                if (con.isConnectingToInternet())
                {
                    String mo = edtName.getText().toString().trim();
                    btnGo.setEnabled(false);
                    if (TextUtils.isEmpty(mo))
                    {
                        edtName.setError("Enter your name");
                        btnGo.setEnabled(true);
                        return;
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
                                                    sendData(mo);
                                                }
                                                else
                                                {
                                                    btnGo.setEnabled(true);
                                                    requestPermissionWake();
                                                }
                                            }
                                            else
                                            {
                                                btnGo.setEnabled(true);
                                                requestPermissionContact();
                                            }
                                        }
                                        else
                                        {
                                            btnGo.setEnabled(true);
                                            requestPermissionPhone();
                                        }
                                    }
                                    else
                                    {
                                        btnGo.setEnabled(true);
                                        requestPermisionAudio();
                                    }
                                }
                                else
                                {
                                    btnGo.setEnabled(true);
                                    requestPermissionGallery();
                                }
                            }
                            else
                            {
                                btnGo.setEnabled(true);
                                requestPermission();
                            }
                        }
                        else
                            sendData(mo);
                    }
                }
                else
                {
                    callFunction("No internet connection");
                    btnGo.setEnabled(true);
                }
            }
        });
        imgPic.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                selectImage();
            }
        });
    }
    private void sendData(final String mo)
    {

        SendData = new AsyncTask<Void, Void, Void>()
        {
            @Override
            protected void onPreExecute()
            {
                super.onPreExecute();
                con = new ConnectionDetector(getApplicationContext());
                if (con.isConnectingToInternet())
                {
                    progress = new ProgressDialog(SecondActivity.this);
                    progress.setMessage("Please wait...");
                    progress.setCanceledOnTouchOutside(false);
                    progress.show();
                    imgPic.buildDrawingCache();
                    Bitmap bitmap = imgPic.getDrawingCache();
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byte[] image = stream.toByteArray();
                    encodedImage = Base64.encodeToString(image, Base64.DEFAULT);
                }
                else
                {
                    SendData.cancel(true);
                    //setMessage("No internet connection","Please connect to internet. try again");
                }
            }

            @Override
            protected Void doInBackground(Void... voids)
            {
                String serverUrl = StaticService.register;
                HttpClient client = new DefaultHttpClient();
                HttpPost post = new HttpPost(serverUrl);
                try
                {
                    JSONObject jo = new JSONObject();

                    jo.put("phone",mobile);
                    jo.put("name",mo);
                    jo.put("token",token);
                   // sharedpreferences.edit().putString("myname",j.getString("Profile_Image")).commit();
                    jo.put("pro_img",encodedImage);

                    System.out.println("the json object tag :" + jo);
                    StringEntity se = new StringEntity(jo.toString());
                    se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,"application/json"));
                    post.setEntity(se);
                    HttpResponse response = client.execute(post);
                    HttpEntity entity = response.getEntity();
                    String responseText = EntityUtils.toString(entity);
                    System.out.println("the response :" + responseText);
                     response_json = new JSONObject(responseText);
                 }
                catch (Exception e)
                {
                    System.out.println("the exception in second : " + e);
                }
                return null;
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
                        JSONObject j = response_json.getJSONObject("posts");
                        //JSONObject jo = j.getJSONObject(0);

                        sharedpreferences.edit().putString("mymobile",j.getString("Phone")).commit();
                        sharedpreferences.edit().putString("myImage",j.getString("Profile_Image")).commit();

                        SharedPreferences sp = getSharedPreferences(Constants.SHARED_PREFS,MODE_PRIVATE);
                        SharedPreferences.Editor edit = sp.edit();
                        edit.putString(Constants.USER_NAME, mo);
                        edit.apply();

                        Intent i = new Intent(SecondActivity.this,UserList.class);
                        final Intent serviceIntent = new Intent(getApplicationContext(), MessageService.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startService(serviceIntent);
                        startActivity(i);
                    }
                    else if(response_json.getString("success").equals("0"))
                    {

                    }
                    else
                    {
                        callFunction("Something going wrong on server");
                    }
                }
                catch (Exception e)
                {
                    System.out.println("the second exception is  "+e);
                }
            }
        };
        SendData.execute();
    }
    private void callFunction(String s)
    {
        LayoutInflater li = LayoutInflater.from(SecondActivity.this);
        View promptsView = li.inflate(R.layout.dialogloerror, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SecondActivity.this);
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
    private void selectImage()
    {
        final CharSequence[] options = { "Take Photo", "Choose from Gallery","Cancel" };
        builder = new AlertDialog.Builder(SecondActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(options, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                if (options[i].equals("Take Photo"))
                {
                 if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                {
                    if(checkPermissionCamera())
                    {
                        System.out.println("the Permission already granted. ");
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        File f = new File(android.os.Environment.getExternalStorageDirectory(), "temp.jpg");
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                        startActivityForResult(intent, 1);
                    }
                    else
                    {
                        System.out.println("the Request Permission.");
                        requestPermission();
                    }
                }
                else
                {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File f = new File(android.os.Environment.getExternalStorageDirectory(), "temp.jpg");
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                    startActivityForResult(intent, 1);
                }
                }
                else if (options[i].equals("Choose from Gallery"))
                {
                    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    {
                        if(checkPermissionGallery())
                        {
                            System.out.println("the Permission already granted gallery. ");
                            Intent intent = new   Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(intent, 2);
                        }
                        else
                        {
                            System.out.println("the Request Permission for gallery.");
                            requestPermissionGallery();
                        }
                    }
                    else
                    {
                        Intent intent = new   Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(intent, 2);
                    }
                }
                else if (options[i].equals("Cancel"))
                {
                    dialogInterface.dismiss();
                }
            }
        } );
        builder.show();
    }
    private void requestPermissionGallery()
    {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity,Manifest.permission.WRITE_EXTERNAL_STORAGE))
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
            final Dialog dialog = new Dialog(SecondActivity.this);
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
            final Dialog dialog = new Dialog(SecondActivity.this);
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
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File f = new File(android.os.Environment.getExternalStorageDirectory(), "temp.jpg");
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                    startActivityForResult(intent, 1);
                }
                else
                {
                    Toast.makeText(context,"Permission Denied",Toast.LENGTH_SHORT).show();
                }
                break;
            case PERMISSION_REQUEST_CODE_GALLERY:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    Intent intent = new   Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, 2);
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
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK)
        {
            if (requestCode == 1)
            {
                File f = new File(Environment.getExternalStorageDirectory().toString());
                for (File temp : f.listFiles())
                {
                    if (temp.getName().equals("temp.jpg"))
                    {
                        f = temp;
                        break;
                    }
                }
                try
                {
                    Bitmap bitmap;
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inDither = false;
                    options.inJustDecodeBounds = false;
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    options.inSampleSize = 1;
                    options.inPurgeable = true;
                    options.inPreferQualityOverSpeed = true;
                    options.inTempStorage=new byte[32 * 1024];
                    System.out.println("the bitmap getting thay chee");

                    bitmap = BitmapFactory.decodeFile(f.getAbsolutePath(),options);
                    imgPic.setImageBitmap(bitmap);
                    btnGo.setVisibility(View.VISIBLE);

                  //  Bitmap resized = Bitmap.createScaledBitmap(bitmap, 200, 200, true);
                  //  im = bitmap;
                    // img.setImageBitmap(bitmap);
                   // imgPic.setTag("Done");

                    String path = android.os.Environment
                            .getExternalStorageDirectory()
                            + File.separator
                            + "Phoenix" + File.separator + "default";
                    f.delete();
                    OutputStream outFile = null;
                    File file = new File(path, String.valueOf(System.currentTimeMillis()) + ".jpg");
                    try
                    {
                        outFile = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outFile);
                        outFile.flush();
                        outFile.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            else if (requestCode == 2)
            {
                try
                {
                    Uri selectedImage = data.getData();
                    String[] filePath = {MediaStore.Images.Media.DATA};
                    Cursor c = getContentResolver().query(selectedImage, filePath, null, null, null);
                    c.moveToFirst();
                    int columnIndex = c.getColumnIndex(filePath[0]);
                    String picturePath = c.getString(columnIndex);
                    c.close();
                    Bitmap thumbnail;
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inDither = false;
                    options.inJustDecodeBounds = false;
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    options.inSampleSize = 1;
                    options.inPurgeable = true;
                    options.inPreferQualityOverSpeed = true;
                    options.inTempStorage = new byte[32 * 1024];
                    System.out.println("the bitmap getting thay chee");
                    thumbnail = (BitmapFactory.decodeFile(picturePath, options));
                    imgPic.setImageBitmap(thumbnail);
                    btnGo.setVisibility(View.VISIBLE);
                   // Bitmap resized = Bitmap.createScaledBitmap(thumbnail, 200, 200, true);
                   // im = thumbnail;
                  //  imgPic.setImageBitmap(thumbnail);
                   // imgPic.setTag("Done");
                }
                catch (Exception e)
                {
                    callFunction("Error in getting image \n "+e);
                }
            }
        }
    }
}
