package com.academia.pokemon.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class RegionPokemonDto {
    private Integer id;
    private String nombre;
    private List<String> tipos;
    private Integer generacion;
}
