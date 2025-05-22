package com.pacoca.screenmatch.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// Record to storage the values from series fetch from IMDB API

@JsonIgnoreProperties(ignoreUnknown = true) // jackson comes with ignore unknown set to false, what makes it break when you leave an attribute unchosen

public record Serie(@JsonAlias("Title") String title, @JsonAlias("imdbRating") float rating, @JsonAlias("totalSeasons")Integer totalSeason) {
}
