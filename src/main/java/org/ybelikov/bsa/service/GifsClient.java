package org.ybelikov.bsa.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class GifsClient {
    private static final Logger logger = LoggerFactory.getLogger(GifsClient.class);

    private final RestTemplate client;
    @Autowired
    public GifsClient(RestTemplate client) {
        this.client = client;
    }

    public byte[] loadGif(String url) {
       return client.getForObject(url, byte[].class);
    }

}
