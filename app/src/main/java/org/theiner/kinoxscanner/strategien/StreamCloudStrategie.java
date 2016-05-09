package org.theiner.kinoxscanner.strategien;

import android.net.Uri;

import org.theiner.kinoxscanner.util.HTTPHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
        this.mimeType = "video/mp4";
    }

    @Override
    public String getVideoURL(String hosterURL) throws InterruptedException{
        String response = "";

        Document streamCloudDoc = HTTPHelper.getDocumentFromUrl(hosterURL, referer, false);

        String postString = "";

        NodeList theForms = streamCloudDoc.getElementsByTagName("form");
        if(theForms.getLength() > 0) {
            Element theForm = (Element) theForms.item(0);

            NodeList theInputs = theForm.getElementsByTagName("input");

            int counter = 0;
            for (int input = 0; input < theInputs.getLength(); input++) {
                Element currentInput = (Element) theInputs.item(input);

                NamedNodeMap theAttributes = currentInput.getAttributes();
                Node namedItem = theAttributes.getNamedItem("name");
                if (namedItem != null) {
                    String theName = namedItem.getNodeValue();
                    String theValue = Uri.encode(theAttributes.getNamedItem("value").getNodeValue());

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
