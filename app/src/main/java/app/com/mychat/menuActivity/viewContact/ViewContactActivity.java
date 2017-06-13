package app.com.mychat.menuActivity.viewContact;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import app.com.mychat.R;

public class ViewContactActivity extends AppCompatActivity {

    private ImageView imageContact;
    private TextView phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_contact);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle bundle = getIntent().getExtras();

        String sNameContact = bundle.getString("name");
        String sPhoneNumber = bundle.getString("number");
        String sImage = bundle.getString("image");

        imageContact = (ImageView) findViewById(R.id.image_contact);
        phoneNumber = (TextView) findViewById(R.id.text_phone_number);

        getSupportActionBar().setTitle(sNameContact);

        Picasso.with(this)
                .load(sImage)
                .placeholder(R.drawable.img_pic)
                .into(imageContact);

        phoneNumber.setText(sPhoneNumber);
    }
}
