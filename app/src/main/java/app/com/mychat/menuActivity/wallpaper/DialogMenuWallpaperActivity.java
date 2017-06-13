package app.com.mychat.menuActivity.wallpaper;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import app.com.mychat.ChatRoomActivity;
import app.com.mychat.R;

public class DialogMenuWallpaperActivity extends Dialog implements View.OnClickListener {

    private TextView txtGalery;
    private TextView txtSolidColor;
    private TextView txtDefaultWallpaper;
    private TextView txtNoWallpaper;

    private Dialog dialog;
    private Activity activity;

    public DialogMenuWallpaperActivity(Activity activity) {
        super(activity);
        this.activity = activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog_menu_wallpaper);

        txtGalery = (TextView) findViewById(R.id.galery_wallpaper);
        txtSolidColor = (TextView) findViewById(R.id.solid_color_wallpaper);
        txtDefaultWallpaper = (TextView) findViewById(R.id.default_wallpaper);
        txtNoWallpaper = (TextView) findViewById(R.id.no_wallpaper);

        txtGalery.setOnClickListener(this);
        txtSolidColor.setOnClickListener(this);
        txtDefaultWallpaper.setOnClickListener(this);
        txtNoWallpaper.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.galery_wallpaper:

                dismiss();
                break;
            case R.id.solid_color_wallpaper:
                activity.startActivity(new Intent(activity, WallpaperActivity.class));
                dismiss();
                break;
            case R.id.default_wallpaper:
                Intent intentDefaul = new Intent(activity, ChatRoomActivity.class);
                intentDefaul.putExtra("wallpaper", "#FFF7F8");

                activity.startActivity(intentDefaul);
                dismiss();
                break;
            case R.id.no_wallpaper:
                Intent intentNoWalpaper = new Intent(activity, ChatRoomActivity.class);
                intentNoWalpaper.putExtra("wallpaper", "#FFFFFF");

                activity.startActivity(intentNoWalpaper);
                dismiss();
                break;
        }
        dismiss();
    }
}
