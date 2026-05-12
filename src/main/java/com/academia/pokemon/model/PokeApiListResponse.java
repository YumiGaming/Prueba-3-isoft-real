package com.academia.pokemon.model;

import lombok.Data;
import java.util.List;

@Data
public class PokeApiListResponse {
    private List<Result> results;

    @Data
    public static class Result {
        private String name;
        private String url;
    }
}
