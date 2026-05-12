package com.academia.pokemon.service;

import com.academia.pokemon.dto.EvolutionChainDto;
import com.academia.pokemon.dto.PokemonDto;
import com.academia.pokemon.dto.RegionPokemonDto;
import com.academia.pokemon.dto.StrongestPokemonDto;
import com.academia.pokemon.dto.TypeDominanceDto;
import com.academia.pokemon.model.PokeApiEvolutionChainResponse;
import com.academia.pokemon.model.PokeApiListResponse;
import com.academia.pokemon.model.PokeApiPokemonDetail;
import com.academia.pokemon.model.PokeApiRegionResponse;
import com.academia.pokemon.model.PokeApiGenerationResponse;
import com.academia.pokemon.model.PokeApiSpeciesResponse;
import com.academia.pokemon.model.PokeApiTypeResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import java.util.ArrayList;

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

    public EvolutionChainDto getEvolutionChain(Integer id) {
        String speciesUrl = POKEAPI_BASE_URL + "/pokemon-species/" + id;
        PokeApiSpeciesResponse speciesResponse;
        try {
            speciesResponse = restTemplate.getForObject(speciesUrl, PokeApiSpeciesResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("Pokemon species not found for id: " + id, e);
        }

        if (speciesResponse == null || speciesResponse.getEvolutionChain() == null) {
            throw new RuntimeException("Evolution chain not found for id: " + id);
        }

        PokeApiEvolutionChainResponse chainResponse = restTemplate.getForObject(
                speciesResponse.getEvolutionChain().getUrl(), PokeApiEvolutionChainResponse.class);

        if (chainResponse == null || chainResponse.getChain() == null) {
            throw new RuntimeException("Evolution chain details not found");
        }

        List<EvolutionChainDto.EvolutionStepDto> steps = new ArrayList<>();
        parseEvolutionChain(chainResponse.getChain(), steps, 1);

        String baseName = chainResponse.getChain().getSpecies() != null ? 
                chainResponse.getChain().getSpecies().getName() : "";

        return EvolutionChainDto.builder()
                .pokemonBase(baseName)
                .cadena(steps)
                .build();
    }

    private void parseEvolutionChain(PokeApiEvolutionChainResponse.ChainLink link, 
                                     List<EvolutionChainDto.EvolutionStepDto> steps, 
                                     int order) {
        if (link == null || link.getSpecies() == null) return;

        Integer minLevel = null;
        if (link.getEvolutionDetails() != null && !link.getEvolutionDetails().isEmpty()) {
            minLevel = link.getEvolutionDetails().get(0).getMinLevel();
        }

        steps.add(EvolutionChainDto.EvolutionStepDto.builder()
                .nombre(link.getSpecies().getName())
                .orden(order)
                .minLevel(minLevel)
                .build());

        if (link.getEvolvesTo() != null && !link.getEvolvesTo().isEmpty()) {
            // we assume a linear evolution chain for the requirement as example shows linear chains
            parseEvolutionChain(link.getEvolvesTo().get(0), steps, order + 1);
        }
    }

    public List<RegionPokemonDto> getPokemonByRegion(String regionName) {
        String regionUrl = POKEAPI_BASE_URL + "/region/" + regionName.toLowerCase();
        PokeApiRegionResponse regionResponse;
        try {
            regionResponse = restTemplate.getForObject(regionUrl, PokeApiRegionResponse.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, 
                "Region '" + regionName + "' no encontrada. Regiones válidas: kanto, johto, hoenn, sinnoh, unova, kalos, alola, galar");
        } catch (Exception e) {
            throw new RuntimeException("Error fetching region", e);
        }

        if (regionResponse == null || regionResponse.getMainGeneration() == null) {
            return List.of();
        }

        PokeApiGenerationResponse genResponse = restTemplate.getForObject(
                regionResponse.getMainGeneration().getUrl(), PokeApiGenerationResponse.class);

        if (genResponse == null || genResponse.getPokemonSpecies() == null) {
            return List.of();
        }

        Integer genId = genResponse.getId();

        return genResponse.getPokemonSpecies().parallelStream()
                .map(species -> {
                    String speciesUrl = species.getUrl();
                    String[] parts = speciesUrl.split("/");
                    int pId = Integer.parseInt(parts[parts.length - 1]);
                    return pId;
                })
                .map(pId -> {
                    try {
                        return restTemplate.getForObject(POKEAPI_BASE_URL + "/pokemon/" + pId, PokeApiPokemonDetail.class);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .map(detail -> {
                    List<String> types = detail.getTypes() != null ?
                            detail.getTypes().stream()
                                    .map(t -> t.getType().getName())
                                    .collect(Collectors.toList()) : List.of();

                    return RegionPokemonDto.builder()
                            .id(detail.getId())
                            .nombre(detail.getName())
                            .tipos(types)
                            .generacion(genId)
                            .build();
                })
                .sorted((p1, p2) -> p1.getId().compareTo(p2.getId()))
                .collect(Collectors.toList());
    }

    public StrongestPokemonDto getStrongestPokemon(Integer generation) {
        if (generation < 1 || generation > 8) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, 
                "Generación '" + generation + "' no encontrada. Generaciones válidas: 1, 2, 3, 4, 5, 6, 7, 8");
        }

        String genUrl = POKEAPI_BASE_URL + "/generation/" + generation;
        PokeApiGenerationResponse genResponse;
        try {
            genResponse = restTemplate.getForObject(genUrl, PokeApiGenerationResponse.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, 
                "Generación '" + generation + "' no encontrada. Generaciones válidas: 1, 2, 3, 4, 5, 6, 7, 8");
        } catch (Exception e) {
            throw new RuntimeException("Error fetching generation", e);
        }

        if (genResponse == null || genResponse.getPokemonSpecies() == null) {
            throw new RuntimeException("Generation data not found");
        }

        PokeApiPokemonDetail strongest = genResponse.getPokemonSpecies().parallelStream()
                .map(species -> {
                    String[] parts = species.getUrl().split("/");
                    return Integer.parseInt(parts[parts.length - 1]);
                })
                .map(pId -> {
                    try {
                        return restTemplate.getForObject(POKEAPI_BASE_URL + "/pokemon/" + pId, PokeApiPokemonDetail.class);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .max((p1, p2) -> Integer.compare(getTotalStats(p1), getTotalStats(p2)))
                .orElse(null);

        if (strongest == null) {
            throw new RuntimeException("No pokemon found in generation");
        }

        List<String> types = strongest.getTypes() != null ?
                strongest.getTypes().stream().map(t -> t.getType().getName()).collect(Collectors.toList()) : List.of();

        StrongestPokemonDto.StatsDto statsDto = StrongestPokemonDto.StatsDto.builder()
                .hp(getStat(strongest, "hp"))
                .attack(getStat(strongest, "attack"))
                .defense(getStat(strongest, "defense"))
                .specialAttack(getStat(strongest, "special-attack"))
                .specialDefense(getStat(strongest, "special-defense"))
                .speed(getStat(strongest, "speed"))
                .build();

        StrongestPokemonDto.PokemonDetailDto detailDto = StrongestPokemonDto.PokemonDetailDto.builder()
                .id(strongest.getId())
                .nombre(strongest.getName())
                .totalBaseStats(getTotalStats(strongest))
                .stats(statsDto)
                .tipos(types)
                .build();

        return StrongestPokemonDto.builder()
                .generacion(generation)
                .pokemon(detailDto)
                .build();
    }

    private int getTotalStats(PokeApiPokemonDetail detail) {
        if (detail.getStats() == null) return 0;
        return detail.getStats().stream()
                .mapToInt(PokeApiPokemonDetail.StatEntry::getBaseStat)
                .sum();
    }
}
