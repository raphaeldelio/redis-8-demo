package dev.raphaeldelio.redis8demofulltextsearch.service;

import com.redis.om.spring.search.stream.EntityStream;
import com.redis.om.spring.search.stream.SearchStream;
import dev.raphaeldelio.redis8demofulltextsearch.domain.Movie;
import dev.raphaeldelio.redis8demofulltextsearch.domain.Movie$;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearchService {

    private static final Logger logger = LoggerFactory.getLogger(SearchService.class);
    private final EntityStream entityStream;

    public SearchService(EntityStream entityStream) {
        this.entityStream = entityStream;
    }

    public List<Movie> searchByExtractAndCast(
            String query,
            List<String> actors,
            Integer year,
            List<String> genres
    ) {
        logger.info("Received text: {}", query);
        logger.info("Received cast: {}", actors);
        logger.info("Received year: {}", year);
        logger.info("Received genres: {}", genres);

        SearchStream<Movie> stream = entityStream.of(Movie.class);
        return stream
                .filter(Movie$.EXTRACT.containing(query))
                .filter(Movie$.CAST.eq(actors))
                .filter(Movie$.YEAR.eq(year))
                .filter(Movie$.GENRES.eq(genres))
                .sorted(Movie$.YEAR)
                .collect(Collectors.toList());
    }
}
