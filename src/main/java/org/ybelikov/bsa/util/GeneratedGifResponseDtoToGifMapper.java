package org.ybelikov.bsa.util;

import org.springframework.stereotype.Component;
import org.ybelikov.bsa.dto.GeneratedGifResponseDto;
import org.ybelikov.bsa.entity.Gif;

@Component

public final class GeneratedGifResponseDtoToGifMapper {
    public Gif map(GeneratedGifResponseDto response) {
        return new Gif(response.getId(), response.getKeyWord(), "");
    }

}
