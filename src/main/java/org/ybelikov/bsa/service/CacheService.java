package org.ybelikov.bsa.service;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.ybelikov.bsa.dto.GifPathsDto;
import org.ybelikov.bsa.repository.CacheRepository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service
public class CacheService {

    private static final Logger logger = LoggerFactory.getLogger(CacheService.class);

    @Value("${cache.path}")
    private String cachePath;

    @Value("${imagesFormat.extension}")
    private String extension;

    private final CacheRepository cacheRepository;

    @Autowired
    public CacheService(CacheRepository cacheRepository) {
        this.cacheRepository = cacheRepository;
    }
    public Optional<List<String>> findGifPathsByQuery(String query) {
        return cacheRepository.findGifPathsByQuery(cachePath, query);
    }

    public void saveGifToCache(String id, String keyWord, byte[] gif) {
        cacheRepository.saveGifToCache(id, keyWord, gif);
    }

    public Optional<List<GifPathsDto>> getAllGifPaths() {
        List<GifPathsDto> gifPaths = new ArrayList<>();
        File cacheDirectory = new File(cachePath);
        if(!cacheDirectory.exists()) {
            return Optional.empty();
        }
        for (File queryDirectory : cacheDirectory.listFiles()) {
            String query = queryDirectory.getName();
            gifPaths.add(new GifPathsDto(query, cacheRepository.findGifPathsByQuery(cachePath, query).get()));
        }
        return Optional.of(gifPaths);
    }

    public Optional<List<String>> getGifPathsInCache() {
        List<String> paths = new ArrayList<>();
        File cacheDirectory = new File(cachePath);
        if(!cacheDirectory.exists()) {
            return Optional.empty();
        }
        for (File queryDirectory : cacheDirectory.listFiles()) {
            String query = queryDirectory.getName();
            paths.addAll(cacheRepository.findGifPathsByQuery(cachePath, query).get());
        }
        return Optional.of(paths);

    }

    public void deleteAll() {
        try {
            cacheRepository.deleteAll(cachePath);
        } catch (IOException ex) {
            logger.info(ex.getMessage(), ex);
        }
    }


}
