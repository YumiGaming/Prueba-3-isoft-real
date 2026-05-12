package com.academia.pokemon.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TypeDominanceDto {
    private String tipo;
    @JsonProperty("total_pokemon")
    private Integer totalPokemon;
    @JsonProperty("pokemon_mas_experimentado")
    private ExperiencedPokemonDto pokemonMasExperimentado;

    @Data
    @Builder
    public static class ExperiencedPokemonDto {
        private Integer id;
        private String nombre;
        @JsonProperty("base_experience")
        private Integer baseExperience;
    }
}
