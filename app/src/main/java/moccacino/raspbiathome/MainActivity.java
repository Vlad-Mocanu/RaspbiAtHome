
package moccacino.raspbiathome;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.app.Activity;
import android.view.View;

public class MainActivity extends Activity implements Observer {

    private static final int RESULT_SETTINGS = 1;

    EditText etResponse;
    TextView tvIsConnected;
    Button updateButton;
    Button settingsButton;
    Spinner spinner;

    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean debugSwitch = sharedPref.getBoolean("pref_debug_switch", false);

        SharedPreferences.OnSharedPreferenceChangeListener listener =
                new SharedPreferences.OnSharedPreferenceChangeListener() {
                    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                        // listener implementation
                        Boolean debugSwitch = prefs.getBoolean("pref_debug_switch", false);
                        etResponse.setVisibility(debugSwitch ? View.VISIBLE : View.INVISIBLE);
                    }
                };
        sharedPref.registerOnSharedPreferenceChangeListener(listener);


        // get reference to the views
        etResponse = (EditText) findViewById(R.id.etResponse);
        etResponse.setVisibility(debugSwitch ? View.VISIBLE : View.INVISIBLE);

        tvIsConnected = (TextView) findViewById(R.id.tvIsConnected);
        updateButton = (Button) findViewById(R.id.updateButton);
        settingsButton = (Button) findViewById(R.id.settingsButton);
        spinner = (Spinner) findViewById(R.id.spinner);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.ips_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);


        // get the listview
        expListView = (ExpandableListView) findViewById(R.id.lvExp);
        // preparing list data
        listDataHeader = Arrays.asList(getResources().getStringArray(R.array.categories));
        listDataChild = new HashMap<String, List<String>>();
        initChildLists();

        listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);
        // setting list adapter
        expListView.setAdapter(listAdapter);

        updateButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                // check if you are connected or not
                if (isConnected()) {
                    tvIsConnected.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
                    tvIsConnected.setText("You are connected");
                    etResponse.setText("");
                } else {
                    tvIsConnected.setText("You are NOT connected");
                }

                initChildLists();

                // call AsynTask to perform network operation on separate thread
                String baseUrl = "http://" + spinner.getSelectedItem().toString() + "/";
                new HttpAsyncTask(MainActivity.this, "get_out_temp").execute(baseUrl);
                new HttpAsyncTask(MainActivity.this, "get_in_temp").execute(baseUrl);
                new HttpAsyncTask(MainActivity.this, "get_humidity").execute(baseUrl);
                new HttpAsyncTask(MainActivity.this, "get_pressure").execute(baseUrl);
                new HttpAsyncTask(MainActivity.this, "get_windows").execute(baseUrl);
            }
        });

        settingsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivityForResult(intent, RESULT_SETTINGS);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RESULT_SETTINGS:
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
                etResponse.setVisibility(sharedPref.getBoolean("pref_debug_switch", false) ? View.VISIBLE : View.INVISIBLE);
                break;
        }
    }

    public void initChildLists() {

        for (int i = 0; i <= listDataHeader.size() - 1; i++) {
            List<String> emptyList = new ArrayList<String>();
            listDataChild.put(listDataHeader.get(i), emptyList);
        }
    }

    public boolean isConnected() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }


    public void receiveUpdates(String title, HashMap<String, String> updates) {
        List<String> listToAppend = null;
        switch (title) {
            case "get_out_temp":
                listToAppend = listDataChild.get(listDataHeader.get(0));
                break;
            case "get_in_temp":
                listToAppend = listDataChild.get(listDataHeader.get(0));
                break;
            case "get_humidity":
                listToAppend = listDataChild.get(listDataHeader.get(1));
                break;
            case "get_pressure":
                listToAppend = listDataChild.get(listDataHeader.get(2));
                break;
            case "get_windows":
                listToAppend = listDataChild.get(listDataHeader.get(3));
                break;
            default:
                etResponse.append("\nERROR: unable to handle rest call: " + title + "\n");
                etResponse.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
                return;
        }

        String toPrintString = "";
        for (HashMap.Entry<String, String> entry : updates.entrySet()) {
            String pairString = entry.getKey() + ": " + entry.getValue() + "\n";
            if (!entry.getKey().equals("id"))
                toPrintString = toPrintString + pairString;
            etResponse.append(pairString);
        }
        etResponse.append("\n");
        //remove trailing \n
        listToAppend.add(toPrintString.substring(0, toPrintString.length() - 1));
    }


}