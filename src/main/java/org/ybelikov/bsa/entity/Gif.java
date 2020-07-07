package org.ybelikov.bsa.entity;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;


@AllArgsConstructor
@Data
public class Gif {
    private String id;
    private String keyWord;
    private String pathToGif;
}
