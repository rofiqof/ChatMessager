package app.com.mychat;

import android.app.Activity;
import android.app.ProgressDialog;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

public class VideoActivity extends Activity
{
    ImageView back;
    String vdo;
    ProgressDialog pDialog;
    VideoView videoview;

    // Insert your Video URL
    String VideoURL ;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        back = (ImageView)findViewById(R.id.imgBack);
        back.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onBackPressed();
            }
        });
        VideoURL =  getIntent().getStringExtra("vdo");
        System.out.println("the video link :" + VideoURL);

        videoview = (VideoView) findViewById(R.id.VideoView);
        // Execute StreamVideo AsyncTask
        // Create a progressbar
        pDialog = new ProgressDialog(VideoActivity.this);
        // Set progressbar title
         // Set progressbar message
        pDialog.setMessage("Buffering...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        // Show progressbar
        pDialog.show();

        try
        {
            // Start the MediaController
            MediaController mediacontroller = new MediaController(VideoActivity.this);
            mediacontroller.setAnchorView(videoview);
            // Get the URL from String VideoURL
            Uri video = Uri.parse(VideoURL);
            videoview.setMediaController(mediacontroller);
            videoview.setVideoURI(video);
        }
        catch (Exception e)
        {
            System.out.println("the exe in vdo :" + e);
        }

        videoview.requestFocus();
        videoview.setOnPreparedListener(new MediaPlayer.OnPreparedListener()
        {
            public void onPrepared(MediaPlayer mp)
            {
                pDialog.dismiss();
                videoview.start();
            }
        });
    }
}
