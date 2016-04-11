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
public class HosterAdapter extends ArrayAdapter<HosterMirror> {

    private int[] colors = new int[] { 0x50424242, 0x50212121 };

    public HosterAdapter(Context context, List<HosterMirror> hosterMirrors) {
        super(context, R.layout.hostermirror_row_layout, hosterMirrors);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());

        View myView = inflater.inflate(R.layout.hostermirror_row_layout, parent, false);

        HosterMirror currentHoster = getItem(position);

        TextView txtHosterName = (TextView) myView.findViewById(R.id.txtHosterName);
        txtHosterName.setText(currentHoster.getStrategie().hosterName);

        TextView txtMirrorCount = (TextView) myView.findViewById(R.id.txtMirrorCount);
        txtMirrorCount.setText(currentHoster.getMirrorCount() + " Server");

        TextView txtHosterDate = (TextView) myView.findViewById(R.id.txtHosterDate);
        txtHosterDate.setText("Datum: " + currentHoster.getHosterdate());

        int colorPos = position % colors.length;
        myView.setBackgroundColor(colors[colorPos]);

        return myView;
    }
}
