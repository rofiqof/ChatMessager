package app.com.mychat;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.NotificationCompat;
import com.google.android.gms.gcm.GcmListenerService;

import org.apache.commons.lang3.StringEscapeUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Random;

public class GCMPushReceiverService extends GcmListenerService
{
    private SharedPreferences sharedpreferences;
    AsyncTask<Void, Void, Void> SendData;
    String username,id,img,type;
    @Override
    public void onMessageReceived(String from, Bundle data)
    {
        sharedpreferences = getSharedPreferences("pref",Context.MODE_PRIVATE);
        String message = data.getString("message");
        id = data.getString("from_id");
        img = data.getString("image_url");
        username = getContactName(data.getString("from_id"));
        type = data.getString("type");

        System.out.println("the got message :" + message + "\n  Id : "+data.getString("from_id"));
        System.out.println("the image url is :" + data.getString("image_url") + "\n type is :" + data.getString("type"));
        try
        {
            if(type.equals("text") || type.equals("Text"))
            {
               System.out.println("the message :" + message);
            }
            else if (type.equals("image"))
            {
                message = "" + type;
            }
            else if (type.equals("video"))
            {
                message = "" + type;
            }
            else if (type.equals("pdf"))
            {
                message = "" + type;
            }

            if(sharedpreferences.getString("rec_no","").equals(id))
            {
                System.out.println("the user olready online" );
            }
            else
                new generatePictureStyleNotification(this, "ChatMessanger", username + ":" + message, "" + img).execute();
        }
        catch (Exception e)
        {

        }
        // sendNotification(username +" : "+message);
    }

   /* private void sendNotification(String message)
    {
        Intent i1 = new Intent(this, ChatRoomActivity.class);
        i1.putExtra("name", username);
        i1.putExtra("RECIPIENT_ID", id);
        i1.putExtra("img",img);
        sharedpreferences.edit().putString("userImg",img.toString()).commit();
        i1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        int requestCode = 0;

        PendingIntent pendingIntent = PendingIntent.getActivity(this, requestCode, i1, PendingIntent.FLAG_ONE_SHOT);
        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder noBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.noti)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentTitle("ChatMessanger")
                .setSound(sound)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, noBuilder.build());
    }
    */

    private String getContactName(String phoneNumber)
    {
        ContentResolver cr = getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = cr.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
        if (cursor == null)
        {
            return phoneNumber;
        }
        String contactName = phoneNumber;
        if(cursor.moveToFirst())
        {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
        }

        if(cursor != null && !cursor.isClosed())
        {
            cursor.close();
        }
        return contactName;
    }

    public class generatePictureStyleNotification extends AsyncTask<String, Void, Bitmap>
    {
        private Context mContext;
        private String title, message, imageUrl;

        public generatePictureStyleNotification(Context context, String title, String message, String imageUrl)
        {
            super();
            this.mContext = context;
            this.title = title;
            this.message = message;
            this.imageUrl = imageUrl;
        }

        @Override
        protected Bitmap doInBackground(String... params)
        {
            InputStream in;
            try
            {
                URL url = new URL(this.imageUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                in = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(in);
                return myBitmap;
            }
            catch (MalformedURLException e)
            {
                System.out.println("the exe 1 is :" + e);
            }
            catch (IOException e)
            {
                System.out.println("the exe 2 is :" + e);
            }
            return null;
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        protected void onPostExecute(Bitmap result)
        {
            super.onPostExecute(result);
            Intent intent = new Intent(mContext,ChatRoomActivity.class);

            Bundle b = new Bundle();
            b.putString("name", username);
            b.putString("RECIPIENT_ID", id);
            b.putString("img",img);
            b.putString("where","noti");
            intent.putExtras(b);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            sharedpreferences.edit().putString("userImg",img).commit();

            PendingIntent pendingIntent = PendingIntent.getActivity(mContext, new Random().nextInt(),
                    intent,PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

            String fromServerUnicodeDecoded = null;
            try
            {
                fromServerUnicodeDecoded = StringEscapeUtils.unescapeJava(URLDecoder.decode(message, "UTF-8"));
            }
            catch (UnsupportedEncodingException e)
            {
                e.printStackTrace();

                System.out.println("the emojis decode exe :" + e);

            }
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            Notification notif = new Notification.Builder(mContext)
                    .setContentIntent(pendingIntent)
                    .setContentTitle(title)
                    .setContentText(fromServerUnicodeDecoded)
                    .setSmallIcon(R.drawable.noti)
                    .setLargeIcon(result)
                    .setSound(notification)
                    // .setStyle(new Notification.BigPictureStyle().bigPicture(result))
                    .build();
            notif.flags |= Notification.FLAG_AUTO_CANCEL;

            notificationManager.notify(1, notif);
            message="";
            type="";
        }
    }
}
