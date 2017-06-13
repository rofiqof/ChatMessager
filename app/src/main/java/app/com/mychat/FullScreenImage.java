package app.com.mychat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.squareup.picasso.Picasso;

/**
 * Created by isquare3 on 12/23/16.
 */

public class FullScreenImage extends Activity
{
    ImageView back;
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.full_image);
        back = (ImageView)findViewById(R.id.imgBack);
        Intent intent = getIntent();
        String imageId = intent.getExtras().getString("image");
         String th = intent.getExtras().getString("thumb");
        System.out.println("the image is :"+ imageId);
         System.out.println("the thumb is :"+ th);
        TouchImageView imageView = (TouchImageView) findViewById(R.id.imageView1);


       Glide.with(getApplicationContext()).load(imageId)
                .thumbnail(0.5f)
                .crossFade()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.image_show)
                .into(imageView);

          //Picasso.with(getApplicationContext()).load(imageId).placeholder(R.drawable.img_pic).into(imageView);

      /*  Glide.with(getApplicationContext()).load(imageId)
                .thumbnail(0.5f)
                .crossFade()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.image_show)
                .into(imageView);
*/
        back.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onBackPressed();
            }
        });

    }
}