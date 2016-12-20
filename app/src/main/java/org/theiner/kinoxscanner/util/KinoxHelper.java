package org.theiner.kinoxscanner.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.theiner.kinoxscanner.async.CheckKinoxTask;
import org.theiner.kinoxscanner.async.CollectVideoLinksTask;
import org.theiner.kinoxscanner.context.KinoxScannerApplication;
import org.theiner.kinoxscanner.data.CheckErgebnis;
import org.theiner.kinoxscanner.data.Film;
import org.theiner.kinoxscanner.data.ExtraInfo;
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

                Elements liElements = element.getElementsByTag("li");

                String currentDateStr = getMaxDateFromElements(liElements);
                List<HosterMirror> hosterMirrors = getVideoLinksFromElements(liElements, "http://www.kinox.to/Stream/" + film.getAddr() + ".html");

                // Vergleichen mit vorherigem Datum
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
                Date currentDate = null;
                Date lastDate = null;
                try {
                    currentDate = sdf.parse(currentDateStr);
                    if(!film.getLastDate().equals(""))
                        lastDate = sdf.parse(film.getLastDate());
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                if(lastDate == null || currentDate.after(lastDate)) {
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
                Elements liElements = element.getElementsByTag("li");

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

    private static List<HosterMirror> getVideoLinksFromElements(Elements liElements, String referer) {

        Pattern datePattern = Pattern.compile("Vom\\:\\s(\\d{2}\\.\\d{2}\\.\\d{4})");
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        Date heute = new Date();

        List<HosterMirror> result = new ArrayList<>();

        for(Element linode : liElements) {

            // Hosternummer bestimmen
            String idStr = linode.attr("id");
            Pattern pattern = Pattern.compile("(\\d+)");
            Matcher matcher = pattern.matcher(idStr);
            int id = 0;
            if(matcher.find())
                id = Integer.parseInt(matcher.group(1));

            // Anzahl Mirrors bestimmen
            String divContent = linode.getElementsByTag("div").get(1).text();
            pattern = Pattern.compile("\\/(\\d+)");
            matcher = pattern.matcher(divContent);
            int mirrorCount = 0;
            if(matcher.find()) {
                mirrorCount = Integer.parseInt(matcher.group(1));
            }

            String textToParse = linode.text();

            Matcher dateMatcher = datePattern.matcher(textToParse);
            String theDate = "";
            if (dateMatcher.find()) {
                try {
                    Date myDate = sdf.parse(dateMatcher.group(1));
                    if (myDate.before(heute)) {
                        // kinox = 10 Tage draufrechnen!
                        myDate.setTime(myDate.getTime() + 10 * 24 * 60 * 60 * 1000);
                    }

                    theDate = sdf.format(myDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            HosterMirror hosterMirror;
            switch(id) {
                case 30:
                    // StreamCloud
                    hosterMirror = new HosterMirror();
                    hosterMirror.setMirrorCount(mirrorCount);
                    hosterMirror.setStrategie(new StreamCloudStrategie(referer));
                    hosterMirror.setHosterdate(theDate);
                    result.add(hosterMirror);

                    break;
                case 33:
                    // FlashX
                    hosterMirror = new HosterMirror();
                    hosterMirror.setMirrorCount(mirrorCount);
                    hosterMirror.setStrategie(new FlashXStrategie(referer));
                    hosterMirror.setHosterdate(theDate);
                    result.add(hosterMirror);

                    break;
                case 50:
                    // VidBull
                    hosterMirror = new HosterMirror();
                    hosterMirror.setMirrorCount(mirrorCount);
                    hosterMirror.setStrategie(new VidBullStrategie());
                    hosterMirror.setHosterdate(theDate);
                    result.add(hosterMirror);

                    break;
                case 51:
                    // VidToMe
                    hosterMirror = new HosterMirror();
                    hosterMirror.setMirrorCount(mirrorCount);
                    hosterMirror.setStrategie(new VidToMeStrategie(referer));
                    hosterMirror.setHosterdate(theDate);
                    result.add(hosterMirror);

                    break;
                case 58:
                    // TheVideo.me
                    hosterMirror = new HosterMirror();
                    hosterMirror.setMirrorCount(mirrorCount);
                    hosterMirror.setStrategie(new TheVideoMeStrategie(referer));
                    hosterMirror.setHosterdate(theDate);
                    result.add(hosterMirror);

                    break;
                case 65:
                    // VodLocker
                    hosterMirror = new HosterMirror();
                    hosterMirror.setMirrorCount(mirrorCount);
                    hosterMirror.setStrategie(new VodLockerStrategie());
                    hosterMirror.setHosterdate(theDate);
                    result.add(hosterMirror);

                    break;
                case 68:
                    // Vidzi
                    hosterMirror = new HosterMirror();
                    hosterMirror.setMirrorCount(mirrorCount);
                    hosterMirror.setStrategie(new VidziStrategie());
                    hosterMirror.setHosterdate(theDate);
                    result.add(hosterMirror);

                    break;
            }
        }

        return result;
    }

    private static String getMaxDateFromElements(Elements liElements) {
        Pattern pattern = Pattern.compile("Vom\\:\\s(\\d{2}\\.\\d{2}\\.\\d{4})");
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        Date currentDate = new Date(100, 0, 1);
        Date heute = new Date();

        for (Element linode : liElements) {
            String textToParse = linode.text();

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

            try {
                if (kinoxHosterResponse != null) {
                    // parse Link from HTML
                    String stream = kinoxHosterResponse.getStream();
                    Pattern pattern = Pattern.compile("href=\"(.*)\"\\starget=\"_blank\">");
                    Matcher matcher = pattern.matcher(stream);
                    if (matcher.find()) {
                        response = matcher.group(1);
                    } else {
                        // iframe
                        Pattern pattern2 = Pattern.compile("src=\"(.*)\"\\swidth=");
                        Matcher matcher2 = pattern2.matcher(stream);
                        if (matcher2.find()) {
                            response = matcher2.group(1);
                        }
                    }

                    // Falls response nicht mit http anfängt, dann filtern
                    response = response.substring(response.indexOf("http"));
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        return response;
    }

    public static List<SearchResult> search(SearchRequest suche) {
        String suchString = suche.getSuchString().replaceAll(" ", "+");

        Document doc = HTTPHelper.getDocumentFromUrl("http://www.kinox.to/Search.html?q=" + suchString, "", false);

        List<SearchResult> result = new ArrayList<SearchResult>();

        if(doc!=null) {
            Element tbody = doc.getElementById("RsltTableStatic").getElementsByTag("tbody").get(0);
            Elements trResults = tbody.getElementsByTag("tr");

            // Gibt es ein Suchergebnis?
            if(trResults.size()>0) {
                int tdAnzahl = trResults.get(0).getElementsByTag("td").size();
                if (tdAnzahl == 7) {
                    for (Element currentTR : trResults) {

                        // zweites TD enthält den Ergebnis-Typ als Attribut eines Images
                        Element img2 = currentTR.getElementsByTag("td").get(1).getElementsByTag("img").get(0);
                        String typ = img2.attr("title");

                        // passt Ergebnis zum Suchtyp?
                        if ((suche.getIsSerie() && typ.equals("series")) || (!suche.getIsSerie() && typ.equals("movie")) || (!suche.getIsSerie() && typ.equals("cinema"))) {
                            // erstes TD enthält die Sprache als image-Namen
                            Element img1 = currentTR.getElementsByTag("td").get(0).getElementsByTag("img").get(0);
                            String languageFilename = img1.attr("src");
                            int languageCode = -1;
                            Pattern pattern = Pattern.compile("\\/(\\d+)\\.png");
                            Matcher matcher = pattern.matcher(languageFilename);
                            if (matcher.find()) {
                                languageCode = Integer.parseInt(matcher.group(1));
                            }

                            // drittes TD enthält die Addr als Teil des Dateinamens eines Anchors
                            Element anchor = currentTR.getElementsByTag("td").get(2).getElementsByTag("a").get(0);
                            String href = anchor.attr("abs:href");
                            pattern = Pattern.compile("\\/Stream\\/(.*)\\.html");
                            matcher = pattern.matcher(href);
                            String addr = "";
                            if (matcher.find()) {
                                addr = matcher.group(1);
                            }

                            // drittes TD enthält den Titel
                            String titel = currentTR.getElementsByTag("td").get(2).text();

                            // hole SeriesID für Serien und ImageSubDir für beides
                            ExtraInfo extraInfo = getExtraInfo(addr);

                            SearchResult ergebnis = new SearchResult();
                            ergebnis.setName(titel);
                            ergebnis.setLanguageCode(languageCode);
                            ergebnis.setAddr(addr);
                            ergebnis.setSeriesID(extraInfo.getSeriesID());
                            ergebnis.setImageSubDir(extraInfo.getImageSubDir());
                            ergebnis.setImdbRating(extraInfo.getImdbRating());
                            result.add(ergebnis);
                        }
                    }
                }
            }
        }
        return result;
    }

    private static ExtraInfo getExtraInfo(String addr) {
        int seriesID = -1;
        String imageSubDir = "";
        Document doc = HTTPHelper.getDocumentFromUrl("http://www.kinox.to/Stream/" + addr + ".html", "", false);

        // SeriesID ist Teil des rel-Attributs der id "SeasonSelection"
        Element select = doc.getElementById("SeasonSelection");
        if(select != null) {
            String rel = select.attr("rel");

            Pattern pattern = Pattern.compile("SeriesID=(\\d+)$");
            Matcher matcher = pattern.matcher(rel);
            if (matcher.find()) {
                seriesID = Integer.parseInt(matcher.group(1));
            }
        }

        // ImageSubDir ist Teil des Image-Tags, dass auch die Addr der Serie/des Films enthält
        Elements images = doc.getElementsByTag("img");

        for(Element currentImage : images) {

            String src = currentImage.attr("src");

            if(src.indexOf(addr) != -1) {
                int startpos = src.indexOf("thumbs") + 7;
                imageSubDir = src.substring(startpos, startpos+8);
            }
        }

        // IMDBRating steht in einer Klasse
        Element imdbElement = doc.getElementsByClass("IMDBRatingLabel").first();
        String imdbText = imdbElement.text();
        Pattern pattern2 = Pattern.compile("^(.*)\\s\\/");
        Matcher matcher2 = pattern2.matcher(imdbText);
        String imdbRating = "";
        if(matcher2.find()) {
            imdbRating = matcher2.group(1);
        }

        return new ExtraInfo(seriesID, imageSubDir, imdbRating);
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
                        neuerLink.setFilename(currentSerie.toString());
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
                        neuerLink.setFilename(currentFilm.toString());
                        result.add(neuerLink);
                    }
                }
                task.doProgress((int) ((mirror / (float) currentHoster.getHosterMirror().getMirrorCount()) * 100));
            }
        }
        return result;
    }

    public static boolean isConnectedViaWifi(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return mWifi.isConnected();
    }

}
