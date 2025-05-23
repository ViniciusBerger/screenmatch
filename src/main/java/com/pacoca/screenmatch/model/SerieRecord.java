package com.pacoca.screenmatch.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// Record to storage the values from series fetch from IMDB API

@JsonIgnoreProperties(ignoreUnknown = true) // jackson has the function to ignore unknown values set to false, what makes it break when you leave an attribute unchosen

public record SerieRecord(@JsonAlias("Title") String title,
                          @JsonAlias("imdbRating") String rating,
                          @JsonAlias("totalSeasons")Integer totalSeason,
                          @JsonAlias("Genre") String genre,
                          @JsonAlias("Actors") String cast,
                          @JsonAlias("Poster") String poster,
                          @JsonAlias("Plot") String plot) {
}
