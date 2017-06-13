package app.com.mychat.menuActivity.wallpaper;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import app.com.mychat.R;

/**
 * Created by rofiqoff on 5/24/17.
 */

public class AdapterWallpaper extends BaseAdapter {

    private Context mContext;

    public AdapterWallpaper(Context context){
        mContext = context;
    }

    @Override
    public int getCount() {
        return mThumbIds.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ImageView imageView;
        if (convertView == null){
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);
        } else {
            imageView = (ImageView) convertView;
        }

        imageView.setImageResource(mThumbIds[position]);

        return imageView;
    }

    private Integer[] mThumbIds = {
            R.drawable.colorblueshade,
            R.drawable.colordiamond,
            R.drawable.colorpink,
            R.drawable.colorred,
            R.drawable.coloryellowiphone
    };
}
