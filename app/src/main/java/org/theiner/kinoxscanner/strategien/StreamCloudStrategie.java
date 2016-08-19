package org.theiner.kinoxscanner.strategien;

import android.net.Uri;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.theiner.kinoxscanner.util.HTTPHelper;

/**
 * Created by TTheiner on 16.03.2016.
 */
public class StreamCloudStrategie extends HosterStrategie {

    private String referer;

    public StreamCloudStrategie(String referer) {
        this.hosterName = "StreamCloud";
        this.hosterNummer = 30;
        this.delayInSec = 11;
        this.referer = referer;
    }

    @Override
    public String getVideoURL(String hosterURL) throws InterruptedException{
        String response = "";

        Document streamCloudDoc = HTTPHelper.getDocumentFromUrl(hosterURL, referer, false);

        String postString = "";

        Elements theForms = streamCloudDoc.getElementsByTag("form");
        if(theForms.size() > 0) {
            Element theForm = theForms.get(0);

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

            int startpos = videoHtml.indexOf("file:") + 7;
            if (startpos > 6) {
                int endpos = startpos;
                while (videoHtml.charAt(endpos) != '"')
                    endpos++;
                response = videoHtml.substring(startpos, endpos);
            }

        }

        return response;
    }
}
