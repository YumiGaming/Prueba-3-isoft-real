package com.academia.pokemon.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class PokeApiPokemonDetail {
    private Integer id;
    private String name;
    @JsonProperty("base_experience")
    private Integer baseExperience;
    private List<StatEntry> stats;
    private List<TypeEntry> types;

    @Data
    public static class StatEntry {
        @JsonProperty("base_stat")
        private Integer baseStat;
        private Stat stat;

        @Data
        public static class Stat {
            private String name;
        }
    }

    @Data
    public static class TypeEntry {
        private Type type;

        @Data
        public static class Type {
            private String name;
        }
    }
}
