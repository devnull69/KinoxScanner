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

import java.util.List;

/**
 * Created by TTheiner on 08.03.2016.
 */
public class SearchAdapter extends ArrayAdapter<SearchResult> {

    public SearchAdapter(Context context, List<SearchResult> searchResults) {
        super(context, R.layout.search_row_layout, searchResults);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());

        View myView = inflater.inflate(R.layout.search_row_layout, parent, false);

        SearchResult currentResult = getItem(position);

        TextView txtName = (TextView) myView.findViewById(R.id.txtName);
        txtName.setText(currentResult.getName());

        ImageView ivLanguage = (ImageView) myView.findViewById(R.id.ivLanguage);
        int languageCode = currentResult.getLanguageCode();
        switch(languageCode) {
            case(1):
            case(2):
            case(15):
                Context context = ivLanguage.getContext();
                int id = context.getResources().getIdentifier("l" + String.valueOf(languageCode), "mipmap", context.getPackageName());
                ivLanguage.setImageResource(id);
                break;
            default:
                ivLanguage.setImageResource(R.mipmap.unknown_language);
                break;

        }

        return myView;
    }
}
