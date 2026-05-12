package com.academia.pokemon.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class PokeApiGenerationResponse {
    private Integer id;
    @JsonProperty("pokemon_species")
    private List<NamedApiResource> pokemonSpecies;

    @Data
    public static class NamedApiResource {
        private String name;
        private String url;
    }
}
