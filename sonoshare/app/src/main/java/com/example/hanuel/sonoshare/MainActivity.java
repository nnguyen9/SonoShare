package com.example.hanuel.sonoshare;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class MainActivity extends ActionBarActivity {
    private WebView webView;
    private HttpURLConnection httpURLConnection;
    private EditText searchBar;
    private String keyword;
    private String jsonURL;
    private String jsonData;
    private final String CLIENT_ID = "3f7407c6252e6932c8ef6ea85c55e9c7";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = (WebView)findViewById(R.id.webView);
        searchBar = (EditText)findViewById(R.id.searchBar);

        WebSettings webViewSettings = webView.getSettings();
        webViewSettings.setJavaScriptEnabled(true);
        webViewSettings.setAllowFileAccess(true);
        webViewSettings.setAllowContentAccess(true);
        webViewSettings.setAllowFileAccessFromFileURLs(true);
        webViewSettings.setAllowUniversalAccessFromFileURLs(true);
        webView.loadUrl("file:///android_asset/index.html");
        webView.addJavascriptInterface(new JavaScriptInterface(this), "Android");

    }
    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    public String getJson(String address) {
        String jsonString = "";

        try {
            URL url = new URL(address);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
            StringBuilder stringBuilder = new StringBuilder();

            String line = "";

            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line + '\n');
            }
            jsonString = stringBuilder.toString();
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
        }
        catch (ProtocolException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            httpURLConnection.disconnect();
        }
        return jsonString;
    }
    public void search(View v) {
        if (searchBar.getText().toString().equals("") || searchBar.getText().toString().length() <= 3) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Search keyword")
                    .setMessage("Please input a keyword that's longer than 3 characters")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int width) {
                            //Do nothing
                        }
                    }).setIcon(android.R.drawable.ic_dialog_alert).show();
        }
        else {
            JSONAsyncTask jsonTask = new JSONAsyncTask();
            jsonTask.execute();
            // Parse json here?
        }

    }
    public class JavaScriptInterface {
        Context context;
        JavaScriptInterface(Context context) {
            this.context = context;
        }
        @JavascriptInterface
        public void showPrint(String str) {
            // Later for streaming
        }
    }
    public class JSONAsyncTask extends AsyncTask<String, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            keyword = searchBar.getText().toString();
            jsonURL = "http://api.soundcloud.com/tracks?title=" +
                    keyword.replaceAll(" ", "%20") + "&client_id=" + CLIENT_ID + "&format=json";
            Log.i("MainActivity:JsonURL", jsonURL);
        }
        @Override
        protected Void doInBackground(String... params) {
            jsonData = getJson(jsonURL);
            return null;
        }
        @Override
        protected void onPostExecute(Void param) {
            Log.i("MainActivity:JSONData", jsonData);
        }
    }
}
