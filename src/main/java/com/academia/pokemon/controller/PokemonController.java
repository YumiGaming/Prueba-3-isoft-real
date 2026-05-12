package com.academia.pokemon.controller;

import com.academia.pokemon.dto.EvolutionChainDto;
import com.academia.pokemon.dto.PokemonDto;
import com.academia.pokemon.dto.RegionPokemonDto;
import com.academia.pokemon.dto.TypeDominanceDto;
import com.academia.pokemon.service.PokemonService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @GetMapping("/type-dominance/{type}")
    public ResponseEntity<TypeDominanceDto> getTypeDominance(@PathVariable String type) {
        return ResponseEntity.ok(pokemonService.getTypeDominance(type));
    }

    @GetMapping("/evolution-chain/{id}")
    public ResponseEntity<EvolutionChainDto> getEvolutionChain(@PathVariable Integer id) {
        return ResponseEntity.ok(pokemonService.getEvolutionChain(id));
    }

    @GetMapping("/region/{region_name}")
    public ResponseEntity<List<RegionPokemonDto>> getPokemonByRegion(@PathVariable("region_name") String regionName) {
        return ResponseEntity.ok(pokemonService.getPokemonByRegion(regionName));
    }
}
