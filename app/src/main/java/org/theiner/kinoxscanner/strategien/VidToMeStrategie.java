package org.theiner.kinoxscanner.strategien;

import android.net.Uri;

import org.theiner.kinoxscanner.util.HTTPHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
        this.referer = referer;
    }

    @Override
    public String getVideoURL(String hosterURL) throws InterruptedException{
        String response = "";

        Document vidToMeDoc = HTTPHelper.getDocumentFromUrl(hosterURL, referer, false);

        String postString = "";

        NodeList theForms = vidToMeDoc.getElementsByTagName("form");
        if(theForms.getLength() > 1) {
            Element theForm = (Element) theForms.item(1);

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

                Thread.sleep(7000);
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
