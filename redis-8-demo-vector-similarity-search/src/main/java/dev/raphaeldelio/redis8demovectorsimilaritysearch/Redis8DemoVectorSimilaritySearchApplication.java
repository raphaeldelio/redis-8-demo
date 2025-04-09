package dev.raphaeldelio.redis8demovectorsimilaritysearch;

import com.redis.om.spring.annotations.EnableRedisEnhancedRepositories;
import dev.raphaeldelio.redis8demovectorsimilaritysearch.service.MovieService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;


@SpringBootApplication
@EnableRedisEnhancedRepositories(basePackages = {"dev.raphaeldelio.redis8demo*"})
public class Redis8DemoVectorSimilaritySearchApplication {

    public static void main(String[] args) {
        SpringApplication.run(Redis8DemoVectorSimilaritySearchApplication.class, args);
    }

    @Bean
    CommandLineRunner loadData(
            MovieService movieService) {
        return args -> {
            if (movieService.isDataLoaded()) {
                System.out.println("Data already loaded. Skipping data load.");
                return;
            }
            movieService.loadAndSaveMovies("movies.json");
        };
    }

}
