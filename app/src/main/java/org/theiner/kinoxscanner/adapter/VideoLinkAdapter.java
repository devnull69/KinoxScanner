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
import org.theiner.kinoxscanner.util.KinoxHelper;

import java.util.Date;
import java.util.List;

/**
 * Created by TTheiner on 08.03.2016.
 */
public class VideoLinkAdapter extends ArrayAdapter<VideoLink> {

    private int[] colors = new int[] { 0x50424242, 0x50212121 };

    private boolean isWifiOnly = true;

    public VideoLinkAdapter(Context context, List<VideoLink> videoLinks, boolean isWifiOnly) {
        super(context, R.layout.videolink_row_layout, videoLinks);
        this.isWifiOnly = isWifiOnly;
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
                if(!isWifiOnly || KinoxHelper.isConnectedViaWifi(me)) {
                    String filename = currentLink.getFilename() + " " + (new Date()).getTime() + currentLink.getExtensionFromMimeType();

                    Uri uri = Uri.parse(currentLink.getVideoURL());

                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).mkdirs();

                    DownloadManager mgr = (DownloadManager) me.getSystemService(Context.DOWNLOAD_SERVICE);

                    int flags = DownloadManager.Request.NETWORK_WIFI;
                    if(!isWifiOnly)
                        flags |= DownloadManager.Request.NETWORK_MOBILE;

                    mgr.enqueue(new DownloadManager.Request(uri)
                            .setAllowedNetworkTypes(flags)
                            .setAllowedOverRoaming(false)
                            .setTitle(getContext().getString(R.string.HeaderNotifyDownload))
                            .setDescription(currentLink.getFilename())
                            .setMimeType(currentLink.getMimeType())
                            .setDestinationInExternalPublicDir(Environment.DIRECTORY_MOVIES, filename));

                    Toast.makeText(me, me.getString(R.string.DownloadStarted) + filename, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(me, R.string.NoWifiConnection, Toast.LENGTH_SHORT).show();
                }
            }
        });

        int colorPos = position % colors.length;
        myView.setBackgroundColor(colors[colorPos]);

        return myView;
    }
}
