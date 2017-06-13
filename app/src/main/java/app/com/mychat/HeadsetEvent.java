package app.com.mychat;
public class HeadsetEvent
{

    public interface OnCustomStateListener
    {
        void stateChanged();
    }

    private static HeadsetEvent mInstance;
    private OnCustomStateListener mListener;
    private boolean mState;

    private HeadsetEvent()
    {}

    public static HeadsetEvent getInstance()
    {
        if(mInstance == null)
        {
            mInstance = new HeadsetEvent();
        }
        return mInstance;
    }

    public void setListener(OnCustomStateListener listener)
    {
        mListener = listener;
    }

    public void changeState(boolean state)
    {
        if(mListener != null)
        {
            mState = state;
            notifyStateChange();
        }
    }

    public boolean getState()
    {
        return mState;
    }

    private void notifyStateChange()
    {
        mListener.stateChanged();
    }
}
