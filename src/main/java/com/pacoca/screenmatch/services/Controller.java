package com.pacoca.screenmatch.services;

import com.pacoca.screenmatch.model.*;

import java.util.*;
import java.util.stream.Collectors;

public class Controller {
    private final Service SERVICE = new Service();
    private final Converter CONVERTER = new Converter();
    private final String URL = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=1f3767fa";


    private static Scanner sc = new Scanner(System.in);


    public void menu() {
        List<Season> seasons = new ArrayList<>();
        Serie show;

        System.out.println("Type a show: ");
        String userEntry = sc.nextLine();

        show = getShow(userEntry);

        seasons = getSeasons(show);

        seasons.forEach(s -> System.out.println(s.getSeasonNumber() + " - " + s.getRating()));


       // best5(userEntry);

    }

    private Serie getShow(String show) {

        String json = SERVICE.getData(URL + show.replace(" ", "+") + API_KEY);
        Serie formattedJson = CONVERTER.dataConverter(json, Serie.class);

        System.out.println(formattedJson);

        return formattedJson;
    }

    private void displaySeason(String URI) {
        String json = SERVICE.getData(URI);
        SeasonRecord formattedData = CONVERTER.dataConverter(json, SeasonRecord.class);


        System.out.println(formattedData);
        List<EpisodeRecord> episodeData = formattedData.episodeData();
        episodeData.forEach(ep -> {System.out.println(ep.title());});


    }

    private void best5(String show) {


        List<Episode> episodes = new ArrayList<>();
        String json = SERVICE.getData(URL + show.replace(" ", "+") + API_KEY);
        Serie formattedJson = CONVERTER.dataConverter(json, Serie.class);

        for (int i = 1; i < formattedJson.totalSeason(); i++) {
            String seasonJson = SERVICE.getData(URL + show.replace(" ", "+") + "&season=" + i + API_KEY);
            SeasonRecord seasonFormattedJson = CONVERTER.dataConverter(seasonJson, SeasonRecord.class);

            List<EpisodeRecord> seasonEpisodesData = seasonFormattedJson.episodeData();

            for (EpisodeRecord ep : seasonEpisodesData) {
                episodes.add(new Episode(ep, i));
            }
        }

        episodes.stream()
                .filter(e -> !e.getRating().equalsIgnoreCase( "N/A") )
                .distinct()
                .sorted((e1,e2) -> Double.compare(
                        Double.parseDouble(e2.getRating()),
                        Double.parseDouble(e1.getRating())
                ))
                .limit(5)
                .map(epData -> epData.getTitle() + " - " + epData.getRating() + " - Season " + epData.getSeason())
                .forEach(e -> System.out.println("\n" + e + "\n"));
    }


    private List<Season> getSeasons(Serie show) {

        List<Season> seasons = new ArrayList<>();

        for (int i = 1; i <= show.totalSeason(); i++) {
            double seasonRating = 0;
            int finalI = i;


            String seasonJson = SERVICE.getData(URL + show.title().replace(" ", "+") + "&season=" + i + API_KEY);
            SeasonRecord seasonFormattedJson = CONVERTER.dataConverter(seasonJson, SeasonRecord.class);


            List<Episode> seasonEpisodes = seasonFormattedJson.episodeData().stream()
                    .map(epRecord -> new Episode(epRecord, finalI))
                    .collect(Collectors.toList());


            for (Episode ep : seasonEpisodes) {
                if (ep.getRating().equalsIgnoreCase("N/A") || ep.getRating().equalsIgnoreCase("0.0")) {
                    continue;
                }
                seasonRating += Double.parseDouble(ep.getRating());


            }

            seasonRating /= seasonEpisodes.size();
            seasons.add(new Season(seasonFormattedJson, seasonEpisodes, seasonRating));
        }

        return seasons;
    }
}
