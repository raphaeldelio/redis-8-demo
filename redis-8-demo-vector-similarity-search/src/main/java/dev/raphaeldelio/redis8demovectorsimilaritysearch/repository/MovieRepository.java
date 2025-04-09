package dev.raphaeldelio.redis8demovectorsimilaritysearch.repository;

import com.redis.om.spring.repository.RedisEnhancedRepository;
import dev.raphaeldelio.redis8demovectorsimilaritysearch.domain.Movie;

public interface MovieRepository extends RedisEnhancedRepository<Movie, String> {
}