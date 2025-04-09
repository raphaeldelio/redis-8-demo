package dev.raphaeldelio.redis8demofulltextsearch.repository;

import com.redis.om.spring.autocomplete.Suggestion;
import com.redis.om.spring.repository.RedisDocumentRepository;
import com.redis.om.spring.repository.query.autocomplete.AutoCompleteOptions;
import dev.raphaeldelio.redis8demofulltextsearch.domain.Movie;

import java.util.List;

public interface MovieRepository extends RedisDocumentRepository<Movie, String> {
    List<Suggestion> autoCompleteTitle(String title, AutoCompleteOptions options);
}