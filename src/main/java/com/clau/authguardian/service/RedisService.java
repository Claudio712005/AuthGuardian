package com.clau.authguardian.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RedisService {

  private final RedisTemplate<String, String> redisTemplate;
  private final String BLACKLIST_KEY = "blacklist";

  public RedisService(@Qualifier("customRedisTemplate")  RedisTemplate<String, String> privatredisTemplate) {
    this.redisTemplate = privatredisTemplate;
  }

  public void addToBlacklist(String token) {
    redisTemplate.opsForSet().add(BLACKLIST_KEY, token);
    redisTemplate.expire(BLACKLIST_KEY, Duration.ofDays(7));
  }

  public boolean isTokenBlacklisted(String token) {
    return redisTemplate.opsForSet().isMember(BLACKLIST_KEY, token);
  }

  public void removeFromBlacklist(String token) {
    redisTemplate.opsForSet().remove(BLACKLIST_KEY, token);
  }

  public void incrementInRedis(String key) {
    redisTemplate.opsForValue().increment(key, 1);
    redisTemplate.expire(key, Duration.ofMinutes(5));
  }

  public void setInRedis(String key, String value) {
    redisTemplate.opsForValue().set(key, value);
  }

  public String getFromRedis(String key) {
    return redisTemplate.opsForValue().get(key);
  }

  public void removeFromRedis(String key) {
    redisTemplate.delete(key);
  }
}
