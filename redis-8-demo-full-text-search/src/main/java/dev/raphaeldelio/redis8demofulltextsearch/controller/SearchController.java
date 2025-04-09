package dev.raphaeldelio.redis8demofulltextsearch.controller;

import com.redis.om.spring.autocomplete.Suggestion;
import com.redis.om.spring.repository.query.autocomplete.AutoCompleteOptions;
import dev.raphaeldelio.redis8demofulltextsearch.domain.Movie;
import dev.raphaeldelio.redis8demofulltextsearch.repository.MovieRepository;
import dev.raphaeldelio.redis8demofulltextsearch.service.SearchService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class SearchController {

    private final SearchService searchService;
    private final MovieRepository movieRepository;

    public SearchController(SearchService searchService, MovieRepository movieRepository) {
        this.searchService = searchService;
        this.movieRepository = movieRepository;
    }

    @GetMapping("/search/{q}")
    public List<Suggestion> query(@PathVariable("q") String query) {
        return movieRepository
                .autoCompleteTitle(query, AutoCompleteOptions.get().withPayload());
    }

    @GetMapping("/search")
    public Map<String, Object> searchByExtract(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<String> cast,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) List<String> genres
    ) {
        List<Movie> matchedMovies = searchService.searchByExtractAndCast(text, cast, year, genres);
        return Map.of(
                "movies", matchedMovies,
                "count", matchedMovies.size()
        );
    }
}
