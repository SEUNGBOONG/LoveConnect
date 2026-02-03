package com.example.demo.config.toss;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.io.InputStream;
import java.security.KeyStore;

@Configuration
@Profile("toss")
public class TossHttpConfig {

    @Value("${toss.cert.path}")
    private Resource certResource;

    @Value("${toss.cert.password}")
    private String certPassword;

    @Bean
    public RestTemplate tossRestTemplate() throws Exception {
        // 1. p12 인증서 로드
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (InputStream is = certResource.getInputStream()) {
            keyStore.load(is, certPassword.toCharArray());
        }

        // 2. mTLS 전용 SSL 컨텍스트 설정 (HttpClient 5 패키지 사용)
        SSLContext sslContext = SSLContextBuilder.create()
                .loadKeyMaterial(keyStore, certPassword.toCharArray())
                .build();

        // 3. HttpClient 5 스타일로 설정
        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(PoolingHttpClientConnectionManagerBuilder.create()
                        .setSSLSocketFactory(SSLConnectionSocketFactoryBuilder.create()
                                .setSslContext(sslContext)
                                .build())
                        .build())
                .build();

        // 4. Factory에 HttpClient 주입
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);

        return new RestTemplate(factory);
    }
}
