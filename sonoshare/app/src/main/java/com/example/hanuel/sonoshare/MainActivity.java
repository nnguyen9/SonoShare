package com.example.hanuel.sonoshare;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
        searchBar.setInputType(InputType.TYPE_CLASS_TEXT);

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
    public class JSONAsyncTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            keyword = searchBar.getText().toString();
            jsonURL = "http://api.soundcloud.com/tracks?q=" +
                    keyword.replaceAll(" ", "%20") + "&client_id=" + CLIENT_ID + "&format=json";
            Log.i("MainActivity:JsonURL", jsonURL);
        }
        @Override
        protected String doInBackground(Void... params) {
            jsonData = getJson(jsonURL);
            JSONArray jsonArray = null;
            keyword = keyword.toLowerCase();
            String[] keywordSplited = keyword.split("\\s+");
            try {
                jsonArray = new JSONArray(jsonData);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            for (int i = 0; i < jsonArray.length(); i++) {
                try {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String trackTitle = jsonObject.getString("title").toLowerCase();
                    if (trackTitle.contains("cover") || trackTitle.contains("parody")) {
                        continue;
                    }
                    Log.i("Originial track title:", trackTitle);
                    for (int j = 0; j < keywordSplited.length; j++) {
                        trackTitle = trackTitle.replaceAll(keywordSplited[j], "");
                    }
                    Log.i("true length:", trackTitle.length() + "");
                    if (trackTitle.replaceAll("\\s+", "").length() <= 5) {
                        return jsonObject.getString("stream_url");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            return null;
        }
        @Override
        protected void onPostExecute(String streamId) {
            if (streamId == null) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Search and Add Failed")
                        .setMessage("Please search and add with another keyword. Track does not exist on SoundCloud!")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int width) {
                                //Do nothing
                            }
                        }).setIcon(android.R.drawable.ic_dialog_alert).show();
            }
            else {
                String streamURL = streamId + "?client_id=" + CLIENT_ID;
                //Log.i("MainActivity:streamID", streamId + "?client_id=" + CLIENT_ID);
                //webView.loadUrl(streamId + "?client_id=" + CLIENT_ID);
                MediaPlayer mediaPlayer = new MediaPlayer();
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                try {
                    mediaPlayer.setDataSource(streamURL);
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }
}
