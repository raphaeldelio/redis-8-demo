# Redis 8 with Redis OM Spring Demo Project

This project demonstrates three key features of Redis 8.0:
1. Full-text search capabilities
2. Probabilistic data structures
3. Vector similarity search (VSS)

## Prerequisites

- Docker and Docker Compose
- Java 17 or higher
- Maven

## Project Structure

The project is divided into three main components, each running in its own Redis instance:

1. **Full-text Search Demo** (Port 6379)
   - Demonstrates searching capabilities:
        - Auto completion
        - Text Search
        - Filtering

2. **Probabilistic Data Structures Demo** (Port 6380)
   - Showcases Redis's probabilistic data structures:
        - Count-Min Sketch
        - Bloom Filter
        - TopK

3. **Vector Similarity Search Demo** (Port 6381)
   - Demonstrates Redis's vector search capabilities
   - Perfect for implementing similarity search and recommendation systems
   - Uses vector embeddings for semantic search

## Getting Started

1. **Start the Redis instances**:
   ```bash
   docker-compose up -d
   ```

2. **Build and run each demo**:
   Each demo component has its own directory with specific instructions. Navigate to the respective directory and follow the instructions in their README files.

## License

This project is licensed under the MIT License - see the LICENSE file for details.