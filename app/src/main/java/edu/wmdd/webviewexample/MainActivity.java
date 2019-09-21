package edu.wmdd.webviewexample;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONException;

import java.text.MessageFormat;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "OAUTH";
    private WebView mWebView;
    private String mToken;

    private String googleClientId = "107256413984-mtp3anc9m0ubj5sp4utd3qndledklfve.apps.googleusercontent.com";

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mWebView.loadUrl("https://google.ca");
                    return true;
                case R.id.navigation_dashboard:
                    mWebView.loadUrl("https://bing.ca");
                    return true;
                case R.id.navigation_notifications:
                    if (mToken == null) {
                        launchLoginTab();
                    } else {
                        String body = "access token is "+mToken;
                        String encodedBody = Base64.encodeToString(body.getBytes(), Base64.NO_PADDING);
                        mWebView.loadData(encodedBody,"text/plain", "base64");
                    }
                    return true;
            }
            return false;
        }
    };

    private void launchLoginTab() {
        String url = MessageFormat.format(
                "https://accounts.google.com/o/oauth2/v2/auth?client_id={0}&redirect_uri={1}&response_type={2}&scope={3}",
                googleClientId,
                "edu.wmdd.webviewexample:/",
                "code",
                "email profile");
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.launchUrl(this, Uri.parse(url));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        mWebView = findViewById(R.id.web_view);
        mWebView.setWebViewClient(new WebViewClient());
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        navView.setSelectedItemId(R.id.navigation_home);

        RequestQueue requestQueue = Volley.newRequestQueue(this);

        Log.d(TAG, getIntent().toString());
        Intent i = getIntent();

        if (i.getData() != null) {
            String code = i.getData().getQueryParameter("code");
            String tokenUrl = MessageFormat.format(
                    "https://oauth2.googleapis.com/token?code={0}&client_id={1}&redirect_uri={2}&grant_type={3}",
                    code,
                    googleClientId,
                    "edu.wmdd.webviewexample:/",
                    "authorization_code");

            String body = MessageFormat.format(
                    "code={0}&client_id={1}&redirect_uri={2}&grant_type={3}",
                    code,
                    googleClientId,
                    "edu.wmdd.webviewexample:/",
                    "authorization_code");

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, "https://oauth2.googleapis.com/token", response -> {
                Log.d(TAG, response.toString());
                try {
                    mToken = response.getString("access_token");
                    navView.setSelectedItemId(R.id.navigation_notifications);
                } catch (JSONException e) {
                    mToken = null;
                }
            }, error -> {
                Log.d(TAG, "error", error);
            }) {
                @Override
                public String getBodyContentType() {
                    return "application/x-www-form-urlencoded";
                }

                @Override
                public byte[] getBody() {
                    return body.getBytes();
                }
            };

            requestQueue.add(request);
        }
    }

}
