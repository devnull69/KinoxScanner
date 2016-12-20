package org.theiner.kinoxscanner.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.theiner.kinoxscanner.R;
import org.theiner.kinoxscanner.data.CheckErgebnis;
import org.theiner.kinoxscanner.data.Film;
import org.theiner.kinoxscanner.data.KinoxElement;
import org.theiner.kinoxscanner.data.Serie;
import org.theiner.kinoxscanner.util.ImageHelper;

import java.util.List;

/**
 * Created by TTheiner on 08.03.2016.
 */
public class CheckErgebnisAdapter extends ArrayAdapter<CheckErgebnis> {

    private int[] colors = new int[] { 0x50424242, 0x50212121 };

    public CheckErgebnisAdapter(Context context, List<CheckErgebnis> checkErgebnisse) {
        super(context, R.layout.checkergebnis_row_layout, checkErgebnisse);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = LayoutInflater.from(getContext());

        View myView = inflater.inflate(R.layout.checkergebnis_row_layout, parent, false);

        CheckErgebnis currentResult = getItem(position);

        TextView txtName = (TextView) myView.findViewById(R.id.txtName);
        txtName.setText(currentResult.toString());

        TextView txtImdbRating = (TextView) myView.findViewById(R.id.txtImdbRating);
        txtImdbRating.setText(currentResult.foundElement.getImdbRating());

        ImageView ivCoverArt = (ImageView) myView.findViewById(R.id.ivCoverArt);
        Bitmap coverArt = currentResult.foundElement.imgFromCache();
        if(coverArt != null) {
            ivCoverArt.setImageBitmap(coverArt);
        } else {
            if(currentResult.foundElement instanceof Serie) {
                Serie currentSerie = (Serie) currentResult.foundElement;
                if(!currentSerie.getImageSubDir().equals("") && !currentSerie.getAddr().equals(""))
                    ImageHelper.startGetImageTask(ivCoverArt, currentSerie.getImageSubDir(), currentSerie.getAddr());
            } else {
                Film currentFilm = (Film) currentResult.foundElement;
                if(!currentFilm.getImageSubDir().equals("") && !currentFilm.getAddr().equals(""))
                    ImageHelper.startGetImageTask(ivCoverArt, currentFilm.getImageSubDir(), currentFilm.getAddr());
            }
        }

        int colorPos = position % colors.length;
        myView.setBackgroundColor(colors[colorPos]);

        return myView;
    }
}
