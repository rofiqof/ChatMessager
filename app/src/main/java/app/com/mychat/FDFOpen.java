package app.com.mychat;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class FDFOpen extends AppCompatActivity
{
    WebView web;
    ImageView back;
    String url1;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fdfopen);
        url1 = getIntent().getExtras().getString("pdf");
        System.out.println("the url is :" + url1);
        web = (WebView) findViewById(R.id.webView);

        back = (ImageView)findViewById(R.id.imgBack);
        back.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                onBackPressed();
            }
        });

        progressBar = (ProgressBar) findViewById(R.id.progressBar1);
        web.setWebViewClient(new myWebClient());
        web.getSettings().setJavaScriptEnabled(true);
        web.loadUrl("http://docs.google.com/gview?embedded=true&url="+url1);
    }

    public class myWebClient extends WebViewClient
    {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon)
        {
             super.onPageStarted(view, url, favicon);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url)
        {
            progressBar.setVisibility(View.VISIBLE);
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url)
        {
            super.onPageFinished(view, url);
            progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && web.canGoBack())
        {
            web.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}


