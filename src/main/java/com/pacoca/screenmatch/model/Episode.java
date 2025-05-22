package com.pacoca.screenmatch.model;

import java.util.List;

public class Episode {
   private String title;
   private String rating;
   private String released;
   private Integer episodeNumber;
   private int season;

   public Episode() {}

    public Episode(List<EpisodeRecord> episodeRecords, int i) {
    }

    public Episode(EpisodeRecord episode, int season) {
        this.title = episode.title();
        this.rating = episode.rating();
        this.released = episode.released();
        this.episodeNumber = episode.episode();
        this.season = season;
    }

    public String getTitle() {
        return title;
    }

    public String getRating() {
        return rating;
    }

    public String getReleased() {
        return released;
    }

    public Integer getEpisodeNumber() {
        return episodeNumber;
    }

    public int getSeason() {
        return season;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setRating(String rating) {
       this.rating = rating;
    }

    public void setReleased(String released) {
        this.released = released;
    }

    public void setEpisodeNumber(Integer episodeNumber) {
        this.episodeNumber = episodeNumber;
    }

    public void setSeason(int season) {
        this.season = season;
    }


}
