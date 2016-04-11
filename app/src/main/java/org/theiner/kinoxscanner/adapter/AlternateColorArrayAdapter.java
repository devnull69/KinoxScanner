package org.theiner.kinoxscanner.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.theiner.kinoxscanner.R;
import org.theiner.kinoxscanner.data.HosterMirror;

import java.util.List;

/**
 * Created by TTheiner on 08.03.2016.
 */
public class AlternateColorArrayAdapter<T> extends ArrayAdapter<T> {

    private int[] colors = new int[] { 0x50424242, 0x50212121 };

    public AlternateColorArrayAdapter(Context context, List<T> content) {
        super(context, R.layout.my_simple_list_item_1, content);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());

        View myView = inflater.inflate(R.layout.my_simple_list_item_1, parent, false);

        String currentContent = getItem(position).toString();

        TextView txtContent = (TextView) myView.findViewById(android.R.id.text1);
        txtContent.setText(currentContent);

        int colorPos = position % colors.length;
        myView.setBackgroundColor(colors[colorPos]);

        return myView;
    }
}
