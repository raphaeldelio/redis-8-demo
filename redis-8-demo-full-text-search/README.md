# Redis 8.0 Full-text Search Demo

This demo showcases Redis 8's full-text search capabilities using Redis 8 & [Redis OM Spring](https://github.com/redis/redis-om-spring). It implements a movie search system that demonstrates various search features provided by the Redis Query Engine.

## Features
### Autocompletion

Autocomplete (provided by the Redis Query Engine) helps you show word suggestions as someone types. You give it a list of words, and it returns matches that start with the letters typed. It's made to be fast and useful for search boxes.

### Full-text Search

Full-text search (provided by the Redis Query Engine) lets you search for words inside text, not just exact matches. It breaks text into words, stores them in a smart way, and finds results even if they're not an exact match. It's great for searching articles, product names, or any long text.

### Filtering & Sorting

Filtering and sorting (provided by the Redis Query Engine) lets you narrow down and order your search results. You can filter by things like numbers, dates, or tags—for example, "price less than 50" or "category is books." You can also sort results, like showing the newest or cheapest first.

## Getting Started

1. **Start the Redis instance**:
   ```bash
   docker-compose up -d redis-full-text-search
   ```

2. **Build the project**:
   ```bash
   mvn clean install
   ```

3. **Run the application**:
   ```bash
   mvn spring-boot:run
   ```

## API Endpoints

The application exposes the following REST endpoints:

### GET /search/{title}

**Purpose:** Autocomplete movie titles

**Example:** /search/Harry P

**Description:**

Returns a list of movie titles that start with the given prefix with the synopsys as part of the payload. Useful for search boxes.

Response example: 
```
[
  {
    "value": "Harry Potter and the Goblet of Fire",
    "score": 1.0,
    "payload": {
      "extract": "Harry Potter and the Goblet of Fire is a 2005 fantasy film directed by Mike Newell from a screenplay by Steve Kloves, based on the 2000 novel of the same name by J.K. Rowling. It is the sequel to Harry Potter and the Prisoner of Azkaban (2004) and the fourth instalment in the Harry Potter film series. The film stars Daniel Radcliffe as Harry Potter, alongside Rupert Grint and Emma Watson as Harry's best friends Ron Weasley and Hermione Granger respectively. Its story follows Harry's fourth year at Hogwarts as he is chosen by the Goblet of Fire to compete in the Triwizard Tournament."
    }
  },
  ...
]
```

### GET /search

**Purpose:** Search for movies based on the synopsis (extract) or metadata (year, cast, etc)

**Example:** /search?text=ultron&cast=Josh Brolin, Mark Ruffalo

**Description:**

Returns a list of movies that match the search criteria. The search is performed on the synopsis (extract) and metadata fields.

**Response example:**
```
{
  "cast": "Josh Brolin, Mark Ruffalo",
  "matchedTexts": [
    {
      "id": "01JRDD47XJNKQ0500A2Y6BYBAK",
      "title": "Avengers: Infinity War",
      "year": 2018,
      "cast": [
        "Robert Downey Jr.",
        "Chris Hemsworth",
        "Mark Ruffalo",
        "Josh Brolin",
        ...
      ],
      "genres": [
        "Superhero"
      ],
      "href": "Avengers:_Infinity_War",
      "extract": "Avengers: Infinity War is a 2018 American superhero film based on the Marvel Comics superhero team the Avengers. Produced by Marvel Studios and distributed by Walt Disney Studios Motion Pictures, it is the sequel to The Avengers (2012) and Avengers: Age of Ultron (2015), and the 19th film in the Marvel Cinematic Universe (MCU). Directed by Anthony and Joe Russo and written by Christopher Markus and Stephen McFeely, the film features an ensemble cast including Robert Downey Jr., Chris Hemsworth, Mark Ruffalo, Chris Evans, Scarlett Johansson, Benedict Cumberbatch, Don Cheadle, Tom Holland, Chadwick Boseman, Paul Bettany, Elizabeth Olsen, Anthony Mackie, Sebastian Stan, Danai Gurira, Letitia Wright, Dave Bautista, Zoe Saldaña, Josh Brolin, and Chris Pratt. In the film, the Avengers and the Guardians of the Galaxy attempt to prevent Thanos from collecting the six all-powerful Infinity Stones as part of his quest to kill half of all life in the universe.",
      "thumbnail": "https://upload.wikimedia.org/wikipedia/en/4/4d/Avengers_Infinity_War_poster.jpg",
      "thumbnailWidth": 0,
      "thumbnailHeight": 0
    }
  ],
  "text": "ultron"
}
```

