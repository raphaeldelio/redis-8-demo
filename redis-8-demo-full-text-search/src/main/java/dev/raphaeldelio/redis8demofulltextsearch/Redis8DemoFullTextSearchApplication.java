package dev.raphaeldelio.redis8demofulltextsearch;

import com.redis.om.spring.annotations.EnableRedisDocumentRepositories;
import dev.raphaeldelio.redis8demofulltextsearch.service.MovieService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableRedisDocumentRepositories(basePackages = {"dev.raphaeldelio.redis8demo*"})
public class Redis8DemoFullTextSearchApplication {
    public static void main(String[] args) {
        SpringApplication.run(Redis8DemoFullTextSearchApplication.class, args);
    }

    @Bean
    CommandLineRunner loadData(MovieService movieService) {
        return args -> {
            if (movieService.isDataLoaded()) {
                System.out.println("Data already loaded. Skipping data load.");
                return;
            }
            movieService.loadAndSaveMovies("movies.json");
        };
    }
}