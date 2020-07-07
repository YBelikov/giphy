package org.ybelikov.bsa.controller;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.ybelikov.bsa.dto.GenerateGifRequestDto;
import org.ybelikov.bsa.dto.GeneratedGifResponseDto;
import org.ybelikov.bsa.dto.GifPathsDto;
import org.ybelikov.bsa.entity.Gif;
import org.ybelikov.bsa.service.CacheService;
import org.ybelikov.bsa.service.GifsClient;
import org.ybelikov.bsa.service.RestGiphyApiClient;

import java.util.ArrayList;
import java.util.List;

@RestController
public class CacheController {
    private static final Logger logger = LoggerFactory.getLogger(CacheController.class);

    private final RestGiphyApiClient giphyApiClient;
    private final CacheService cacheService;
    private final GifsClient gifsClient;

    @Autowired
    public CacheController(RestGiphyApiClient giphyApiClient, CacheService cacheService, GifsClient gifsClient) {
        this.giphyApiClient = giphyApiClient;
        this.cacheService = cacheService;
        this.gifsClient = gifsClient;
    }

    @PostMapping("/generate")
    public ResponseEntity<GifPathsDto> generateGif(@RequestBody GenerateGifRequestDto requestDto) {
        GeneratedGifResponseDto responseDto = giphyApiClient.retrieveInformation(requestDto.getQuery());
        byte[] gifImage = gifsClient.loadGif(responseDto.getUrlWithOriginalGif());
        cacheService.saveGifToCache(responseDto.getId(), responseDto.getKeyWord(), gifImage);
        GifPathsDto result = new GifPathsDto(requestDto.getQuery(), cacheService.findGifPathsByQuery(requestDto.getQuery()).get());
        return ResponseEntity.of(Optional.of(result));
    }

    @GetMapping("/cache")
    public ResponseEntity<List<GifPathsDto>> getGifsByQuery(@RequestParam String query) {
        logger.info("Cache GET request");
        List<GifPathsDto> result = new ArrayList<>();
        if (query != "") {
            result.add(new GifPathsDto(query, cacheService.findGifPathsByQuery(query).get()));
            return ResponseEntity.of(Optional.of(result));
        }
        return ResponseEntity.of(cacheService.getAllGifPaths());
    }

    @GetMapping("/gifs")
    public ResponseEntity<List<String>> getGifPaths() {
        logger.info("Gifs GET request");
        return ResponseEntity.of(cacheService.getGifPathsInCache());
    }

    @DeleteMapping("/cache")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void deleteCache() {
        logger.info("Cache DELETE request");
        cacheService.deleteAll();
    }

}
