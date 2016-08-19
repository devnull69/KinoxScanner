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
public class VidToMeStrategie extends HosterStrategie {

    private String referer;

    public VidToMeStrategie(String referer) {
        this.hosterName = "VidToMe";
        this.hosterNummer = 51;
        this.delayInSec = 7;
        this.referer = referer;
    }

    @Override
    public String getVideoURL(String hosterURL) throws InterruptedException{
        String response = "";

        Document vidToMeDoc = HTTPHelper.getDocumentFromUrl(hosterURL, referer, false);

        String postString = "";

        Elements theForms = vidToMeDoc.getElementsByTag("form");
        if(theForms.size() > 1) {
            Element theForm = theForms.get(1);

            Elements theInputs = theForm.getElementsByTag("input");

            int counter = 0;
            for (Element currentInput : theInputs) {

                String theName = currentInput.attr("name");
                if (theName != null && !"".equals(theName)) {
                    String theValue = Uri.encode(currentInput.attr("value"));

                    postString += (counter > 0 ? "&" : "") + theName + "=" + theValue;
                    counter++;
                }
            }

                Thread.sleep(delayInSec * 1000);
                String videoHtml = HTTPHelper.getHtmlFromPOST(hosterURL, postString, false);

                int packedStartPos = videoHtml.indexOf("return p") + 11;
                if(packedStartPos > 10) {
                    int packedEndPos = packedStartPos;
                    int prevPos = packedEndPos - 1;
                    while (videoHtml.charAt(packedEndPos) != '\'' || videoHtml.charAt(prevPos) == '\\') {
                        packedEndPos++;
                        prevPos++;
                    }

                    String packed = videoHtml.substring(packedStartPos, packedEndPos);

                    int keysStartPos = videoHtml.indexOf("'||") + 1;
                    int keysEndPos = keysStartPos;
                    while(videoHtml.charAt(keysEndPos) != '\'')
                        keysEndPos++;

                    String keys = videoHtml.substring(keysStartPos, keysEndPos);

                    String[] matches = keys.split("\\|");

                    String unpacked = HTTPHelper.unPACKED(matches, packed, 36);

                    int responseStartPos = unpacked.indexOf("360p") + 12;
                    int responseEndPos = responseStartPos;
                    while(unpacked.charAt(responseEndPos) != '"')
                        responseEndPos++;

                    response = unpacked.substring(responseStartPos, responseEndPos);
                }

        }

        return response;
    }
}
