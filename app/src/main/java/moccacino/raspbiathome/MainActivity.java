
package moccacino.raspbiathome;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.app.Activity;
import android.view.View;


public class MainActivity extends Activity implements Observer {

    EditText etResponse;
    TextView tvIsConnected;
    Button updateButton;
    Spinner spinner;

    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // get reference to the views
        etResponse = (EditText) findViewById(R.id.etResponse);
        tvIsConnected = (TextView) findViewById(R.id.tvIsConnected);
        updateButton = (Button) findViewById(R.id.updateButton);
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
        List<String> emptyList = new ArrayList<String>();
        emptyList.add("<This section is empty>");
        for (int i = 0; i <= listDataHeader.size() - 1; i++)
            listDataChild.put(listDataHeader.get(i), emptyList);

        listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);
        // setting list adapter
        expListView.setAdapter(listAdapter);

        updateButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                // check if you are connected or not
                if (isConnected()) {
                    tvIsConnected.setBackgroundColor(0xFF00CC00);
                    tvIsConnected.setText("You are connected");
                    etResponse.setText("");
                } else {
                    tvIsConnected.setText("You are NOT connected");
                }

                // call AsynTask to perform network operation on separate thread
                String baseUrl = "http://" + spinner.getSelectedItem().toString() + "/";
                new HttpAsyncTask(MainActivity.this, "get_out_temp").execute(baseUrl);
                new HttpAsyncTask(MainActivity.this, "get_in_temp").execute(baseUrl);
                new HttpAsyncTask(MainActivity.this, "get_humidity").execute(baseUrl);
                new HttpAsyncTask(MainActivity.this, "get_pressure").execute(baseUrl);
                new HttpAsyncTask(MainActivity.this, "get_windows").execute(baseUrl);
            }
        });

    }

    public boolean isConnected() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    public void receiveUpdates(String title, ArrayList<String> updates) {
        for (String s : updates) {
            etResponse.append(s + "\n");
        }

        List<String> childrenList = new ArrayList<String>();
        for (String s : updates) {
            childrenList.add(s);
        }

        switch (title) {
            case "get_out_temp":
                listDataChild.put(listDataHeader.get(0), childrenList);
                break;
            case "get_humidity":
                listDataChild.put(listDataHeader.get(1), childrenList);
                break;
            case "get_pressure":
                listDataChild.put(listDataHeader.get(2), childrenList);
                break;
            case "get_windows":
                listDataChild.put(listDataHeader.get(3), childrenList);
                break;
        }
    }

}