package com.academia.pokemon.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class EvolutionChainDto {
    @JsonProperty("pokemon_base")
    private String pokemonBase;
    private List<EvolutionStepDto> cadena;

    @Data
    @Builder
    public static class EvolutionStepDto {
        private String nombre;
        private Integer orden;
        @JsonProperty("min_level")
        private Integer minLevel;
    }
}
