package org.ybelikov.bsa.service;
import java.io.FileNotFoundException;
import java.util.*;

import com.opencsv.CSVWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.ybelikov.bsa.dto.GenerateGifRequestDto;
import org.ybelikov.bsa.dto.GeneratedGifResponseDto;
import org.ybelikov.bsa.dto.GifPathsDto;
import org.ybelikov.bsa.dto.UserHistoryDto;
import org.ybelikov.bsa.entity.Gif;
import org.ybelikov.bsa.repository.CacheRepository;
import org.ybelikov.bsa.util.GeneratedGifResponseDtoToGifMapper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Value("${usersImages.path}")
    private String pathToUsersImageFolder;

    @Value("${cache.path}")
    private String cachePath;

    @Value("${imagesFormat.extension}")
    private String extension;


    private final CacheRepository cacheRepository;
    private final GeneratedGifResponseDtoToGifMapper mapper;
    @Autowired
    public UserService(CacheRepository cacheRepository, GeneratedGifResponseDtoToGifMapper mapper) {
        this.cacheRepository = cacheRepository;
        this.mapper = mapper;
    }

    public Optional<List<UserHistoryDto>> getUserHistory(String id) {
        String userPersonalDirecotryPath = pathToUsersImageFolder + "/" + id;
        File personalDirectory = new File(userPersonalDirecotryPath);
        if (!personalDirectory.exists()) {
            return Optional.empty();
        }
        try {
            Optional<List<UserHistoryDto>> result = cacheRepository.getUserHistory(userPersonalDirecotryPath);
            return result;
        }catch(IOException ex) {
            logger.info(ex.getMessage(), ex);
            return Optional.empty();
        }
    }

    public String saveGif(String userName, GeneratedGifResponseDto gifResponseDto) {
        Gif gif = mapper.map(gifResponseDto);
        gif.setPathToGif(pathToUsersImageFolder + "/" + userName + "/" + gif.getKeyWord() + "/" + gif.getId() + extension);
        createDirectories(userName, gif);
        saveGifTOPersonalDirectory(userName, gif);
        createRecordInHistoryFile(userName, gif);

        return gif.getPathToGif();
    }

    private void createDirectories(String userName, Gif gif) {
        createUsersDirectory();
        createPersonalUserDirectory(userName);
        createKeyWordGifsDirectory(userName, gif.getKeyWord());
    }

    private void createRecordInHistoryFile(String userName, Gif gif) {
        Date date = new Date();
        String currentDate = new SimpleDateFormat("yyyy-mm-dd").format(date);
        try{
           FileWriter out = new FileWriter(pathToUsersImageFolder + "/" + userName + "/" + "history.csv", true);
           CSVWriter writer = new CSVWriter(out);
           List<String[]> lines = new ArrayList<>();
           lines.add(new String[]{currentDate, gif.getKeyWord(), gif.getPathToGif()});
           for (String[] line : lines) {
               writer.writeNext(line);
           }
           writer.close();
       }catch(IOException ex) {
            logger.info(ex.getMessage(), ex);
       }

    }
    private void saveGifTOPersonalDirectory(String userName, Gif gif) {
        try {
            File gifOriginal = new File(cachePath + "/" + gif.getKeyWord() + "/" + gif.getId() + extension);
            File gifCopy = new File(pathToUsersImageFolder + "/" + userName + "/" + gif.getKeyWord()
                    + "/" + gif.getId() + extension);
            com.google.common.io.Files.copy(gifOriginal, gifCopy);
        } catch (IOException ex) {
           logger.info(ex.getMessage(), ex);
        }
    }
    private void createUsersDirectory() {
        File usersDirectory = new File(pathToUsersImageFolder);
        if (!usersDirectory.exists()) {
            usersDirectory.mkdir();
        }
    }

    private void createPersonalUserDirectory(String userName) {
        String userPersonalDirectoryPath = pathToUsersImageFolder + "/" + userName;
        File userPersonalDirectory = new File(userPersonalDirectoryPath);
        if (!userPersonalDirectory.exists()) {
            userPersonalDirectory.mkdir();
        }
    }

    private void createKeyWordGifsDirectory(String userName, String keyWord) {
        String usersImagesDirectoryPath = pathToUsersImageFolder + "/" + userName + "/" + keyWord;
        File userKeyWordDirectory = new File(usersImagesDirectoryPath);
        if (!userKeyWordDirectory.exists()) {
            userKeyWordDirectory.mkdir();
        }
    }

    public void saveGifImage(String key, String keyWord, byte[] gifImage) {
        cacheRepository.saveGifToCache(key, keyWord, gifImage);
    }

    public Optional<Gif> findGif(GenerateGifRequestDto requestDto) {
        return cacheRepository.findGif(cachePath, requestDto.getQuery());
    }

    public Optional<List<GifPathsDto>> findUserAllGifs(String userName) {
        String personalDirectoryPath = pathToUsersImageFolder + "/" + userName;
        File personalDirectory =  new File(personalDirectoryPath);
        if (!personalDirectory.exists()) {
           return Optional.empty();
        }

        File[] subdirectories = personalDirectory.listFiles();
        List<GifPathsDto> result = new ArrayList<>();
       for (int i = 0; i < subdirectories.length && subdirectories[i].isDirectory(); ++i ) {
           result.add(new GifPathsDto(subdirectories[i].getName(),
                   cacheRepository.findGifPathsByQuery(personalDirectoryPath, subdirectories[i].getName()).get()));
       }
       return Optional.of(result);
    }

    public String addGifToUser(String userName, Gif gif) {
        createDirectories(userName, gif);
        saveGifTOPersonalDirectory(userName, gif);
        createRecordInHistoryFile(userName, gif);
        gif.setPathToGif(pathToUsersImageFolder + "/" + userName + "/" + gif.getId() + extension);
        return gif.getPathToGif();
    }

    public Optional<String> searchGifInMemoryCache(String id, String query) {

        Optional<String> pathToGifOptional = cacheRepository.searchGifInMemoryCache(id, query);
        return !pathToGifOptional.isPresent() ? searchGifInFileSystemCache(id, query) : pathToGifOptional;
    }

    public Optional<String> searchGifInFileSystemCache(String id, String query) {
        Optional<Gif> gifFromUserCache = cacheRepository.searchGifInUsersDirectoryCache(pathToUsersImageFolder, id, query);
        if (!gifFromUserCache.isPresent()) {
            return Optional.empty();
        }
        cacheRepository.addToMemoryCache(id, gifFromUserCache.get());
        return Optional.of(gifFromUserCache.get().getPathToGif());
    }

    public void resetInMemoryCache(String id, String query) {
        cacheRepository.resetInMemoryCache(id, query);
    }

    public void deleteUserHistory(String userName) {
        cacheRepository.deleteUserHistory(pathToUsersImageFolder + "/" + userName + "/" + "history.csv");
    }

    public void cleanUserCache(String id) {
        String pathToPersonalDirectory = pathToUsersImageFolder + "/" + id;
        try {
            cacheRepository.cleanUserCache(id, pathToPersonalDirectory);
        }catch (IOException ex) {
            logger.info(ex.getMessage(), ex);
        }
    }

}
