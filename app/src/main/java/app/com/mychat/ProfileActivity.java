package app.com.mychat;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.os.Bundle;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubException;
import com.squareup.picasso.Picasso;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import mehdi.sakout.fancybuttons.FancyButton;


public class ProfileActivity extends Activity
{
    EditText edtName;
    AlertDialog.Builder builder;
    SharedPreferences sharedpreferences;
    String img,currentUserId;
    private Activity activity;
    Context context;
    ConnectionDetector con;
    private ProgressDialog progress;
    AsyncTask<Void, Void, Void> SendData;
    JSONObject response_json;
    FancyButton btnGo;
    String encodedImage;
    ImageView imgPic,imgBlur,camButton;

    private Pubnub pubnub;
    private static final String PUBLISH_KEY = "pub-c-60ab2436-b4ca-4921-bb3a-1b94cbd7b2ff";
    private static final String P_SECRET_KEY = "sec-c-NGVlYWRhODEtM2UyNS00YmZiLWFmMDctMzBmZWQyM2Q5YTNi";
    private static final String SUBSCRIBE_KEY = "sub-c-f917bc56-8154-11e6-8409-0619f8945a4f";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        sharedpreferences = getSharedPreferences("pref", Context.MODE_PRIVATE);
        currentUserId = sharedpreferences.getString("mymobile","");
        context = getApplicationContext();
        activity = this;
        imgPic = (ImageView)findViewById(R.id.imgPic);
        imgBlur=(ImageView)findViewById(R.id.imgBlur);
        camButton=(ImageView)findViewById(R.id.camButton);

        String sh = (sharedpreferences.getString("myImage",""));
        System.out.println("the image path :" + sh);

        Picasso.with(context).load(sharedpreferences.getString("myImage","")) .placeholder(R.drawable.img_pic)
                .error(R.drawable.img_pic) .into(imgPic);

        Picasso.with(context).load(sharedpreferences.getString("myImage","")) .placeholder(R.drawable.img_pic)
                .error(R.drawable.img_pic) .into(imgBlur);
        // optional

        Handler handler = new Handler();
        Runnable r = new Runnable()
        {
            @Override
            public void run()
            {
                BitmapDrawable drawable = (BitmapDrawable) imgBlur.getDrawable();
                Bitmap bitmap = drawable.getBitmap();
                Bitmap blurred = blurRenderScript(bitmap, 25);//second parametre is radius
                imgBlur.setImageBitmap(blurred);
            }
        };
        handler.postDelayed(r,2000);


        pubnub = new Pubnub(PUBLISH_KEY,SUBSCRIBE_KEY);
        pubnub.setUUID(currentUserId);

        try {
            pubnub.subscribe("calling_channel", new Callback() {

            });
        }
        catch (PubnubException e)
        {
            Log.d("PubnubException",e.toString());
        }

