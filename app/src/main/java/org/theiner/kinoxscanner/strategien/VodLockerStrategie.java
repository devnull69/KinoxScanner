package org.theiner.kinoxscanner.strategien;

import org.theiner.kinoxscanner.util.HTTPHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

/**
 * Created by TTheiner on 16.03.2016.
 */
public class VodLockerStrategie extends HosterStrategie {

    public VodLockerStrategie() {
        this.hosterName = "VodLocker";
        this.hosterNummer = 65;
    }

    @Override
    public String getVideoURL(String hosterURL) throws InterruptedException{
        String response = "";
        // get MP4 file link directly (with mobile browser setup)
        Document vodLockerDocument = HTTPHelper.getDocumentFromUrl(hosterURL, "", true);

        NodeList sources = vodLockerDocument.getElementsByTagName("source");
        if(sources.getLength()>0) {
            Element source = (Element) sources.item(0);

            NamedNodeMap attrMap = source.getAttributes();
            response = attrMap.getNamedItem("src").getNodeValue();

        }

        return response;
    }
}
