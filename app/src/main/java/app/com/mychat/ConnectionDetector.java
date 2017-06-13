package app.com.mychat;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class ConnectionDetector
{
    Context _context;
    public ConnectionDetector(Context context)
    {
        this._context = context;
    }

    public boolean isConnectingToInternet()
    {
        try
        {
            ConnectivityManager connectivity = (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity != null) {
                NetworkInfo[] info = connectivity.getAllNetworkInfo();
                if (info != null)
                    for (int i = 0; i < info.length; i++)
                        if (info[i].getState() == NetworkInfo.State.CONNECTED)
                        {
                            return true;
                        }
            }
        }
        catch (Exception e)
        {
            System.out.println("the Exception :" + e);
        }
        return false;
    }
}