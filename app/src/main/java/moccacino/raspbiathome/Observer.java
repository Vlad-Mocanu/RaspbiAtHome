package moccacino.raspbiathome;

import java.util.ArrayList;

/**
 * Created by moccacino on 04.11.2016.
 */

interface Observer {
    void receiveUpdates(String title, ArrayList<String> updates);
}
