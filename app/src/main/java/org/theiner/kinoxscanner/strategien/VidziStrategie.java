package org.theiner.kinoxscanner.strategien;

import org.theiner.kinoxscanner.util.HTTPHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by TTheiner on 16.03.2016.
 */
public class VidziStrategie extends HosterStrategie {

    public VidziStrategie() {
        this.hosterName = "Vidzi";
        this.hosterNummer = 68;
    }

    @Override
    public String getVideoURL(String hosterURL) {
        String response = "";
        // get MP4 filename from p.a.c.k.e.d javascript
        String vidziHTML = HTTPHelper.getHtmlFromUrl(hosterURL, true);

        Pattern pattern = Pattern.compile("(" + Pattern.quote("||http") + "[^']*)'");
        Matcher matcher = pattern.matcher(vidziHTML);

        if(matcher.find()) {
            String[] matches = matcher.group(1).split("\\|");

            // Scheme is: 2://40.39.38.37/80/v.79
            response = matches[2] + "://" + matches[40] + "." + matches[39] + "." + matches[38] + "." + matches[37] + "/" + matches[80] + "/v." + matches[79];
        }

        return response;
    }
}
