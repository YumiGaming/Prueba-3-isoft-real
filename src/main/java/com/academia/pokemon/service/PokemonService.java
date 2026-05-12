package com.academia.pokemon.service;

import com.academia.pokemon.dto.PokemonDto;
import com.academia.pokemon.dto.TypeDominanceDto;
import com.academia.pokemon.model.PokeApiListResponse;
import com.academia.pokemon.model.PokeApiPokemonDetail;
import com.academia.pokemon.model.PokeApiTypeResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class PokemonService {

    private final RestTemplate restTemplate;
    private static final String POKEAPI_BASE_URL = "https://pokeapi.co/api/v2";

    public PokemonService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<PokemonDto> getWeakDefensePokemons() {
        String url = POKEAPI_BASE_URL + "/pokemon?limit=150";
        PokeApiListResponse listResponse = restTemplate.getForObject(url, PokeApiListResponse.class);

        if (listResponse == null || listResponse.getResults() == null) {
            return List.of();
        }

        return listResponse.getResults().parallelStream()
                .map(result -> restTemplate.getForObject(result.getUrl(), PokeApiPokemonDetail.class))
                .filter(Objects::nonNull)
                .filter(this::hasWeakDefense)
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private boolean hasWeakDefense(PokeApiPokemonDetail detail) {
        int defense = getStat(detail, "defense");
        int spDefense = getStat(detail, "special-defense");
        return defense > 0 && defense < 40 && spDefense > 0 && spDefense < 40;
    }

    private int getStat(PokeApiPokemonDetail detail, String statName) {
        if (detail.getStats() == null) return 0;
        return detail.getStats().stream()
                .filter(s -> s.getStat() != null && statName.equals(s.getStat().getName()))
                .map(PokeApiPokemonDetail.StatEntry::getBaseStat)
                .findFirst()
                .orElse(0);
    }

    private PokemonDto mapToDto(PokeApiPokemonDetail detail) {
        List<String> types = detail.getTypes() != null ?
                detail.getTypes().stream()
                        .map(t -> t.getType().getName())
                        .collect(Collectors.toList()) : List.of();

        return PokemonDto.builder()
                .id(detail.getId())
                .nombre(detail.getName())
                .defense(getStat(detail, "defense"))
                .spDefense(getStat(detail, "special-defense"))
                .tipos(types)
                .build();
    }

    public TypeDominanceDto getTypeDominance(String type) {
        String url = POKEAPI_BASE_URL + "/type/" + type;
        PokeApiTypeResponse typeResponse;
        try {
            typeResponse = restTemplate.getForObject(url, PokeApiTypeResponse.class);
        } catch (Exception e) {
            // Type might not exist or other error
            throw new RuntimeException("Error fetching type from PokeAPI", e);
        }

        if (typeResponse == null || typeResponse.getPokemon() == null) {
            return TypeDominanceDto.builder().tipo(type).totalPokemon(0).build();
        }

        List<String> gen1PokemonUrls = typeResponse.getPokemon().stream()
                .map(p -> p.getPokemon().getUrl())
                .filter(this::isGen1)
                .collect(Collectors.toList());

        if (gen1PokemonUrls.isEmpty()) {
            return TypeDominanceDto.builder().tipo(type).totalPokemon(0).build();
        }

        PokeApiPokemonDetail mostExperienced = gen1PokemonUrls.parallelStream()
                .map(u -> restTemplate.getForObject(u, PokeApiPokemonDetail.class))
                .filter(Objects::nonNull)
                .max((p1, p2) -> Integer.compare(
                        p1.getBaseExperience() == null ? 0 : p1.getBaseExperience(),
                        p2.getBaseExperience() == null ? 0 : p2.getBaseExperience()
                )).orElse(null);

        TypeDominanceDto.ExperiencedPokemonDto expDto = null;
        if (mostExperienced != null) {
            expDto = TypeDominanceDto.ExperiencedPokemonDto.builder()
                    .id(mostExperienced.getId())
                    .nombre(mostExperienced.getName())
                    .baseExperience(mostExperienced.getBaseExperience())
                    .build();
        }

        return TypeDominanceDto.builder()
                .tipo(type)
                .totalPokemon(gen1PokemonUrls.size())
                .pokemonMasExperimentado(expDto)
                .build();
    }

    private boolean isGen1(String url) {
        try {
            String[] parts = url.split("/");
            int id = Integer.parseInt(parts[parts.length - 1]);
            return id >= 1 && id <= 151;
        } catch (Exception e) {
            return false;
        }
    }
}
