package app.com.mychat;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import java.io.FileInputStream;
import java.io.IOException;

public class AudioPlayer
{
    static final String LOG_TAG = AudioPlayer.class.getSimpleName();
    private Context mContext;
    private MediaPlayer mPlayer;
    private AudioTrack mProgressTone;
    private final static int SAMPLE_RATE = 16000;
    AudioManager audioManager;

    public AudioPlayer(Context context)
    {
        this.mContext = context.getApplicationContext();
        this.audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
    }
    public void playRingtone()
    {
        // Honour silent mode
        switch (audioManager.getRingerMode())
        {
            case AudioManager.RINGER_MODE_NORMAL:
                mPlayer = new MediaPlayer();
                mPlayer.setAudioStreamType(AudioManager.STREAM_RING);
                try
                {
                    mPlayer.setDataSource(mContext,Uri.parse("android.resource://" + mContext.getPackageName() + "/" + R.raw.aler_call));
                    mPlayer.prepare();
                }
                catch (IOException e)
                {
                    System.out.println("the could not setup media player for ringtone 2 ");
                    mPlayer = null;
                    return;
                }
                mPlayer.setLooping(true);
                mPlayer.start();
                break;
        }
    }

    public void playRingtone2()
    {
        // Honour silent mode
        switch (audioManager.getRingerMode())
        {
            case AudioManager.RINGER_MODE_NORMAL:
                mPlayer = new MediaPlayer();
                mPlayer.setAudioStreamType(AudioManager.STREAM_RING);
                try
                {
                    mPlayer.setDataSource(mContext,Uri.parse("android.resource://" + mContext.getPackageName() + "/" + R.raw.beep_beep));
                    mPlayer.prepare();
                }
                catch (IOException e)
                {
                    System.out.println("the could not setup media player for ringtone 2 ");
                    mPlayer = null;
                    return;
                }
                mPlayer.setLooping(true);
                mPlayer.start();
                break;
        }
    }
    public void stopRingtone()
    {
        if (mPlayer != null)
        {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
    }

    public void playProgressTone()
    {
        stopProgressTone();
        try
        {
            mProgressTone = createProgressTone(mContext);
            mProgressTone.play();
        }
        catch (Exception e)
        {
             System.out.println("the 11 Could not play progress tone"+ e);
        }
    }

    public void stopProgressTone()
    {
        if (mProgressTone != null)
        {
            mProgressTone.stop();
            mProgressTone.release();
            mProgressTone = null;
        }
    }

    public void Mute()
    {
        audioManager.setMicrophoneMute(true);
        audioManager.setMode(AudioManager.ADJUST_MUTE);
    }

    public void UnMute()
    {
        audioManager.setMicrophoneMute(false);
        audioManager.setMode(AudioManager.ADJUST_UNMUTE);
    }

    public void AudouType(int i)
    {
        if(i==1)
            audioManager.setMode(AudioManager.MODE_IN_CALL);
        else if(i==2)
            audioManager.setMode(AudioManager.MODE_RINGTONE);
        else
            System.out.println("the else");
    }


    private static AudioTrack createProgressTone(Context context) throws IOException
    {
        AssetFileDescriptor fd = context.getResources().openRawResourceFd(R.raw.aler_call);
        int length = (int) fd.getLength();

        AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_VOICE_CALL, SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, length, AudioTrack.MODE_STATIC);

        byte[] data = new byte[length];
        readFileToBytes(fd, data);

        audioTrack.write(data, 0, data.length);
        audioTrack.setLoopPoints(0, data.length / 2, 30);

        return audioTrack;
    }

    private static void readFileToBytes(AssetFileDescriptor fd, byte[] data) throws IOException
    {
        FileInputStream inputStream = fd.createInputStream();
        int bytesRead = 0;
        while (bytesRead < data.length)
        {
            int res = inputStream.read(data, bytesRead, (data.length - bytesRead));
            if (res == -1)
            {
                break;
            }
            bytesRead += res;
        }
    }
}
