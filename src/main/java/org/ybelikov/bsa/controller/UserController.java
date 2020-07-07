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
import org.ybelikov.bsa.dto.UserHistoryDto;
import org.ybelikov.bsa.entity.Gif;
import org.ybelikov.bsa.service.GifsClient;
import org.ybelikov.bsa.service.RestGiphyApiClient;
import org.ybelikov.bsa.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/user")

public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final RestGiphyApiClient apiClient;
    private final GifsClient gifsClient;
    private final UserService userService;
    @Autowired
    public UserController(RestGiphyApiClient apiClient, GifsClient gifsClient, UserService userService) {
        this.apiClient = apiClient;
        this.gifsClient = gifsClient;
        this.userService = userService;
    }

    @PostMapping("/{id}/generate")
    public String generateGif(@PathVariable String id, @RequestBody GenerateGifRequestDto generateGifRequestDto) {
        logger.info("Gif generation POST request");
        if(generateGifRequestDto.isForced()) {
            return loadGif(id, generateGifRequestDto);
        }
        Optional<Gif> gif = userService.findGif(generateGifRequestDto);
        if(gif.isPresent()) {
            return userService.addGifToUser(id, gif.get());
        }
        return loadGif(id, generateGifRequestDto);
    }
    @GetMapping("/{id}/history")
    public ResponseEntity<List<UserHistoryDto>> getUserHistory(@PathVariable String id) {
        logger.info("GET request for user history");
        return ResponseEntity.of(userService.getUserHistory(id));
    }
    @GetMapping("/{id}/all")
    public ResponseEntity<List<GifPathsDto>> allUserFiles(@PathVariable String id) {
        logger.info("All users gifs GET request");
        return ResponseEntity.of(userService.findUserAllGifs(id));
    }

    @GetMapping("/{id}/search")
    public ResponseEntity<String> searchGif(@PathVariable String id, @RequestParam String query, @RequestParam boolean force) {
        logger.info("Search gif GET request");
        if (!force) {
            return ResponseEntity.of(userService.searchGifInFileSystemCache(id, query));
        }
        Optional<String> pathToGifInMemoryCache = userService.searchGifInMemoryCache(id, query);
        if(pathToGifInMemoryCache.isPresent()) return ResponseEntity.of(pathToGifInMemoryCache);
        Optional<String> pathToGifInFileSystemCache = userService.searchGifInFileSystemCache(id, query);
        if(pathToGifInFileSystemCache.isPresent()) return ResponseEntity.of(pathToGifInFileSystemCache);
        return new ResponseEntity<>("Can't found such gif", HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}/history/clean")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void deleteUserHistory(@PathVariable String id) {
        logger.info("Delete user history");
        userService.deleteUserHistory(id);
    }

    @DeleteMapping("/{id}/reset")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void resetInMemoryCache(@PathVariable String id, @RequestParam String query) {
        logger.info("In memory cache clearing DELETE request");
        userService.resetInMemoryCache(id, query);
    }

    @DeleteMapping("/{id}/clean")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void cleanUserCache(@PathVariable String id) {
        logger.info("All users caches clearing DELETE request");
        userService.cleanUserCache(id);
    }

    private String loadGif(String id, GenerateGifRequestDto requestDto) {
        GeneratedGifResponseDto responseDto = apiClient.retrieveInformation(requestDto.getQuery());
        byte[] gifImage = gifsClient.loadGif(responseDto.getUrlWithOriginalGif());
        userService.saveGifImage(responseDto.getId(),responseDto.getKeyWord(), gifImage);
        return userService.saveGif(id, responseDto);
    }

}
