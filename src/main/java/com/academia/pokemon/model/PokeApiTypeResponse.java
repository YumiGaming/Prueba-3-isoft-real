package com.academia.pokemon.model;

import lombok.Data;
import java.util.List;

@Data
public class PokeApiTypeResponse {
    private List<PokemonEntry> pokemon;

    @Data
    public static class PokemonEntry {
        private Pokemon pokemon;

        @Data
        public static class Pokemon {
            private String name;
            private String url;
        }
    }
}
