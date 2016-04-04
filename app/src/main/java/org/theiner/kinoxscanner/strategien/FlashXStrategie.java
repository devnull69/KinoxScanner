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
    public String getVideoURL(String hosterURL) throws InterruptedException{
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

        if(!"".equals(postString)) {
            // hart verdrahtet: FlashX misst 15 Sekunden zwischen beiden Seitenaufrufen
            Thread.sleep(16000);

            String response = HTTPHelper.getHtmlFromPOST(destURL, postString, false);
            int packedStartPos = response.indexOf("return p") + 11;
            if (packedStartPos > 10) {
                int packedEndPos = packedStartPos;
                int prevPos = packedEndPos - 1;
                while (response.charAt(packedEndPos) != '\'' || response.charAt(prevPos) == '\\') {
                    packedEndPos++;
                    prevPos++;
                }

                String packed = response.substring(packedStartPos, packedEndPos);

                int keysStartPos = response.indexOf("'||") + 1;
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
