package com.academia.pokemon.controller;

import com.academia.pokemon.dto.PokemonDto;
import com.academia.pokemon.service.PokemonService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/pokemon")
public class PokemonController {

    private final PokemonService pokemonService;

    public PokemonController(PokemonService pokemonService) {
        this.pokemonService = pokemonService;
    }

    @GetMapping("/weak-defense")
    public ResponseEntity<List<PokemonDto>> getWeakDefensePokemons() {
        return ResponseEntity.ok(pokemonService.getWeakDefensePokemons());
    }
}
