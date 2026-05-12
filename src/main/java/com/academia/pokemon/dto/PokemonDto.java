package com.academia.pokemon.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PokemonDto {
    private Integer id;
    private String nombre;
    private Integer defense;
    @JsonProperty("sp_defense")
    private Integer spDefense;
    private List<String> tipos;
}
