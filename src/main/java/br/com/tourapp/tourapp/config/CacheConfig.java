package br.com.tourapp.tourapp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

    @Value("${app.cache.enabled}")
    private boolean cacheEnabled;

    @Bean
    public CacheManager cacheManager() {
        if (!cacheEnabled) {
            return new ConcurrentMapCacheManager(); // Cache desabilitado
        }

        return new ConcurrentMapCacheManager(
            "excursoes",
            "organizadores",
            "clientes",
            "dashboard"
        );
    }
}

