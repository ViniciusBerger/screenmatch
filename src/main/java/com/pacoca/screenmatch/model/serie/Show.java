package com.pacoca.screenmatch.model.serie;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.pacoca.screenmatch.model.Category;
import com.pacoca.screenmatch.model.season.Season;

public class Show {
    private String title;
    private double rating;
    private Integer totalSeason;
    private Optional<Category> genre;
    private String cast;
    private String poster;
    private String plot;

    private static List<Show> shows = new ArrayList<Show>();


    public Show() {}

    public Show(ShowRecord serieRecord) {
        this.title = serieRecord.title();
        this.rating = Optional.ofNullable(serieRecord.rating()).map(this::parseRating).orElse(0.0);
        this.totalSeason = serieRecord.totalSeason();
        this.genre = Optional.ofNullable(serieRecord.genre())
            .map(String::trim)
            .filter(s -> !s.isEmpty() && !"N/A".equalsIgnoreCase(s))
            .map(s -> s.split(",")[0])
            // .map(String::trim)
            .map(Category::fromString);
        this.cast = serieRecord.cast();
        this.poster = serieRecord.poster();
        this.plot = serieRecord.plot();

        shows.add(this);
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

    public Optional<Category> getGenre() {
        return genre;
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
        shows.forEach(s -> System.out.println(s.getTitle()));
    }

    public static String[] getSearchedSeries() {

        String[] listSeries;
        listSeries = shows.stream().map(Show::getTitle).toArray(String[]::new);


        return listSeries;
    }
}
