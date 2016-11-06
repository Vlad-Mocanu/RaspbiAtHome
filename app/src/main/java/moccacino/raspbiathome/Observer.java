package moccacino.raspbiathome;

import java.util.HashMap;

/**
 * Created by moccacino on 04.11.2016.
 */

interface Observer {
    void receiveUpdates(String title, HashMap<String, String> results);
}
