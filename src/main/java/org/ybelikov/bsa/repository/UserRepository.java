package org.ybelikov.bsa.repository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;


@Repository
public class UserRepository {



    @Value("${usersImages.path}")
    private String pathToUsersImageFolder;

    @Value("${cache.path}")
    private String cachePath;

    @Value("${imagesFormat.extension}")
    private String extension;


}
