package org.theiner.kinoxscanner.strategien;

import android.net.Uri;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.theiner.kinoxscanner.util.HTTPHelper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by TTheiner on 16.03.2016.
 */
public class LetWatchToStrategie extends HosterStrategie {

    private String referer;

    public LetWatchToStrategie() {
        this.hosterName = "LetWatch.to";
        this.hosterNummer = 62;
        this.delayInSec = 0;
        //this.referer = referer;
    }

    @Override
    public String getVideoURL(String hosterURL) throws InterruptedException{
        String result = "";

        String letWatchToHtml = HTTPHelper.getHtmlFromUrl(hosterURL, "", false);

        int startPos = letWatchToHtml.indexOf("sources") + 17;
        if(startPos > 16) {
            int endPos = startPos + 1;
            while(letWatchToHtml.charAt(endPos) != '"')
                endPos++;

            result = letWatchToHtml.substring(startPos, endPos);
        }

        return result;
    }
}