        btnGo = (FancyButton)findViewById(R.id.btnGo);
        btnGo.setEnabled(false);
        imgPic.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                selectImage();
            }
        });
        btnGo.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                sendData(currentUserId);
            }
        });
        camButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                selectImage();
            }
        });
    }
    private void selectImage()
    {
        final CharSequence[] options = { "Take Photo", "Choose from Gallery","Cancel" };
        builder = new AlertDialog.Builder(ProfileActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(options, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                if (options[i].equals("Take Photo"))
                {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File f = new File(android.os.Environment.getExternalStorageDirectory(), "temp.jpg");
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                    startActivityForResult(intent, 1);
                }
                else if (options[i].equals("Choose from Gallery"))
                {
                    Intent intent = new   Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, 2);
                }
                else if (options[i].equals("Cancel"))
                {
                    dialogInterface.dismiss();
                }
            }
        } );
        builder.show();
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
                    btnGo.setEnabled(true);

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
                    btnGo.setEnabled(true);
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

    private void callFunction(String s)
    {
        LayoutInflater li = LayoutInflater.from(ProfileActivity.this);
        View promptsView = li.inflate(R.layout.dialogloerror, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ProfileActivity.this);
        final TextView text = (TextView) promptsView
                .findViewById(R.id.textError);
        text.setText(""+s);
        text.setTypeface(Typeface.DEFAULT);
         alertDialogBuilder.setView(promptsView);
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
                    progress = new ProgressDialog(ProfileActivity.this);
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
                String serverUrl = StaticService.update;
                HttpClient client = new DefaultHttpClient();
                HttpPost post = new HttpPost(serverUrl);
                try
                {
                    JSONObject jo = new JSONObject();
                    jo.put("phone",mo);
                    jo.put("pro_img",encodedImage);
                    System.out.println("the json object tag :" + jo);
                    StringEntity se = new StringEntity(jo.toString());
                    se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,"application/json"));
                    post.setEntity(se);
                    HttpResponse response = client.execute(post);
                    HttpEntity entity = response.getEntity();
                    String responseText = EntityUtils.toString(entity);
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
                          JSONObject jo = response_json.getJSONObject("posts");
                          System.out.println("the got image is :" + jo.getString("pro_img"));
                          sharedpreferences.edit().putString("myImage",jo.getString("pro_img")).commit();
                       // Picasso.with(context).load(sharedpreferences.getString("myImage","")).into(imgPic);
                        Picasso.with(context).load(sharedpreferences.getString("myImage","")) .placeholder(R.drawable.img_pic)
                                .error(R.drawable.img_pic) .into(imgBlur);
                        Handler handler = new Handler();
                        Runnable r = new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                BitmapDrawable drawable = (BitmapDrawable) imgBlur.getDrawable();
                                Bitmap bitmap = drawable.getBitmap();
                                Bitmap blurred = blurRenderScript(bitmap, 25);//second parametre is radius
                                imgBlur.setImageBitmap(blurred);
                            }
                        };
                        handler.postDelayed(r,1000);
                        btnGo.setEnabled(false);

                        // onBackPressed();
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

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private Bitmap blurRenderScript(Bitmap smallBitmap, int radius)
    {
        try {
            smallBitmap = RGB565toARGB888(smallBitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }


        Bitmap bitmap = Bitmap.createBitmap(
                smallBitmap.getWidth(), smallBitmap.getHeight(),
                Bitmap.Config.ARGB_8888);

        RenderScript renderScript = RenderScript.create(context);

        Allocation blurInput = Allocation.createFromBitmap(renderScript, smallBitmap);
        Allocation blurOutput = Allocation.createFromBitmap(renderScript, bitmap);

        ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(renderScript,Element.U8_4(renderScript));
        blur.setInput(blurInput);
        blur.setRadius(radius);
        blur.forEach(blurOutput);

        blurOutput.copyTo(bitmap);
        renderScript.destroy();

        return bitmap;
    }

    private Bitmap RGB565toARGB888(Bitmap img) throws Exception
    {
        int numPixels = img.getWidth() * img.getHeight();
        int[] pixels = new int[numPixels];

        //Get JPEG pixels.  Each int is the color values for one pixel.
        img.getPixels(pixels, 0, img.getWidth(), 0, 0, img.getWidth(), img.getHeight());

        //Create a Bitmap of the appropriate format.
        Bitmap result = Bitmap.createBitmap(img.getWidth(), img.getHeight(), Bitmap.Config.ARGB_8888);

        //Set RGB pixels.
        result.setPixels(pixels, 0, result.getWidth(), 0, 0, result.getWidth(), result.getHeight());
        return result;
    }

    @Override
    protected void onResume()
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
    }

    @Override
    public void onPause()
    {
        super.onPause();
        pubnub.unsubscribe("calling_channel");
    }

    @Override
    protected void onStop() {
        super.onStop();
        pubnub.unsubscribe("calling_channel");
    }

}
