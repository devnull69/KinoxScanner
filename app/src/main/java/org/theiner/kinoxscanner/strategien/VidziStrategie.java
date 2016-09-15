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
        this.delayInSec = 0;
    }

    @Override
    public String getVideoURL(String hosterURL) throws InterruptedException{
        String response = "";
        // get MP4 filename from p.a.c.k.e.d javascript
        String vidziHTML = HTTPHelper.getHtmlFromUrl(hosterURL, "", true);

        Pattern pattern = Pattern.compile("(" + Pattern.quote("||") + "[^']*)'");
        Matcher matcher = pattern.matcher(vidziHTML);

        if(matcher.find()) {
            String[] matches = matcher.group(1).split("\\|");

            // Get scheme (find /v. and go back to previous double quote and forward to next double quote)
            int startpos = vidziHTML.indexOf("/v.");
            while(vidziHTML.charAt(startpos) != '"')
                startpos--;
            startpos++;
            int endpos = startpos;
            while(vidziHTML.charAt(endpos) != '"')
                endpos++;
            String scheme = vidziHTML.substring(startpos, endpos);

            response = HTTPHelper.unPACKED(matches, scheme, 36);
        }

        return response;
    }
}