### HTTP File
Requests can easily be made with the request.http file included in the project. 
This file contains all the endpoints and their respective request methods. 
You can use it with any HTTP client that supports .http files, such as Intellij, Postman or Insomnia.

## Example Usage

1. **Search for movies**:
   ```bash
   curl -X GET "http://localhost:8080/api/movies/search?query=action&genre=Action&year=2020&rating=7.5"
   ```

2. **Add a new movie**:
   ```bash
   curl -X POST "http://localhost:8080/api/movies" \
   -H "Content-Type: application/json" \
   -d '{
     "title": "Inception",
     "description": "A thief who steals corporate secrets through the use of dream-sharing technology...",
     "genre": "Sci-Fi",
     "year": 2010,
     "rating": 8.8
   }'
   ```

## Implementation Details

Full text search is implemented using Redis OM Spring, which provides a simple and efficient way to interact with Redis. The following sections detail the implementation of the movie search system.

### Project Setup

1. **Add Redis OM Spring Dependencies**
   Add the following dependencies to your `pom.xml`:
   ```xml
   <dependency>
       <groupId>com.redis.om.spring</groupId>
       <artifactId>redis-om-spring</artifactId>
       <version>0.9.10</version>
   </dependency>
   ```

### Entity Definition

Create a Movie entity with Redis OM Spring annotations:

```java
@Document // Defines a JSON document in Redis
public class Movie {
    @Id // ID is created automatically by Redis OM Spring as ULID
    private String id;
    
    @AutoComplete // Enables autocomplete on the title
    private String title;
    
    @Searchable // Enables full-text search on the synopsis
    @AutoCompletePayload("title") // Returns the synopsis as part of the payload of the title autocompletion
    private String extract;
    
    @Indexed // Enables indexing on the year
    private Integer year;
    
    @Indexed // Enables indexing on the cast
    private List<String> cast;
    
    @Indexed // Enables indexing on the genres
    private List<String> genres;
    
    // Getters and setters
}
```

### Repository Setup

Create a repository interface extending `RedisDocumentRepository`:

```java
@Repository
public interface MovieRepository extends RedisDocumentRepository<Movie, String> {
    
    // Autocomplete method for movie titles
    List<Suggestion> autoCompleteTitle(String title, AutoCompleteOptions options);
}
```

### Service Implementation

Implement the search functionality in a service class:

```java
@Service
public class SearchService {

   private static final Logger logger = LoggerFactory.getLogger(SearchService.class);

   /** Entity Strem is a Redis OM Spring abstraction that allows you to create a stream of entities
    * to perform operations on them. It works by creating a command that will be executed by Redis.
    */
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

      // This is not the Java Streams API. 
      // Filtering and sorting is done by Redis.
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
```

### Key Features Explained

1. **@Document Annotation**
   - Marks a class as a Redis document

2. **@Searchable Annotation**
   - Enables full-text search on the annotated field

3. **@Indexed Annotation**
   - Enables filtering and sorting operations

4. **@AutoComplete Annotation**
   - Enables autocomplete functionality on the annotated field

5. **@AutoCompletePayload Annotation**
   - Specifies the field to be returned as part of the payload during autocomplete

6. **EntityStream**
   - Provides a fluent API for building Redis queries

### Additional Resources

- [Redis OM Spring Documentation](https://github.com/redis/redis-om-spring)
- [Redis Search Documentation](https://redis.io/docs/latest/develop/interact/search-and-query/)

