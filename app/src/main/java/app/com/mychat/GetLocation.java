package app.com.mychat;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.InputStream;

public class GetLocation extends AppCompatActivity
{
    double longitude;
    double latitude;
    GPSTracker gps;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_location);
        gps = new GPSTracker(GetLocation.this);

        if(gps.canGetLocation())
        {
            longitude = gps.getLongitude();
            latitude = gps .getLatitude();

            Toast.makeText(getApplicationContext(),"Longitude:"+Double.toString(longitude)+"\nLatitude:"+Double.toString(latitude),Toast.LENGTH_SHORT).show();
        }
        else
        {
            gps.showSettingsAlert();
        }

       /*
            https://maps.googleapis.com/maps/api/staticmap?center=Brooklyn+Bridge,New+York,NY&zoom=13&size=600x300&maptype=roadmap
            &markers=color:blue%7Clabel:S%7C40.702147,-74.015794&markers=color:green%7Clabel:G%7C40.711614,-74.012318
            &markers=color:red%7Clabel:C%7C40.718217,-73.998284
            &key=YOUR_API_KEY
       */

        Bitmap thum = getGoogleMapThumbnail(latitude+"",longitude+"");
        ImageView img = (ImageView)findViewById(R.id.imgLocation);
        img.setImageBitmap(thum);
    }

    public static Bitmap getGoogleMapThumbnail(String lati1, String longi1)
    {
        String URL = "http://maps.google.com/maps/api/staticmap?center=" +lati1 + "," + longi1 + "&markers=color:red%7Clabel:C%7C"+lati1+","+longi1+"&zoom=15&size=300x300&sensor=false";
        System.out.println("the url bitmap is:"+URL);
        Bitmap bmp = null;
        HttpClient httpclient = new DefaultHttpClient();
        HttpGet request = new HttpGet(URL);
        InputStream in = null;
        try
        {
            in = httpclient.execute(request).getEntity().getContent();
            bmp = BitmapFactory.decodeStream(in);
            in.close();
        }
        catch (IOException e)
        {
            System.out.println("the exe e :" + e );
        }
        return bmp;
    }
}
