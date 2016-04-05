package org.theiner.kinoxscanner.adapter;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.theiner.kinoxscanner.R;
import org.theiner.kinoxscanner.data.VideoLink;

import java.util.Date;
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

        final VideoLink currentLink = getItem(position);

        TextView txtHosterName = (TextView) myView.findViewById(R.id.txtHosterName);
        txtHosterName.setText(currentLink.getHosterName());

        TextView txtVideoURL = (TextView) myView.findViewById(R.id.txtVideoURL);
        txtVideoURL.setText(currentLink.getVideoURL());

        ImageView imgDownload = (ImageView) myView.findViewById(R.id.imgDownload);

        final Context me = this.getContext();
        imgDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String filename = currentLink.getFilename() + " " + (new Date()).getTime() + ".mp4";

                Uri uri = Uri.parse(currentLink.getVideoURL());

                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).mkdirs();

                DownloadManager mgr = (DownloadManager) me.getSystemService(Context.DOWNLOAD_SERVICE);

                mgr.enqueue(new DownloadManager.Request(uri)
                    .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                    .setAllowedOverRoaming(false)
                    .setTitle("Kinoxscanner Download")
                    .setDescription(currentLink.getFilename())
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_MOVIES, filename));

                Toast.makeText(me, "Download gestartet: " + filename, Toast.LENGTH_SHORT).show();
            }
        });
        return myView;
    }
}
