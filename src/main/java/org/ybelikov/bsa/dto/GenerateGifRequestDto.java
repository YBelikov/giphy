package org.ybelikov.bsa.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
public class GenerateGifRequestDto {
    private final String query;
    private final boolean forced;
    public GenerateGifRequestDto() {
        query = "";
        forced = false;
    }
}
