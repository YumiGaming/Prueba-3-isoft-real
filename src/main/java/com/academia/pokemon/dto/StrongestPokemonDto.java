package com.academia.pokemon.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class StrongestPokemonDto {
    private Integer generacion;
    private PokemonDetailDto pokemon;

    @Data
    @Builder
    public static class PokemonDetailDto {
        private Integer id;
        private String nombre;
        @JsonProperty("total_base_stats")
        private Integer totalBaseStats;
        private StatsDto stats;
        private List<String> tipos;
    }

    @Data
    @Builder
    public static class StatsDto {
        private Integer hp;
        private Integer attack;
        private Integer defense;
        @JsonProperty("special_attack")
        private Integer specialAttack;
        @JsonProperty("special_defense")
        private Integer specialDefense;
        private Integer speed;
    }
}
