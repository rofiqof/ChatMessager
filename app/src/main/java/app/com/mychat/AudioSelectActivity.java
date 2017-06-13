package app.com.mychat;

import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import java.io.IOException;

public class AudioSelectActivity extends AppCompatActivity
{
    ImageView ani_stop;
    String link;
    SharedPreferences pref;
    String file;
    ImageView btnTv;
    MediaPlayer mPlayer;
    SeekBar seek_bar;
    Handler seekHandler = new Handler();
    private int mediaFileLengthInMilliseconds;
    ImageView back;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_select);
        seek_bar = (SeekBar)findViewById(R.id.seek_bar);
        seek_bar.setMax(99);
        ani_stop = (ImageView)findViewById(R.id.imageView5);
      //  btnDone = (ImageView)findViewById(R.id.btnDone);
        btnTv = (ImageView)findViewById(R.id.btnTv);
        back = (ImageView)findViewById(R.id.imgBack);

        link = getIntent().getExtras().getString("audio");
        back.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onBackPressed();
            }
        });

        System.out.println("the audio link is :"+link);

        mPlayer = new MediaPlayer();
        mPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener()
        {
            @Override
            public void onBufferingUpdate(MediaPlayer mp, int percent)
            {
                seek_bar.setSecondaryProgress(percent);
            }
        });

        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
        {
            @Override
            public void onCompletion(MediaPlayer mp)
            {
                btnTv.setImageResource(R.drawable.paly);
                ani_stop.setVisibility(View.VISIBLE);
            }
        });

        seek_bar.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if(mPlayer.isPlaying())
                {
                    SeekBar sb = (SeekBar)v;
                    int playPositionInMillisecconds = (mediaFileLengthInMilliseconds / 100) * sb.getProgress();
                    mPlayer.seekTo(playPositionInMillisecconds);
                }
                return false;
            }
        });

        btnTv.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                try
                {
                    mPlayer.setDataSource(link);
                    mPlayer.prepare();
                    ani_stop.setVisibility(View.GONE);
                }
                catch (IllegalStateException e)
                {

                }
                catch (IOException e)
                {
                    System.out.println("the You might not set the URI " + e);
                }

                mediaFileLengthInMilliseconds = mPlayer.getDuration();

                if(!mPlayer.isPlaying())
                {
                    mPlayer.start();
                    btnTv.setImageResource(R.drawable.pause);
                    ani_stop.setVisibility(View.GONE);
                }
                else
                {
                    mPlayer.pause();
                    btnTv.setImageResource(R.drawable.paly);
                    ani_stop.setVisibility(View.VISIBLE);
                }
                primarySeekBarProgressUpdater();
            }
        });

    }
    private void primarySeekBarProgressUpdater()
    {
        try
        {
            seek_bar.setProgress((int) (((float) mPlayer.getCurrentPosition() / mediaFileLengthInMilliseconds) * 100)); // This math construction give a percentage of "was playing"/"song length"
            if (mPlayer.isPlaying())
            {
                Runnable notification = new Runnable()
                {
                    public void run()
                    {
                        primarySeekBarProgressUpdater();
                    }
                };
                seekHandler.postDelayed(notification, 1000);
            }
        }
        catch (Exception e)
        {
            System.out.println("the exe in change :" + e);
        }
    }
    @Override
    public void onPause()
    {
        super.onPause();
        if(mPlayer!=null && mPlayer.isPlaying())
        {
            mPlayer.release();
            mPlayer = null;
        }
        else
            System.out.println("the pause 3");

    }

    @Override
    public void onStop()
    {
        super.onStop();
        if(mPlayer!=null && mPlayer.isPlaying())
        {
            mPlayer.release();
            mPlayer = null;
        }
        else
            System.out.println("the pause 3");
    }
}
