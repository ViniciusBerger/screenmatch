package com.pacoca.screenmatch.model.season;

import java.util.List;

import com.pacoca.screenmatch.model.episode.Episode;

public class Season {

    private String seasonNumber;
    private int totalSeasons;
    private List<Episode> episodes;
    private double rating;


    public Season() {}

    public Season(SeasonRecord seasonRecord, List<Episode> episodes, double rating) {
        this.seasonNumber = seasonRecord.season();
        this.totalSeasons = seasonRecord.totalSeasons();
        this.episodes = episodes;
        this.rating = rating;
    }

    public String getSeasonNumber() {
        return seasonNumber;
    }

    public void setSeasonNumber(String seasonNumber) {
        this.seasonNumber = seasonNumber;
    }

    public int getTotalSeasons() {
        return totalSeasons;
    }

    public void setTotalSeasons(int totalSeasons) {
        this.totalSeasons = totalSeasons;
    }

    public List<Episode> getEpisodes() {
        return episodes;
    }

    public void setEpisode(List<Episode> episode) {
        this.episodes.addAll(episode);
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }



}
