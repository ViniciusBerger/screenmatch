package com.pacoca.screenmatch.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Serie {
    private String title;
    private double rating;
    private Integer totalSeason;
    private Category genre;
    private String cast;
    private String poster;
    private String plot;

    private static List<Serie> series = new ArrayList<Serie>();


    public Serie() {}

    public Serie(SerieRecord serieRecord) {
        this.title = serieRecord.title();
        this.rating = Optional.ofNullable(serieRecord.rating()).map(this::parseRating).orElse(0.0);
        this.totalSeason = serieRecord.totalSeason();
        this.genre = Category.fromString(serieRecord.genre().split(",")[0]);
        this.cast = serieRecord.cast();
        this.poster = serieRecord.poster();
        this.plot = serieRecord.plot();

        series.add(this);
    }


    public Season getSeasons() {
        return seasons;
    }

    public void setSeasons(Season seasons) {
        this.seasons = seasons;
    }

    private Season seasons;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(String rating) {
        if (rating.equalsIgnoreCase("N/A") || rating.isEmpty()) {
            this.rating = 0.0;
        }
        this.rating = Double.parseDouble(rating);
    }

    public double parseRating(String rating) {
        if (rating.equalsIgnoreCase("N/A") || rating.isEmpty()) {
            return 0.0;
        }
        return Double.parseDouble(rating);
    }

    public Integer getTotalSeason() {
        return totalSeason;
    }

    public void setTotalSeason(Integer totalSeason) {
        this.totalSeason = totalSeason;
    }

    public Category getGenre() {
        return genre;
    }

    public void setGenre(Category genre) {
        this.genre = genre;
    }

    public String getCast() {
        return cast;
    }

    public void setCast(String cast) {
        this.cast = cast;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public String getPlot() {
        return plot;
    }

    public void setPlot(String plot) {
        this.plot = plot;
    }

    public static void listSearchedSeries() {
        series.forEach(s -> System.out.println(s.getTitle()));
    }

    public static String[] getSearchedSeries() {

        String[] listSeries;
        listSeries = series.stream().map(Serie::getTitle).toArray(String[]::new);


        return listSeries;
    }
}
