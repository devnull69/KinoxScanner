package org.theiner.kinoxscanner.util;

import android.app.Application;
import android.content.SharedPreferences;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.xalan.xsltc.trax.SAX2DOM;
import org.ccil.cowan.tagsoup.Parser;
import org.theiner.kinoxscanner.context.KinoxScannerApplication;
import org.theiner.kinoxscanner.data.CheckErgebnis;
import org.theiner.kinoxscanner.data.Film;
import org.theiner.kinoxscanner.data.SearchRequest;
import org.theiner.kinoxscanner.data.SearchResult;
import org.theiner.kinoxscanner.data.Serie;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by TTheiner on 26.02.2016.
 */
public class KinoxHelper {
    private static final String userAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.116 Safari/537.36";

    public static List<CheckErgebnis> check(KinoxScannerApplication myApp) {

        List<CheckErgebnis> result = new ArrayList<CheckErgebnis>();

        for(Film film : myApp.getFilme()) {
            Document doc = getDocumentFromUrl("http://www.kinox.to/Stream/" + film.toQueryString());
            if(doc!=null) {
                Element element = doc.getElementById("HosterList");

                NodeList liElements = element.getElementsByTagName("li");

                String currentDateStr = getMaxDateFromElements(liElements);

                // Vergleichen mit vorherigem Datum
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
                Date currentDate = null;
                Date lastDate = null;
                try {
                    currentDate = sdf.parse(currentDateStr);
                    lastDate = sdf.parse(film.getLastDate());
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                if(currentDate.after(lastDate)) {
                    CheckErgebnis ergebnis = new CheckErgebnis();
                    ergebnis.name = film.getName();
                    ergebnis.datum = currentDateStr;
                    ergebnis.videoLink = "";
                    ergebnis.foundElement = film;
                    result.add(ergebnis);
                }
            }
        }

        for(Serie serie : myApp.getSerien()) {
            String queryString = serie.toQueryString();

            String HTML = getHtmlFromUrl("http://www.kinox.to/aGET/MirrorByEpisode/" + queryString);
            Document serienDoc = getDocumentFromHTML("<html><body>" + HTML + "</body></html>");

            Element element = serienDoc.getElementById("HosterList");

            if (element != null) {
                NodeList liElements = element.getElementsByTagName("li");

                String currentDateStr = getMaxDateFromElements(liElements);

                CheckErgebnis ergebnis = new CheckErgebnis();
                ergebnis.name = serie.toString();
                ergebnis.datum = currentDateStr;
                ergebnis.videoLink = "";
                ergebnis.foundElement = serie;
                result.add(ergebnis);
            }
        }
        return result;
    }

    private static String getMaxDateFromElements(NodeList liElements) {
        Pattern pattern = Pattern.compile("Vom\\:\\s(\\d{2}\\.\\d{2}\\.\\d{4})");
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        Date currentDate = new Date(100, 0, 1);
        Date heute = new Date();

        for (int i = 0; i < liElements.getLength(); i++) {
            Element linode = (Element) liElements.item(i);
            String textToParse = linode.getTextContent();

            Matcher matcher = pattern.matcher(textToParse);
            while (matcher.find()) {
                try {
                    Date myDate = sdf.parse(matcher.group(1));
                    if (myDate.before(heute)) {
                        // kinox = 10 Tage draufrechnen!
                        myDate.setTime(myDate.getTime() + 10 * 24 * 60 * 60 * 1000);
                    }

                    if (myDate.after(currentDate))
                        currentDate = myDate;
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
        return sdf.format(currentDate);
    }

    public static List<Serie> getSerien() {
        List<Serie> result = new ArrayList<Serie>();

        Serie twd = new Serie();
        twd.setName("The Walking Dead");
        twd.setAddr("The_Walking_Dead-1");
        twd.setSeriesID(10437);
        twd.setSeason(6);
        twd.setEpisode(12);
        result.add(twd);

        Serie shameless = new Serie();
        shameless.setName("Shameless");
        shameless.setAddr("Shameless-2");
        shameless.setSeriesID(39278);
        shameless.setSeason(6);
        shameless.setEpisode(1);
        result.add(shameless);

        Serie sn_en = new Serie();
        sn_en.setName("Supernatural (en)");
        sn_en.setAddr("Supernatural_german_subbed");
        sn_en.setSeriesID(27249);
        sn_en.setSeason(11);
        sn_en.setEpisode(15);
        result.add(sn_en);

        Serie sn_de = new Serie();
        sn_de.setName("Supernatural (de)");
        sn_de.setAddr("Supernatural");
        sn_de.setSeriesID(2375);
        sn_de.setSeason(11);
        sn_de.setEpisode(1);
        result.add(sn_de);

        Serie vikings = new Serie();
        vikings.setName("Vikings");
        vikings.setAddr("Vikings-1");
        vikings.setSeriesID(46277);
        vikings.setSeason(4);
        vikings.setEpisode(1);
        result.add(vikings);

        Serie devious = new Serie();
        devious.setName("Devious Maids");
        devious.setAddr("Devious_Maids-1");
        devious.setSeriesID(47777);
        devious.setSeason(4);
        devious.setEpisode(1);
        result.add(devious);

        Serie feartwd = new Serie();
        feartwd.setName("Fear The Walking Dead");
        feartwd.setAddr("Fear_the_Walking_Dead-1");
        feartwd.setSeriesID(55976);
        feartwd.setSeason(2);
        feartwd.setEpisode(1);
        result.add(feartwd);

        Serie tmithc = new Serie();
        tmithc.setName("The Man in the High Castle");
        tmithc.setAddr("The_Man_in_the_High_Castle");
        tmithc.setSeriesID(64040);
        tmithc.setSeason(2);
        tmithc.setEpisode(1);
        result.add(tmithc);

        Serie bcs = new Serie();
        bcs.setName("Better Call Saul");
        bcs.setAddr("Better_Call_Saul");
        bcs.setSeriesID(54593);
        bcs.setSeason(2);
        bcs.setEpisode(4);
        result.add(bcs);

        Serie ol = new Serie();
        ol.setName("Outlander");
        ol.setAddr("Outlander-3");
        ol.setSeriesID(54425);
        ol.setSeason(2);
        ol.setEpisode(1);
        result.add(ol);

        Serie ahs = new Serie();
        ahs.setName("American Horror Story");
        ahs.setAddr("American_Horror_Story-Die_dunkle_Seite_in_dir-1");
        ahs.setSeriesID(37361);
        ahs.setSeason(6);
        ahs.setEpisode(1);
        result.add(ahs);

        Serie tbbt = new Serie();
        tbbt.setName("The Big Bang Theory");
        tbbt.setAddr("The_Big_Bang_Theory_german_subbed");
        tbbt.setSeriesID(27242);
        tbbt.setSeason(9);
        tbbt.setEpisode(18);
        result.add(tbbt);

        return result;
    }

    public static List<Film> getFilme() {
        List<Film> result = new ArrayList<Film>();

        Film gh = new Film();
        gh.setName("Gänsehaut");
        gh.setAddr("Gaensehaut");
        gh.setLastDate("04.03.2016");
        result.add(gh);

        Film panem = new Film();
        panem.setName("Tribute von Panem 4 Teil 2");
        panem.setAddr("Die_Tribute_von_Panem-Mockingjay_Teil_2");
        panem.setLastDate("12.01.2016");
        result.add(panem);

        Film testament = new Film();
        testament.setName("Das brandneue Testament");
        testament.setAddr("Das_brandneue_Testament");
        testament.setLastDate("25.02.2016");
        result.add(testament);

        Film deadpool = new Film();
        deadpool.setName("Deadpool");
        deadpool.setAddr("Deadpool");
        deadpool.setLastDate("04.03.2016");
        result.add(deadpool);

        Film zoomania = new Film();
        zoomania.setName("Zoomania");
        zoomania.setAddr("Zoomania");
        zoomania.setLastDate("25.02.2016");
        result.add(zoomania);

        return result;
    }

    private static Document getDocumentFromHTML(String html) {
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

    private static Document getDocumentFromUrl(String strUrl) {
        Parser p = new Parser();
        SAX2DOM sax2dom = null;
        Document doc  = null;

        try {

            URL url = new URL(strUrl);
            URLConnection con = url.openConnection();

            // force server to mimic specific Browser
            con.setRequestProperty("User-Agent", userAgent);

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

    private static String getHtmlFromUrl(String strUrl) {
        URL url = null;
        BufferedReader reader = null;
        StringBuilder sb = null;
        String returnValue = "";

        try {
            url = new URL(strUrl);
            URLConnection con = url.openConnection();

            // force server to mimic specific Browser
            con.setRequestProperty("User-Agent", userAgent);

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

    public static List<SearchResult> search(SearchRequest suche) {
        String suchString = suche.getSuchString().replaceAll(" ", "+");

        Document doc = getDocumentFromUrl("http://www.kinox.to/Search.html?q=" + suchString);

        List<SearchResult> result = new ArrayList<SearchResult>();

        if(doc!=null) {
            Element tbody = (Element) doc.getElementById("RsltTableStatic").getElementsByTagName("tbody").item(0);
            NodeList trResults = tbody.getElementsByTagName("tr");

            // Gibt es ein Suchergebnis?
            if(trResults.getLength()>0) {
                int tdAnzahl = ((Element) trResults.item(0)).getElementsByTagName("td").getLength();
                if (tdAnzahl == 7) {
                    for (int i = 0; i < trResults.getLength(); i++) {
                        Element currentTR = (Element) trResults.item(i);

                        // zweites TD enthält den Ergebnis-Typ als Attribut eines Images
                        NamedNodeMap attrMap = ((Element) currentTR.getElementsByTagName("td").item(1)).getElementsByTagName("img").item(0).getAttributes();
                        String typ = attrMap.getNamedItem("title").getNodeValue();

                        // passt Ergebnis zum Suchtyp?
                        if ((suche.getIsSerie() && typ.equals("series")) || (!suche.getIsSerie() && typ.equals("movie")) || (!suche.getIsSerie() && typ.equals("cinema"))) {
                            // erstes TD enthält die Sprache als image-Namen
                            attrMap = ((Element) currentTR.getElementsByTagName("td").item(0)).getElementsByTagName("img").item(0).getAttributes();
                            String languageFilename = attrMap.getNamedItem("src").getNodeValue();
                            int languageCode = -1;
                            Pattern pattern = Pattern.compile("\\/(\\d+)\\.png");
                            Matcher matcher = pattern.matcher(languageFilename);
                            if (matcher.find()) {
                                languageCode = Integer.parseInt(matcher.group(1));
                            }

                            // drittes TD enthält die Addr als Teil des Dateinamens eines Anchors
                            attrMap = ((Element) currentTR.getElementsByTagName("td").item(2)).getElementsByTagName("a").item(0).getAttributes();
                            String href = attrMap.getNamedItem("href").getNodeValue();
                            pattern = Pattern.compile("\\/Stream\\/(.*)\\.html");
                            matcher = pattern.matcher(href);
                            String addr = "";
                            if (matcher.find()) {
                                addr = matcher.group(1);
                            }

                            // drittes TD enthält den Titel
                            String titel = currentTR.getElementsByTagName("td").item(2).getTextContent();

                            // hole SeriesID für Serien
                            int seriesID = -1;
                            if (typ.equals("series")) {
                                seriesID = getSeriesID(addr);
                            }

                            SearchResult ergebnis = new SearchResult();
                            ergebnis.setName(titel);
                            ergebnis.setLanguageCode(languageCode);
                            ergebnis.setAddr(addr);
                            ergebnis.setSeriesID(seriesID);
                            result.add(ergebnis);
                        }
                    }
                }
            }
        }
        return result;
    }

    private static int getSeriesID(String addr) {
        int result = -1;
        Document doc = getDocumentFromUrl("http://www.kinox.to/Stream/" + addr + ".html");

        // SeriesID ist Teil des rel-Attributs der id "SeasonSelection"
        Element select = doc.getElementById("SeasonSelection");
        if(select != null) {
            NamedNodeMap attrMap = select.getAttributes();
            String rel = attrMap.getNamedItem("rel").getNodeValue();

            Pattern pattern = Pattern.compile("SeriesID=(\\d+)$");
            Matcher matcher = pattern.matcher(rel);
            if (matcher.find()) {
                result = Integer.parseInt(matcher.group(1));
            }
        }

        return result;
    }
}
