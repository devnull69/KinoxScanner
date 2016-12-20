package org.theiner.kinoxscanner.strategien;

import android.net.Uri;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.theiner.kinoxscanner.util.HTTPHelper;

/**
 * Created by TTheiner on 16.03.2016.
 */
public class FlashXStrategie extends HosterStrategie {

    private String referer;

    public FlashXStrategie(String referer) {
        this.hosterName = "FlashX";
        this.hosterNummer = 33;
        this.delayInSec = 8;
        this.referer = referer;
    }

    @Override
    public String getVideoURL(String hosterURL) throws InterruptedException{
        String result = "";

        // FlashX switched to HTTPS so let's consider that
        if(!hosterURL.contains("https"))
            hosterURL = hosterURL.replace("http", "https");

        String flashXHtml = HTTPHelper.getHtmlFromUrl(hosterURL, referer, false);
        Document flashXDoc = HTTPHelper.getDocumentFromHTML(flashXHtml);

        String postString = "";

        Element theForm = flashXDoc.getElementsByTag("form").get(0);

        String destURL = theForm.attr("abs:action");

        Elements theInputs = theForm.getElementsByTag("input");

        for(Element currentInput : theInputs) {

            String theName = currentInput.attr("name");
            String theValue = Uri.encode(currentInput.attr("value"));
            postString += (theInputs.indexOf(currentInput)>0?"&":"") + theName + "=" + theValue;
        }

        if(!"".equals(postString)) {
            // hart verdrahtet: FlashX misst 15 Sekunden zwischen beiden Seitenaufrufen
            Thread.sleep(delayInSec * 1000);

            String response = HTTPHelper.getHtmlFromPOST(destURL, postString, false);
            int packedStartPos = response.indexOf("return p}") + 11;
            // Bugfix: FlashX now has more than one p.a.c.k.e.d area, from which we need the second
            packedStartPos = response.indexOf("return p}", packedStartPos) + 11;
            if (packedStartPos > 10) {
                int packedEndPos = packedStartPos;
                int prevPos = packedEndPos - 1;
                while (response.charAt(packedEndPos) != '\'' || response.charAt(prevPos) == '\\') {
                    packedEndPos++;
                    prevPos++;
                }

                String packed = response.substring(packedStartPos, packedEndPos);

                int keysStartPos = response.indexOf("'||") + 1;
                // Bugfix, we need the second p.a.c.k.e.d area
                keysStartPos = response.indexOf("'||", keysStartPos) + 1;
                int keysEndPos = keysStartPos;
                while (response.charAt(keysEndPos) != '\'')
                    keysEndPos++;

                String keys = response.substring(keysStartPos, keysEndPos);

                String[] matches = keys.split("\\|");

                String unpacked = HTTPHelper.unPACKED(matches, packed, 36);

                int responseStartPos = unpacked.indexOf("file") + 6;
                int responseEndPos = responseStartPos;
                while (unpacked.charAt(responseEndPos) != '"')
                    responseEndPos++;

                result = unpacked.substring(responseStartPos, responseEndPos);
            }
        }

        return result;
    }
}
