package dev.raphaeldelio.redis8demovectorsimilaritysearch.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.raphaeldelio.redis8demovectorsimilaritysearch.domain.Movie;
import dev.raphaeldelio.redis8demovectorsimilaritysearch.repository.MovieRepository;
import lombok.extern.java.Log;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;

@Log
@Service
public class MovieService {

    private final ObjectMapper objectMapper;
    private final ResourceLoader resourceLoader;
    private final MovieRepository movieRepository;

    public MovieService(ObjectMapper objectMapper, ResourceLoader resourceLoader, MovieRepository movieRepository) {
        this.objectMapper = objectMapper;
        this.resourceLoader = resourceLoader;
        this.movieRepository = movieRepository;
    }

    public void loadAndSaveMovies(String filePath) throws Exception {
        Resource resource = resourceLoader.getResource("classpath:" + filePath);
        try (InputStream is = resource.getInputStream()) {
            List<Movie> movies = objectMapper.readValue(is, new TypeReference<>() {});
            List<Movie> unprocessedMovies = movies.stream()
                    .filter(movie -> !movieRepository.existsById(movie.getTitle()) &&
                            movie.getYear() > 1980
                    ).toList();
            long systemMillis = System.currentTimeMillis();
            movieRepository.saveAll(unprocessedMovies);
            long elapsedMillis = System.currentTimeMillis() - systemMillis;
            log.info("Saved " + movies.size() + " movies in " + elapsedMillis + " ms");
        }
    }

    public boolean isDataLoaded() {
        return movieRepository.count() > 0;
    }
}