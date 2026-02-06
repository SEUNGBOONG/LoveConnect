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
        // 1. KeyStore 로드 (.p12 인증서)
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (InputStream is = ResourceUtils.getURL(certPath).openStream()) {
            keyStore.load(is, certPassword.toCharArray());
        }

        // 2. SSLContext 생성 (인증서 포함)
        SSLContext sslContext = SSLContextBuilder.create()
                .loadKeyMaterial(keyStore, certPassword.toCharArray())
                .build();

        // 3. SSLConnectionSocketFactory 설정 (HttpClient 5 방식)
        SSLConnectionSocketFactory sslSocketFactory = SSLConnectionSocketFactoryBuilder.create()
                .setSslContext(sslContext)
                .build();

        // 4. HttpClient 생성 (Spring Boot 3 / HttpClient 5 환경)
        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(PoolingHttpClientConnectionManagerBuilder.create()
                        .setSSLSocketFactory(sslSocketFactory)
                        .build())
                .build();

        // 5. RestTemplate에 HttpClient 5 연결
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        factory.setConnectTimeout((int) Duration.ofSeconds(5).toMillis()); // 연결 타임아웃
        // Spring Boot 3의 경우 setReadTimeout 대신 다른 방식이나 기본 설정 사용 가능

        return new RestTemplate(factory);
    }
}
