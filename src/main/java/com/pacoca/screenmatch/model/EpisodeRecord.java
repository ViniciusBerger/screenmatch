package com.pacoca.screenmatch.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EpisodeRecord(@JsonAlias("Title") String title,
                            @JsonAlias("imdbRating") String rating,
                            @JsonAlias("Released") String released,
                            @JsonAlias("Episode") Integer episode,
                            String season)  {
}
