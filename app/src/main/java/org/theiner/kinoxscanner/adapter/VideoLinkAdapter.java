package org.theiner.kinoxscanner.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.theiner.kinoxscanner.R;
import org.theiner.kinoxscanner.data.SearchResult;
import org.theiner.kinoxscanner.data.VideoLink;

import java.util.List;

/**
 * Created by TTheiner on 08.03.2016.
 */
public class VideoLinkAdapter extends ArrayAdapter<VideoLink> {

    public VideoLinkAdapter(Context context, List<VideoLink> videoLinks) {
        super(context, R.layout.videolink_row_layout, videoLinks);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());

        View myView = inflater.inflate(R.layout.videolink_row_layout, parent, false);

        VideoLink currentLink = getItem(position);

        TextView txtHosterName = (TextView) myView.findViewById(R.id.txtHosterName);
        txtHosterName.setText(currentLink.getHosterName());

        TextView txtVideoURL = (TextView) myView.findViewById(R.id.txtVideoURL);
        txtVideoURL.setText(currentLink.getVideoURL());

        return myView;
    }
}
