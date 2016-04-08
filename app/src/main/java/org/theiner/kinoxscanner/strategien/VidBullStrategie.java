package org.theiner.kinoxscanner.strategien;

import org.theiner.kinoxscanner.util.HTTPHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

/**
 * Created by TTheiner on 16.03.2016.
 */
public class VidBullStrategie extends HosterStrategie {

    public VidBullStrategie() {
        this.hosterName = "VidBull";
        this.hosterNummer = 50;
        this.delayInSec = 0;
    }

    @Override
    public String getVideoURL(String hosterURL) throws InterruptedException{
        String response = "";
        // get MP4 file link directly (with mobile browser setup)
        Document vidBullDocument = HTTPHelper.getDocumentFromUrl(hosterURL, "", true);

        NodeList sources = vidBullDocument.getElementsByTagName("source");
        if(sources.getLength()>0) {
            Element source = (Element) sources.item(0);

            NamedNodeMap attrMap = source.getAttributes();
            response = attrMap.getNamedItem("src").getNodeValue();

        }

        return response;
    }
}
