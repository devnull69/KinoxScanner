package org.theiner.kinoxscanner.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.theiner.kinoxscanner.async.CheckKinoxTask;
import org.theiner.kinoxscanner.async.CollectVideoLinksTask;
import org.theiner.kinoxscanner.context.KinoxScannerApplication;
import org.theiner.kinoxscanner.data.CheckErgebnis;
import org.theiner.kinoxscanner.data.Film;
import org.theiner.kinoxscanner.data.KinoxElementHoster;
import org.theiner.kinoxscanner.data.SearchRequest;
import org.theiner.kinoxscanner.data.SearchResult;
import org.theiner.kinoxscanner.data.Serie;
import org.theiner.kinoxscanner.data.KinoxHosterResponse;
import org.theiner.kinoxscanner.data.HosterMirror;
import org.theiner.kinoxscanner.data.VideoLink;
import org.theiner.kinoxscanner.strategien.FlashXStrategie;
import org.theiner.kinoxscanner.strategien.StreamCloudStrategie;
import org.theiner.kinoxscanner.strategien.TheVideoMeStrategie;
import org.theiner.kinoxscanner.strategien.VidBullStrategie;
import org.theiner.kinoxscanner.strategien.VidToMeStrategie;
import org.theiner.kinoxscanner.strategien.VidziStrategie;
import org.theiner.kinoxscanner.strategien.VodLockerStrategie;
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
            Document doc = HTTPHelper.getDocumentFromUrl("http://www.kinox.to/Stream/" + film.toQueryString(), "", false);
            if(doc!=null) {
                Element element = doc.getElementById("HosterList");

                NodeList liElements = element.getElementsByTagName("li");

                String currentDateStr = getMaxDateFromElements(liElements);
                List<HosterMirror> hosterMirrors = getVideoLinksFromElements(liElements, "http://www.kinox.to/Stream/" + film.getAddr() + ".html");

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
                    ergebnis.hosterMirrors = hosterMirrors;
                    ergebnis.foundElement = film;
                    result.add(ergebnis);
                }
            }
            counter++;
            task.doProgress((int) ((counter / (float) gesamtZahl) * 100));
        }

        for(Serie serie : myApp.getSerien()) {
            String queryString = serie.toQueryString();

            String HTML = HTTPHelper.getHtmlFromUrl("http://www.kinox.to/aGET/MirrorByEpisode/" + queryString, "", false);
            Document serienDoc = HTTPHelper.getDocumentFromHTML("<html><body>" + HTML + "</body></html>");

            Element element = serienDoc.getElementById("HosterList");

            if (element != null) {
                NodeList liElements = element.getElementsByTagName("li");

                List<HosterMirror> hosterMirrors = getVideoLinksFromElements(liElements, "http://www.kinox.to/Stream/" + serie.getAddr() + ".html");

                CheckErgebnis ergebnis = new CheckErgebnis();
                ergebnis.name = serie.toString();
                ergebnis.datum = "";
                ergebnis.hosterMirrors = hosterMirrors;
                ergebnis.foundElement = serie;
                result.add(ergebnis);
            }
            counter++;
            task.doProgress((int) ((counter / (float) gesamtZahl) * 100));
        }
        return result;
    }

    private static List<HosterMirror> getVideoLinksFromElements(NodeList liElements, String referer) {
        List<HosterMirror> result = new ArrayList<>();

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

            HosterMirror hosterMirror;
            switch(id) {
                case 30:
                    // StreamCloud
                    hosterMirror = new HosterMirror();
                    hosterMirror.setMirrorCount(mirrorCount);
                    hosterMirror.setStrategie(new StreamCloudStrategie(referer));
                    result.add(hosterMirror);

                    break;
                case 33:
                    // FlashX
                    hosterMirror = new HosterMirror();
                    hosterMirror.setMirrorCount(mirrorCount);
                    hosterMirror.setStrategie(new FlashXStrategie(referer));
                    result.add(hosterMirror);

                    break;
                case 50:
                    // VidBull
                    hosterMirror = new HosterMirror();
                    hosterMirror.setMirrorCount(mirrorCount);
                    hosterMirror.setStrategie(new VidBullStrategie());
                    result.add(hosterMirror);

                    break;
                case 51:
                    // VidToMe
                    hosterMirror = new HosterMirror();
                    hosterMirror.setMirrorCount(mirrorCount);
                    hosterMirror.setStrategie(new VidToMeStrategie(referer));
                    result.add(hosterMirror);

                    break;
                case 58:
                    // TheVideo.me
                    hosterMirror = new HosterMirror();
                    hosterMirror.setMirrorCount(mirrorCount);
                    hosterMirror.setStrategie(new TheVideoMeStrategie(referer));
                    result.add(hosterMirror);

                    break;
                case 65:
                    // VodLocker
                    hosterMirror = new HosterMirror();
                    hosterMirror.setMirrorCount(mirrorCount);
                    hosterMirror.setStrategie(new VodLockerStrategie());
                    result.add(hosterMirror);

                    break;
                case 68:
                    // Vidzi
                    hosterMirror = new HosterMirror();
                    hosterMirror.setMirrorCount(mirrorCount);
                    hosterMirror.setStrategie(new VidziStrategie());
                    result.add(hosterMirror);

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

    private static String getHosterURL(String strUrl) {
        String response = "";
        if(!Thread.interrupted()) {
            String json = HTTPHelper.getHtmlFromUrl(strUrl, "", false);

            KinoxHosterResponse kinoxHosterResponse = null;
            if (!"".equals(json)) {
                ObjectMapper mapper = new ObjectMapper();
                try {
                    kinoxHosterResponse = mapper.readValue(json, new TypeReference<KinoxHosterResponse>() {
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            if (kinoxHosterResponse != null) {
                // parse Link from HTML
                String stream = kinoxHosterResponse.getStream();
                Pattern pattern = Pattern.compile("href=\"(.*)\"\\starget=\"_blank\">");
                Matcher matcher = pattern.matcher(stream);
                if (matcher.find()) {
                    response = matcher.group(1);
                }

                // Falls response nicht mit http anfängt, dann filtern
                response = response.substring(response.indexOf("http"));
            }
        }
        return response;
    }

    public static List<SearchResult> search(SearchRequest suche) {
        String suchString = suche.getSuchString().replaceAll(" ", "+");

        Document doc = HTTPHelper.getDocumentFromUrl("http://www.kinox.to/Search.html?q=" + suchString, "", false);

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
        Document doc = HTTPHelper.getDocumentFromUrl("http://www.kinox.to/Stream/" + addr + ".html", "", false);

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

    public static List<VideoLink> collectVideoLinks(CollectVideoLinksTask task, KinoxElementHoster currentHoster) throws InterruptedException{
        List<VideoLink> result;
        if(currentHoster.getVideoLinks() != null) {
            result = currentHoster.getVideoLinks();
        } else {
            result = new ArrayList<VideoLink>();
        }

        for(int mirror=1; mirror<=currentHoster.getHosterMirror().getMirrorCount(); mirror++) {
            if(!Thread.interrupted()) {
                String url = "";
                if (currentHoster.getFoundElement() instanceof Serie) {
                    Serie currentSerie = (Serie) currentHoster.getFoundElement();
                    url = "http://www.kinox.to/aGET/Mirror/" + currentSerie.getAddr() + "&Hoster=" + currentHoster.getHosterMirror().getStrategie().hosterNummer + "&Mirror=" + mirror + "&Season=" + currentSerie.getSeason() + "&Episode=" + currentSerie.getEpisode();
                    String hosterURL = getHosterURL(url);

                    String videoStreamURL = currentHoster.getHosterMirror().getStrategie().getVideoURL(hosterURL);

                    if (!"".equals(videoStreamURL)) {
                        VideoLink neuerLink = new VideoLink();
                        neuerLink.setHosterName(currentHoster.getHosterMirror().getStrategie().hosterName);
                        neuerLink.setVideoURL(videoStreamURL);
                        result.add(neuerLink);
                    }
                } else {
                    Film currentFilm = (Film) currentHoster.getFoundElement();
                    url = "http://www.kinox.to/aGET/Mirror/" + currentFilm.getAddr() + "&Hoster=" + currentHoster.getHosterMirror().getStrategie().hosterNummer + "&Mirror=" + mirror;
                    String hosterURL = getHosterURL(url);

                    String videoStreamURL = currentHoster.getHosterMirror().getStrategie().getVideoURL(hosterURL);

                    if (!"".equals(videoStreamURL)) {
                        VideoLink neuerLink = new VideoLink();
                        neuerLink.setHosterName(currentHoster.getHosterMirror().getStrategie().hosterName);
                        neuerLink.setVideoURL(videoStreamURL);
                        result.add(neuerLink);
                    }
                }
                task.doProgress((int) ((mirror / (float) currentHoster.getHosterMirror().getMirrorCount()) * 100));
            }
        }
        return result;
    }
}
