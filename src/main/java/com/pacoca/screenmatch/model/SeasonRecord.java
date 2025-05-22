package com.pacoca.screenmatch.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SeasonRecord(@JsonAlias("Season")String season,
                           @JsonAlias("totalSeasons") Integer totalSeasons,
                           @JsonAlias("Episodes") List<EpisodeRecord> episodeData) { }
