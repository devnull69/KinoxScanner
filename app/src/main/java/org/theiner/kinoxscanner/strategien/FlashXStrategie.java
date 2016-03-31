package org.theiner.kinoxscanner.strategien;

import android.net.Uri;

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
public class FlashXStrategie extends HosterStrategie {

    private String referer;

    public FlashXStrategie(String referer) {
        this.hosterName = "FlashX";
        this.hosterNummer = 33;
        this.referer = referer;
    }

    @Override
    public String getVideoURL(String hosterURL) {
        String result = "";

        String flashXHtml = HTTPHelper.getHtmlFromUrl(hosterURL, referer, false);
        Document flashXDoc = HTTPHelper.getDocumentFromHTML(flashXHtml);

        String postString = "";

        Element theForm = (Element) flashXDoc.getElementsByTagName("form").item(0);

        NamedNodeMap formAttributes = theForm.getAttributes();
        String destURL = formAttributes.getNamedItem("action").getNodeValue();

        NodeList theInputs = theForm.getElementsByTagName("input");

        for(int input=0; input<theInputs.getLength(); input++) {
            Element currentInput = (Element) theInputs.item(input);

            NamedNodeMap theAttributes = currentInput.getAttributes();
            String theName = theAttributes.getNamedItem("name").getNodeValue();
            String theValue = Uri.encode(theAttributes.getNamedItem("value").getNodeValue());
            postString += (input>0?"&":"") + theName + "=" + theValue;
        }

        try {

            // hart verdrahtet: FlashX misst 15 Sekunden zwischen beiden Seitenaufrufen
            Thread.sleep(16000);

            String response = HTTPHelper.getHtmlFromPOST(destURL, postString, false);
            // PACKED
            int schemeStartPos = response.indexOf("2://");
            if(schemeStartPos != -1) {
                int schemeEndPos = schemeStartPos;
                while (response.charAt(schemeEndPos) != '"')
                    schemeEndPos++;

                String scheme = response.substring(schemeStartPos, schemeEndPos);

                Pattern pattern = Pattern.compile("(" + Pattern.quote("||http") + "[^']*)'");
                Matcher matcher = pattern.matcher(response);

                if(matcher.find()) {
                    String[] matches = matcher.group(1).split("\\|");

                    result = HTTPHelper.unPACKED(matches, scheme, 36);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}
