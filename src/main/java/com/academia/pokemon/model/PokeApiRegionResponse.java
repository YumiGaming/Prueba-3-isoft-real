package com.academia.pokemon.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PokeApiRegionResponse {
    private String name;
    @JsonProperty("main_generation")
    private NamedApiResource mainGeneration;

    @Data
    public static class NamedApiResource {
        private String name;
        private String url;
    }
}
