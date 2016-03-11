package org.theiner.kinoxscanner.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.theiner.kinoxscanner.async.CheckKinoxTask;
import org.theiner.kinoxscanner.context.KinoxScannerApplication;
import org.theiner.kinoxscanner.data.CheckErgebnis;
import org.theiner.kinoxscanner.data.Film;
import org.theiner.kinoxscanner.data.SearchRequest;
import org.theiner.kinoxscanner.data.SearchResult;
import org.theiner.kinoxscanner.data.Serie;
import org.theiner.kinoxscanner.data.KinoxHosterResponse;
import org.theiner.kinoxscanner.data.VideoLink;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import java.io.IOException;
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

    public static List<CheckErgebnis> check(CheckKinoxTask task, KinoxScannerApplication myApp) {

        List<CheckErgebnis> result = new ArrayList<CheckErgebnis>();

        int gesamtZahl = myApp.getFilme().size() + myApp.getSerien().size();
        int counter = 0;

        for(Film film : myApp.getFilme()) {
            Document doc = HTTPHelper.getDocumentFromUrl("http://www.kinox.to/Stream/" + film.toQueryString(), false);
            if(doc!=null) {
                Element element = doc.getElementById("HosterList");

                NodeList liElements = element.getElementsByTagName("li");

                String currentDateStr = getMaxDateFromElements(liElements);
                List<VideoLink> videoLinks = getVideoLinksFromElements(liElements);

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
                    ergebnis.videoLinks = videoLinks;
                    ergebnis.foundElement = film;
                    result.add(ergebnis);
                }
            }
            counter++;
            task.doProgress((int) ((counter / (float) gesamtZahl) * 100));
        }

        for(Serie serie : myApp.getSerien()) {
            String queryString = serie.toQueryString();

            String HTML = HTTPHelper.getHtmlFromUrl("http://www.kinox.to/aGET/MirrorByEpisode/" + queryString, false);
            Document serienDoc = HTTPHelper.getDocumentFromHTML("<html><body>" + HTML + "</body></html>");

            Element element = serienDoc.getElementById("HosterList");

            if (element != null) {
                NodeList liElements = element.getElementsByTagName("li");

                List<VideoLink> videoLinks = getVideoLinksFromElements(liElements);

                CheckErgebnis ergebnis = new CheckErgebnis();
                ergebnis.name = serie.toString();
                ergebnis.datum = "";
                ergebnis.videoLinks = videoLinks;
                ergebnis.foundElement = serie;
                result.add(ergebnis);
            }
            counter++;
            task.doProgress((int) ((counter / (float) gesamtZahl) * 100));
        }
        return result;
    }

    private static List<VideoLink> getVideoLinksFromElements(NodeList liElements) {
        List<VideoLink> result = new ArrayList<>();

        for(int i=0; i < liElements.getLength(); i++) {
            Element linode = (Element) liElements.item(i);

            // Hosternummer bestimmen
            NamedNodeMap attrMap = linode.getAttributes();
            String idStr = attrMap.getNamedItem("id").getNodeValue();
            Pattern pattern = Pattern.compile("(\\d+)");
            Matcher matcher = pattern.matcher(idStr);
            int id = 0;
            if(matcher.find())
                id = Integer.parseInt(matcher.group(1));

            // Anzahl Mirrors bestimmen
            String divContent = linode.getElementsByTagName("div").item(1).getTextContent();
            pattern = Pattern.compile("\\/(\\d+)");
            matcher = pattern.matcher(divContent);
            int mirrorCount = 0;
            if(matcher.find()) {
                mirrorCount = Integer.parseInt(matcher.group(1));
            }

            switch(id) {
                case 65:
                    // VodLocker
                    VideoLink videoLink = new VideoLink();
                    videoLink.setHosterName("VodLocker.com");
                    videoLink.setHosterNummer(id);
                    videoLink.setMirrorCount(mirrorCount);
                    result.add(videoLink);

                    break;
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

    private static String getVideoStreamLinkFromUrl(String strUrl) {
        String json = HTTPHelper.getHtmlFromUrl(strUrl, false);
        String response = "";

        KinoxHosterResponse kinoxHosterResponse = null;
        if(!"".equals(json)) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                kinoxHosterResponse = mapper.readValue(json, new TypeReference<KinoxHosterResponse>() {
                });
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        if(kinoxHosterResponse != null) {
            String hosterURL = "";
            // parse Link from HTML
            String stream = kinoxHosterResponse.getStream();
            Pattern pattern = Pattern.compile("href=\"(.*)\"\\starget=\"_blank\">");
            Matcher matcher = pattern.matcher(stream);
            if(matcher.find()) {
                hosterURL = matcher.group(1);
            }

            if(!"".equals(hosterURL)) {
                // get MP4 file link directly (with mobile browser setup)
                Document vodLockerDocument = HTTPHelper.getDocumentFromUrl(hosterURL, true);

                NodeList sources = vodLockerDocument.getElementsByTagName("source");
                if(sources.getLength()>0) {
                    Element source = (Element) sources.item(0);

                    NamedNodeMap attrMap = source.getAttributes();
                    response = attrMap.getNamedItem("src").getNodeValue();

                }

            }
        }

        return response;
    }

    public static List<SearchResult> search(SearchRequest suche) {
        String suchString = suche.getSuchString().replaceAll(" ", "+");

        Document doc = HTTPHelper.getDocumentFromUrl("http://www.kinox.to/Search.html?q=" + suchString, false);

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
        Document doc = HTTPHelper.getDocumentFromUrl("http://www.kinox.to/Stream/" + addr + ".html", false);

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
