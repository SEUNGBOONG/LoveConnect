package com.example.demo.config.toss;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.io.InputStream;
import java.security.KeyStore;
import java.time.Duration;

@Configuration
public class TossHttpConfig {

    @Value("${toss.cert.path}")
    private String certPath;

    @Value("${toss.cert.password}")
    private String certPassword;

    @Bean(name = "tossRestTemplate")
    public RestTemplate tossRestTemplate() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (InputStream is = ResourceUtils.getURL(certPath).openStream()) {
            keyStore.load(is, certPassword.toCharArray());
        }

        SSLContext sslContext = SSLContextBuilder.create()
                .loadKeyMaterial(keyStore, certPassword.toCharArray())
                .build();

        SSLConnectionSocketFactory sslSocketFactory = SSLConnectionSocketFactoryBuilder.create()
                .setSslContext(sslContext)
                .build();

        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(PoolingHttpClientConnectionManagerBuilder.create()
                        .setSSLSocketFactory(sslSocketFactory)
                        .build())
                .build();

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        factory.setConnectTimeout((int) Duration.ofSeconds(5).toMillis());

        return new RestTemplate(factory);
    }
}
