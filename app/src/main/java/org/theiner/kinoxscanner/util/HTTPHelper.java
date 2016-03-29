package org.theiner.kinoxscanner.util;

import org.apache.xalan.xsltc.trax.SAX2DOM;
import org.ccil.cowan.tagsoup.Parser;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;

/**
 * Created by TTheiner on 10.03.2016.
 */
public class HTTPHelper {

    private static final String userAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.116 Safari/537.36";
    private static final String userAgentMobile = "Mozilla/5.0 (Linux; Android 5.0.1; Galaxy Nexus Build/IMM76B) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.133 Mobile Safari/535.19";

    public static Document getDocumentFromUrl(String strUrl, boolean isMobile) {
        Parser p = new Parser();
        SAX2DOM sax2dom = null;
        Document doc  = null;

        try {

            URL url = new URL(strUrl);
            URLConnection con = url.openConnection();

            // force server to mimic specific Browser
            con.setRequestProperty("User-Agent", userAgent);
            if(isMobile)
                con.setRequestProperty("User-Agent", userAgentMobile);

            con.setReadTimeout(15000);
            con.connect();

            p.setFeature(Parser.namespacesFeature, false);
            p.setFeature(Parser.namespacePrefixesFeature, false);
            sax2dom = new SAX2DOM();
            p.setContentHandler(sax2dom);
            p.parse(new InputSource(new InputStreamReader(con.getInputStream())));

            doc = (Document) sax2dom.getDOM();
        } catch (Exception e) {
            // TODO handle exception
        }


        return doc;
    }

    public static Document getDocumentFromHTML(String html) {
        Parser p = new Parser();
        SAX2DOM sax2dom = null;
        Document doc  = null;

        try {

            p.setFeature(Parser.namespacesFeature, false);
            p.setFeature(Parser.namespacePrefixesFeature, false);
            sax2dom = new SAX2DOM();
            p.setContentHandler(sax2dom);
            p.parse(new InputSource(new InputStreamReader(new ByteArrayInputStream(html.getBytes(Charset.defaultCharset())))));

            doc = (Document) sax2dom.getDOM();
        } catch (Exception e) {
            // TODO handle exception
        }


        return doc;
    }

    public static String getHtmlFromUrl(String strUrl, boolean isMobile) {
        URL url = null;
        BufferedReader reader = null;
        StringBuilder sb = null;
        String returnValue = "";

        try {
            url = new URL(strUrl);
            URLConnection con = url.openConnection();

            // force server to mimic specific Browser
            con.setRequestProperty("User-Agent", userAgent);
            if(isMobile)
                con.setRequestProperty("User-Agent", userAgentMobile);

            con.setReadTimeout(15000);
            con.connect();

            reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            sb = new StringBuilder();

            String line = null;
            while((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            returnValue = sb.toString();
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            if(reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return returnValue;
    }

    public static String unPACKED(String[] k, String p, int a) {
        int c = k.length;

        while(c > 0) {
            c--;
            if(!"".equals(k[c]))
                p = p.replaceAll("\\b" + getIntToBase(c, a) + "\\b", k[c]);
        }

        return p;
    }

    private static String getIntToBase(int c, int base) {
        int first = c / base;
        int remainder = c - first * base;

        String result = "";
        if(first>0)
            if(first>9) {
                result = Character.toString((char) (first + 87));
            } else {
                result = String.valueOf(first);
            }

        if(remainder>9) {
            result += Character.toString((char) (remainder + 87));
        } else {
            result += String.valueOf(remainder);
        }
        return result;
    }
}
