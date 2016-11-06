package moccacino.raspbiathome;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by moccacino on 01.11.2016.
 */

class HttpAsyncTask extends AsyncTask<String, Void, String> {

    Observer observer;
    String rest_call;

    public HttpAsyncTask(Observer my_observer, String my_rest_call) {
        observer = my_observer;
        rest_call = my_rest_call;
    }

    @Override
    protected String doInBackground(String[] url) {

        return GET(url[0] + rest_call);
    }

    public static String GET(String url) {
        InputStream inputStream = null;
        String result = "";
        try {

            // create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            // make GET request to the given URL
            HttpResponse httpResponse = httpclient.execute(new HttpGet(url));

            // receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // convert inputstream to string
            if (inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }

        return result;
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while ((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;
    }

    // onPostExecute displays the results of the AsyncTask.
    @Override
    protected void onPostExecute(String result) {
//        Toast.makeText(getBaseContext(), "Received!", Toast.LENGTH_SHORT).show();
        try {
            JSONArray jsonArray = new JSONArray(result);
            for (int i = 0; i <= jsonArray.length() - 1; i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                Iterator<String> keysIterator = jsonObject.keys();
                List<String> results = new ArrayList();
                HashMap<String, String> resultsHashMap = new HashMap<String, String>();
                while (keysIterator.hasNext()) {
                    String keyStr = keysIterator.next();
                    results.add(jsonObject.getString(keyStr));
                    resultsHashMap.put(keyStr, jsonObject.getString(keyStr));
                }

                observer.receiveUpdates(rest_call, resultsHashMap);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}