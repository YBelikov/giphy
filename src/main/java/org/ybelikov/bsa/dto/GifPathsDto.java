package org.ybelikov.bsa.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class GifPathsDto {
    private String query;
    private List<String> gifs;
}
