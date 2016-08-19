package org.theiner.kinoxscanner.strategien;

import android.net.Uri;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.theiner.kinoxscanner.util.HTTPHelper;


/**
 * Created by TTheiner on 16.03.2016.
 */
public class TheVideoMeStrategie extends HosterStrategie {

    private String referer;

    public TheVideoMeStrategie(String referer) {
        this.hosterName = "TheVideo.me";
        this.hosterNummer = 58;
        this.delayInSec = 0;
        this.referer = referer;
    }

    @Override
    public String getVideoURL(String hosterURL) throws InterruptedException{
        String result = "";

        String theVideoMeHtml = HTTPHelper.getHtmlFromUrl(hosterURL, referer, false);
        Document theVideoMeDoc = HTTPHelper.getDocumentFromHTML(theVideoMeHtml);

        String postString = "";

        String theName = "_vhash";
        int startpos = theVideoMeHtml.indexOf(theName) + 33;

        if(startpos>32) {
            int endpos = startpos + 10;
            String theValue = Uri.encode(theVideoMeHtml.substring(startpos, endpos));
            postString = theName + "=" + theValue;

            theName = "gfk";
            startpos = theVideoMeHtml.indexOf(theName) + 27;
            endpos = startpos + 10;
            theValue = Uri.encode(theVideoMeHtml.substring(startpos, endpos));
            postString += "&" + theName + "=" + theValue;

            Element theForm = theVideoMeDoc.getElementById("veriform");
            Elements theInputs = theForm.getElementsByTag("input");

            for (Element currentInput : theInputs) {

                theName = currentInput.attr("name");
                theValue = Uri.encode(currentInput.attr("value"));
                postString += "&" + theName + "=" + theValue;
            }

            postString += "&imhuman=";

            try {
                String response = HTTPHelper.getHtmlFromPOST(hosterURL, postString, false);
                int sourcesPos = response.indexOf("sources:");
                if(sourcesPos != -1) {
                    int fileStartPos = response.indexOf("file:", sourcesPos) + 7;
                    int fileEndPos = fileStartPos;
                    while (response.charAt(fileEndPos) != '\'')
                        fileEndPos++;
                    result = response.substring(fileStartPos, fileEndPos);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return result;
    }
}
