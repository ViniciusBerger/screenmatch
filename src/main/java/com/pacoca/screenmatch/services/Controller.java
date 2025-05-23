package com.pacoca.screenmatch.services;

import com.pacoca.screenmatch.model.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import io.github.cdimascio.dotenv.Dotenv;




public class Controller {
    Dotenv dotenv = Dotenv.load();
    private final Service SERVICE = new Service();
    private final Converter CONVERTER = new Converter();
    private final String URL = "https://www.omdbapi.com/?t=";
    private final String API_KEY = dotenv.get("OMDB_API_KEY");




    private static Scanner sc = new Scanner(System.in);


    public void menu() {
        List<Season> seasons = new ArrayList<>();

        var menu = "\n" +
                "1 - Search a show\n" +
                "2 - Display a show's seasons rating\n" +
                "3 - Display a show's best 5 episodes\n" +
                "4 - Display search history\n" +
                "5 - Get a recommendation\n" +
                "0 - Exit";


        int option = -1;
        String userEntry;
        Serie show;


        while (option !=0) {
            System.out.println(menu);
            try {
                option = Integer.parseInt(sc.nextLine());
            } catch (NumberFormatException e) {
                option = -1;
            }
            switch (option) {
                case 1:
                    System.out.println("Type a show: ");
                    show = getShow(sc.nextLine());
                    System.out.println(show.getTitle() + "\n - " + show.getRating() + "\n - " + show.getTotalSeason() + " seasons" + "\n - " + show.getCast() + "\n - " + show.getPlot() + "\n - " + show.getGenre());
                    break;
                case 2:
                    System.out.println("Type a show: ");
                    show = getShow(sc.nextLine());
                    getSeasons(show).forEach(s -> System.out.println(s.getSeasonNumber() + " - " + s.getRating()));
                    break;
                case 3:
                    System.out.println("Type a show: ");
                    show = getShow(sc.nextLine());
                    best5(show.getTitle());
                    break;
                case 4:
                    Serie.listSearchedSeries();
                    break;
                case 5:
                    try {
                        Service.GetRecomendation();
                    } catch (IOException | InterruptedException e) {
                        System.out.println("Error: " + e.getMessage() + "\n we were unable to get the recomendations, please try again later.");
                    }
                case 0:
                    System.out.println("Exiting...");
                    break;
            }
            }

    }

    private Serie getShow(String show) {

        String json = SERVICE.getData(URL + show.replace(" ", "+") + API_KEY);
        SerieRecord formattedJson = CONVERTER.dataConverter(json, SerieRecord.class);

        return new Serie(formattedJson);
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
        SerieRecord formattedJson = CONVERTER.dataConverter(json, SerieRecord.class);

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

        for (int i = 1; i <= show.getTotalSeason(); i++) {
            double seasonRating = 0;
            int finalI = i;


            String seasonJson = SERVICE.getData(URL + show.getTitle().replace(" ", "+") + "&season=" + i + API_KEY);
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

    public void displayMenu() {

    }
}
