package org.ybelikov.bsa.repository;



import java.util.*;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.ybelikov.bsa.entity.Gif;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Repository
public class CacheRepository {

    private static final Map<String, Gif> usersCache = new HashMap<>();

    @Value("${cache.path}")
    private String cachePath;

    @Value("${imagesFormat.extension}")
    private String extension;

    public void saveGifToCache(String id, String keyWord, byte[] gif) {
        try {
            File cacheDirectory = new File(cachePath);
            if (!cacheDirectory.exists()) {
               cacheDirectory.mkdir();
            }
            File keyWordDirectory = new File(cachePath + "/"  + keyWord);
            if (!keyWordDirectory.exists()) {
               keyWordDirectory.mkdir();
            }
            Files.write(Paths.get(cachePath + "/"  + keyWord + "/" + id + extension), gif);
        } catch(IOException ex) {

        }
    }

    public Optional<Gif> findGif(String pathToCache, String query) {
        String pathToQueryDirectory = pathToCache + "/" + query;
        File queryDirectory = new File(pathToQueryDirectory);
        if(!queryDirectory.exists()) {
            return Optional.empty();
        }
        File[] files = queryDirectory.listFiles();
        Random rand = new Random();
        File gifFile = files[rand.nextInt(files.length)];
        String gifPath = gifFile.getAbsolutePath();
        String fileName = gifFile.getName();
        return Optional.of(new Gif(FilenameUtils.removeExtension(fileName), query, gifPath));
    }

    public Optional<List<String>> findGifPathsByQuery(String pathToCache, String query) {
        List<String> gifPaths = new ArrayList<String>();
        File cacheDirectory = new File(pathToCache);
        if (!cacheDirectory.exists()) {
            return Optional.empty();
        }
        File queryDirectory = new File(pathToCache + "/" + query);

        File[] files = queryDirectory.listFiles();
        for (File file : files) {
            gifPaths.add(file.getAbsolutePath());
        }
        return Optional.of(gifPaths);
    }

    public Optional<String> searchGifInMemoryCache(String id, String query) {
        if (!usersCache.containsKey(id)) {
           return Optional.empty();
        }
        var values = usersCache.entrySet();
        for (var pair : values) {
            if (pair.getKey() == id && pair.getValue().getKeyWord() == query) {
                return Optional.of(pair.getValue().getPathToGif());
            }
        }
        return Optional.empty();
    }

    public Optional<Gif> searchGifInUsersDirectoryCache(String pathToUsersImageFolder, String id, String query) {
        String pathToQueryDirectory = pathToUsersImageFolder + "/" + id + "/" + query;
        File queryDirectory = new File(pathToQueryDirectory);
        if(!queryDirectory.exists()) {
            return Optional.empty();
        }
        File[] files = queryDirectory.listFiles();
        Random rand = new Random();
        File gifFile = files[rand.nextInt(files.length)];
        String gifPath = gifFile.getAbsolutePath();
        String fileName = gifFile.getName();
        return Optional.of(new Gif(FilenameUtils.removeExtension(fileName), query, gifPath));
    }

    public void addToMemoryCache(String id, Gif gif) {
        usersCache.put(id, gif);
    }

    public void resetInMemoryCache(String id, String query) {
        if(query == "") {
            resetInMemoryCacheForUser(id);
        }
        for(var pair : usersCache.entrySet()) {
            if (pair.getKey() == id && pair.getValue().getKeyWord() == query) {
                usersCache.remove(id);
            }
        }
    }

    private void resetInMemoryCacheForUser(String id) {
        for(var pair : usersCache.entrySet()) {
            if (pair.getKey() == id) {
                usersCache.remove(id);
            }
        }
    }
    public void cleanUserCache(String id, String pathToPersonalDirectory) throws IOException {
        resetInMemoryCacheForUser(id);
        deleteAll(pathToPersonalDirectory);
    }

    public void deleteUserHistory(String path) {
        File historyFile = new File(path);
        historyFile.delete();
    }

    public void deleteAll(String directoryPath) throws IOException {
        FileUtils.deleteDirectory(new File(directoryPath));
    }


}
