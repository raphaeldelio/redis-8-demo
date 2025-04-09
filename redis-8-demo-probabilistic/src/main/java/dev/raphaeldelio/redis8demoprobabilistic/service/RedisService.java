package dev.raphaeldelio.redis8demoprobabilistic.service;

import com.redis.om.spring.client.RedisModulesClient;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.ops.pds.CountMinSketchOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.ZAddParams;

@Service
public class RedisService {

    private static final Logger logger = LoggerFactory.getLogger(RedisService.class);
    private final RedisModulesClient redisModulesClient;
    private final CountMinSketchOperations<String> countMinSketchOperations;

    public RedisService(RedisModulesClient redisModulesClient, RedisModulesOperations<String> redisModulesOperations) {
        this.redisModulesClient = redisModulesClient;
        this.countMinSketchOperations = redisModulesOperations.opsForCountMinSketch();
    }

    public void flushAll() {
        try (Jedis jedis = redisModulesClient.getJedis().get()) {
            jedis.select(0);
            jedis.flushAll();
        } catch (Exception e) {
            logger.error("Failed to flush database", e);
        }
    }

    public void incrementZSet(String key, String member) {
        try (Jedis jedis = redisModulesClient.getJedis().get()) {
            jedis.zaddIncr(key, 1.0, member, ZAddParams.zAddParams());
        } catch (Exception e) {
            logger.error("Failed to increment ZSet: {} for member: {}", key, member, e);
        }
    }

    public void incrementSet(String key, String member) {
        try (Jedis jedis = redisModulesClient.getJedis().get()) {
            jedis.sadd(key, member);
        } catch (Exception e) {
            logger.error("Failed to increment Set: {} for member: {}", key, member, e);
        }
    }

    public void incrementCmsCount(String key, String item) {
        try {
            countMinSketchOperations.cmsIncrBy(key, item, 1);
        } catch (Exception e) {
            logger.error("Failed to increment word count for: {}", item, e);
        }
    }

    public void initializeCountMinSketch(String key, long width, long depth) {
        try {
            countMinSketchOperations.cmsInitByDim(key, width, depth);
        } catch (Exception e) {
            logger.error("Failed to initialize CountMinSketch for key: {}", key, e);
        }
    }

    public void initializeTopK(String key, long k) {
        try {
            redisModulesClient.clientForTopK().topkReserve(key, k, 100, 7, 0.9);
        } catch (Exception e) {
            logger.error("Failed to initialize TopK for key: {}", key, e);
        }
    }

    public void incrementTopK(String key, String item) {
        try {
            redisModulesClient.clientForTopK().topkIncrBy(key, item, 1);
        } catch (Exception e) {
            logger.error("Failed to increment TopK for key: {}", key, e);
        }
    }

    public void initializeBloomFilter(String key, long capacity, double errorRate) {
        try {
            redisModulesClient.clientForBloom().bfReserve(key, errorRate, capacity);
        } catch (Exception e) {
            logger.error("Failed to initialize BloomFilter for key: {}", key, e);
        }
    }

    public void addBloomFilter(String key, String item) {
        try {
            redisModulesClient.clientForBloom().bfAdd(key, item);
        } catch (Exception e) {
            logger.error("Failed to add item to BloomFilter for key: {}", key, e);
        }
    }
}
