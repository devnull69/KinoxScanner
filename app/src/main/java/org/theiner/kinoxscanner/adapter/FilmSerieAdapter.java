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
import org.theiner.kinoxscanner.data.Film;
import org.theiner.kinoxscanner.data.FilmSerieWrapper;
import org.theiner.kinoxscanner.data.KinoxElement;
import org.theiner.kinoxscanner.data.SearchResult;
import org.theiner.kinoxscanner.data.Serie;
import org.theiner.kinoxscanner.util.ImageHelper;

import java.util.Comparator;
import java.util.List;

/**
 * Created by TTheiner on 08.03.2016.
 */
public class FilmSerieAdapter extends ArrayAdapter<FilmSerieWrapper> {

    private int[] colors = new int[] { 0x50424242, 0x50212121 };

    public FilmSerieAdapter(Context context, List<FilmSerieWrapper> kinoxElementWrappers) {
        super(context, R.layout.search_row_layout, kinoxElementWrappers);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());

        View myView = inflater.inflate(R.layout.filmserie_row_layout, parent, false);

        FilmSerieWrapper currentResult = (FilmSerieWrapper) getItem(position);

        TextView txtName = (TextView) myView.findViewById(R.id.txtName);
        txtName.setText(currentResult.getKinoxelement().toString());

        ImageView ivCoverArt = (ImageView) myView.findViewById(R.id.ivCoverArt);
        Bitmap coverArt = currentResult.getKinoxelement().imgFromCache();
        if(coverArt != null) {
            ivCoverArt.setImageBitmap(coverArt);
        } else {
            if(currentResult.getKinoxelement() instanceof Serie) {
                Serie currentSerie = (Serie) currentResult.getKinoxelement();
                if(!currentSerie.getImageSubDir().equals("") && !currentSerie.getAddr().equals(""))
                    ImageHelper.startGetImageTask(ivCoverArt, currentSerie.getImageSubDir(), currentSerie.getAddr());
            } else {
                Film currentFilm = (Film) currentResult.getKinoxelement();
                if(!currentFilm.getImageSubDir().equals("") && !currentFilm.getAddr().equals(""))
                    ImageHelper.startGetImageTask(ivCoverArt, currentFilm.getImageSubDir(), currentFilm.getAddr());
            }
        }


        int colorPos = position % colors.length;
        if(currentResult.isSelected())
            myView.setBackgroundColor(0x5098FF98);
        else
            myView.setBackgroundColor(colors[colorPos]);

        return myView;
    }

}
