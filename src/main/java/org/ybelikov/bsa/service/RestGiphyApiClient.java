package org.ybelikov.bsa.service;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.json.JSONArray;
import org.ybelikov.bsa.dto.GeneratedGifResponseDto;

@Component
public class RestGiphyApiClient implements GiphyApiClient {
    private static final Logger logger = LoggerFactory.getLogger(RestGiphyApiClient.class);

    @Value("${giphy.searchingUrl}")
    private String url;

    @Value("${giphy.key}")
    private String key;

    private final RestTemplate client;

    @Autowired
    public RestGiphyApiClient(RestTemplate client) {
        this.client = client;
    }

    @Override
    public String getGifAsJson(String keyWord) {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
            builder.queryParam("api_key", key);
            builder.queryParam("q", keyWord);
            builder.queryParam("limit", 1);
            HttpEntity<?> entity = new HttpEntity<>(headers);
            System.out.println(builder.toUriString());
            HttpEntity<String> responseEntity = client.exchange(builder.toUriString(), HttpMethod.GET, entity, String.class);
            logger.info("Response:" + responseEntity.getBody());
            return responseEntity.getBody();
    }

    public GeneratedGifResponseDto retrieveInformation(String keyWord) {
        String gifBody = getGifAsJson(keyWord);
        JSONObject gifObject = new JSONObject(gifBody);
        JSONArray data = new JSONArray(gifObject.get("data").toString());
        JSONObject allData = new JSONObject(data.get(0).toString());
        JSONObject images = new JSONObject(allData.get("images").toString());
        JSONObject previewGif = new JSONObject(images.get("preview_gif").toString());
        return new GeneratedGifResponseDto(allData.get("id").toString(), keyWord, previewGif.get("url").toString());
    }
}
