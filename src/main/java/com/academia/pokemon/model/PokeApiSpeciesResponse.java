package com.academia.pokemon.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PokeApiSpeciesResponse {
    private String name;
    @JsonProperty("evolution_chain")
    private EvolutionChainRef evolutionChain;

    @Data
    public static class EvolutionChainRef {
        private String url;
    }
}
