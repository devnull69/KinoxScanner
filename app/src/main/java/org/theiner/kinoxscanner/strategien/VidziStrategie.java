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

            // Get scheme (find v. and go back 19 positions with length 23)
            int startpos = vidziHTML.indexOf("v.") - 19;
            String scheme = vidziHTML.substring(startpos, startpos+23);

            // Get positions from scheme
            Pattern ptnPositions = Pattern.compile("\\b([a-z0-9]{1,2})\\b");
            Matcher posMatcher = ptnPositions.matcher(scheme);
            String[] positions = new String[8];
            int current = 0;
            while(posMatcher.find()) {
                positions[current] = matches[getIntFromHexaTridecimal(posMatcher.group(1))];
                if("".equals(positions[current]))
                    positions[current] = posMatcher.group(1);
                current++;
            }

            // Scheme is: 0://1.2.3.4/5/6.7
            response = positions[0] + "://" + positions[1] + "." + positions[2] + "." + positions[3] + "." + positions[4] + "/" + positions[5] + "/" + positions[6] + "." + positions[7];
        }

        return response;
    }

    private int getIntFromHexaTridecimal(String hextri) {
        int result = 0;
        if(hextri.length() > 1) {
            String first = hextri.substring(0, 1);
            hextri = hextri.substring(1);
            result = getIntFromHexaTridecimalDigit(first) * 36;
        }
        result = result + getIntFromHexaTridecimalDigit(hextri);
        return result;
    }

    private int getIntFromHexaTridecimalDigit(String digit) {
        int result = 0;
        Pattern pattern = Pattern.compile("[a-z]");
        Matcher matcher = pattern.matcher(digit);
        if(matcher.find()) {
            // alphabetic
            result = ((int) digit.charAt(0)) - 87;
        } else {
            // numeric
            result = Integer.parseInt(digit);
        }
        return result;
    }
}
