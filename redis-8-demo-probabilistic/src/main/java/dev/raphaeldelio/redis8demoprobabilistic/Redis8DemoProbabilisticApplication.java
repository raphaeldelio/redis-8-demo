package dev.raphaeldelio.redis8demoprobabilistic;

import com.redis.om.spring.annotations.EnableRedisEnhancedRepositories;
import dev.raphaeldelio.redis8demoprobabilistic.domain.Movie;
import dev.raphaeldelio.redis8demoprobabilistic.service.MovieService;
import dev.raphaeldelio.redis8demoprobabilistic.service.RedisService;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootApplication
@EnableRedisEnhancedRepositories(basePackages = {"dev.raphaeldelio.redis8demoprobabilistic*"})
public class Redis8DemoProbabilisticApplication {

    private static final Logger logger = LoggerFactory.getLogger(Redis8DemoProbabilisticApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(Redis8DemoProbabilisticApplication.class, args);
    }

    @Bean
    CommandLineRunner loadData(
            RedisService redisService,
            MovieService movieService) {
        return args -> {
            redisService.flushAll();

            logger.info("Loading movies...");
            List<Movie> movies = movieService.loadMovies("movies.json");

            redisService.initializeTopK("movies:actor-topk", 10);
            redisService.initializeBloomFilter("movies:indexed-urls-bf", 100000, 0.05);
            redisService.initializeCountMinSketch("movies:extract-words-cms", 2000, 10);

            long systemMillis = System.currentTimeMillis();
            AtomicInteger counter = new AtomicInteger();
            movies.forEach(movie -> {
                movie.getCast().forEach(actor -> {
                    redisService.incrementTopK("movies:actor-topk", actor);
                    redisService.incrementZSet("movies:actor-zset", actor);
                });

                if (movie.getExtract() != null && counter.get() < 1000) {
                    List<String> cleanedWords = cleanText(movie.getExtract());
                    cleanedWords.forEach(word -> {
                        redisService.incrementCmsCount("movies:extract-words-cms", word);
                        redisService.incrementZSet("movies:extract-words-zset", word);
                    });
                    counter.getAndIncrement();
                }

                if (movie.getHref() != null) {
                    redisService.addBloomFilter("movies:indexed-urls-bf", movie.getHref());
                    redisService.incrementSet("movies:indexed-urls-set", movie.getHref());
                }
            });
            long elapsedMillis = System.currentTimeMillis() - systemMillis;
            logger.info("Saved {} movies in {} ms", movies.size(), elapsedMillis);
            logger.info("Movies loaded.");
        };
    }

    private List<String> cleanText(String text) {
        try (Analyzer analyzer = new StandardAnalyzer()) {
            TokenStream stream = analyzer.tokenStream("", new StringReader(text));
            stream.reset();

            List<String> result = new ArrayList<>();
            while (stream.incrementToken()) {
                result.add(stream.getAttribute(CharTermAttribute.class).toString());
            }
            stream.end();
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}



/*
ZREVRANGE  movies:actor-zset 0 10 WITHSCORES
TOPK.LIST movies:actor-topk WITHCOUNT

ZREMRANGEBYRANK  movies:actor-zset 0 -11

ZMSCORE movies:extract-words-zset scene movie beautiful comedy release paramount
CMS.QUERY movies:extract-words-cms scene movie beautiful comedy release paramount

BF.EXISTS movies:indexed-urls-bf Donnie_Darko
 */