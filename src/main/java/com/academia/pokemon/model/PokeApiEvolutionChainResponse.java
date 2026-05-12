package com.academia.pokemon.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class PokeApiEvolutionChainResponse {
    private ChainLink chain;

    @Data
    public static class ChainLink {
        private Species species;
        @JsonProperty("evolution_details")
        private List<EvolutionDetail> evolutionDetails;
        @JsonProperty("evolves_to")
        private List<ChainLink> evolvesTo;

        @Data
        public static class Species {
            private String name;
        }

        @Data
        public static class EvolutionDetail {
            @JsonProperty("min_level")
            private Integer minLevel;
        }
    }
}
