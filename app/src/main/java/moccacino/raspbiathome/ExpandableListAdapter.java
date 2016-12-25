package moccacino.raspbiathome;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ExpandableListAdapter extends BaseExpandableListAdapter {

    public List<String> _listDataHeaderSummary;
    private Context _context;
    private List<String> _listDataHeader; // header titles
    // child data in format of header title, child title
    private HashMap<String, List<String>> _listDataChild;

    public ExpandableListAdapter(Context context, List<String> listDataHeader, List<List<String>> listAllDataHeaderSummary,
                                 HashMap<String, List<String>> listChildData) {
        this._context = context;
        this._listDataHeader = listDataHeader;

        this._listDataHeaderSummary = new ArrayList<String>();
        for (int i = 0; i <= listAllDataHeaderSummary.size() - 1; i++) {
            String mySummaryString = "";
            for (int j = 0; j <= listAllDataHeaderSummary.get(i).size() - 1; j++)
                mySummaryString += listAllDataHeaderSummary.get(i).get(j);
            this._listDataHeaderSummary.add(mySummaryString);
        }
        this._listDataChild = listChildData;
    }

    //concatenates listDataHeaderSummary and sets the result at position from _listDataHeaderSummary
    public void setHeaderSummary(int position, List<String> listDataHeaderSummary, boolean append) {
        String mySummaryString = "";
        for (int j = 0; j <= listDataHeaderSummary.size() - 1; j++)
            mySummaryString += listDataHeaderSummary.get(j);
        if (append)
            this._listDataHeaderSummary.set(position, this._listDataHeaderSummary.get(position) + mySummaryString);
        else
            this._listDataHeaderSummary.set(position, mySummaryString);
    }

    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition))
                .get(childPosititon);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        final String childText = (String) getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_item, null);
        }

        TextView txtListChild = (TextView) convertView
                .findViewById(R.id.lblListItem);

        txtListChild.setText(childText);
        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition))
                .size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this._listDataHeader.get(groupPosition);
    }

    public Object getSummary(int groupPosition) {
        return this._listDataHeaderSummary.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this._listDataHeader.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);
        String headerSummary = (String) getSummary(groupPosition);

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_group, null);
        }

        TextView lblListHeader = (TextView) convertView
                .findViewById(R.id.lblListHeader);
        lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setText(headerTitle);

        TextView lblListHeaderSummary = (TextView) convertView
                .findViewById(R.id.lblListSummary);
        lblListHeaderSummary.setTypeface(null, Typeface.BOLD);
        lblListHeaderSummary.setText(headerSummary);

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
