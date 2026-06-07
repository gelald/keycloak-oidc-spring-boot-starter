package com.github.gelald.keycloak.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.gelald.keycloak.config.KeycloakOidcProperties;
import com.github.gelald.keycloak.exception.KeycloakErrorDecoder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * RestClient 封装，提供 HTTP 方法调用、错误处理和 Basic Auth 作用域控制。
 * <p>
 * 认证头（Basic Auth）仅在需要认证的请求中发送，公开端点（certs, ready, live）不发送认证信息。
 *
 * @since 1.1
 */
class KeycloakHttp {

    private final RestClient restClient;
    private final KeycloakOidcProperties properties;
    private final KeycloakErrorDecoder errorDecoder;
    private final String basicAuthHeader;

    KeycloakHttp(KeycloakOidcProperties properties, ObjectMapper objectMapper,
                 RestClient.Builder restClientBuilder) {
        this.properties = properties;
        this.errorDecoder = new KeycloakErrorDecoder(objectMapper);

        this.basicAuthHeader = "Basic " + Base64.getEncoder()
                .encodeToString((properties.getClientId() + ":" + properties.getClientSecret())
                        .getBytes(StandardCharsets.UTF_8));

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(properties.getConnectTimeout())
                .build();

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(properties.getReadTimeout());

        this.restClient = restClientBuilder
                .baseUrl(properties.getDomain())
                .requestFactory(requestFactory)
                .build();
    }

    /**
     * 发送带认证的 POST 表单请求，返回解析后的响应体。
     */
    <T> T postFormWithAuth(String uri, org.springframework.util.MultiValueMap<String, String> formBody, Class<T> responseType) {
        return restClient.post()
                .uri(uri, properties.getRealm())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header(HttpHeaders.AUTHORIZATION, basicAuthHeader)
                .body(formBody)
                .retrieve()
                .onStatus(HttpStatusCode::isError, errorHandler())
                .body(responseType);
    }

    /**
     * 发送带认证的 POST 表单请求，不返回响应体。
     */
    void postFormWithAuthVoid(String uri, org.springframework.util.MultiValueMap<String, String> formBody) {
        restClient.post()
                .uri(uri, properties.getRealm())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header(HttpHeaders.AUTHORIZATION, basicAuthHeader)
                .body(formBody)
                .retrieve()
                .onStatus(HttpStatusCode::isError, errorHandler())
                .toBodilessEntity();
    }

    /**
     * 发送不带认证的 GET 请求，返回解析后的响应体。
     * 用于公开端点（certs, ready, live）。
     */
    <T> T getJson(String uri, Class<T> responseType) {
        return restClient.get()
                .uri(uri, properties.getRealm())
                .retrieve()
                .onStatus(HttpStatusCode::isError, errorHandler())
                .body(responseType);
    }

    /**
     * 统一的 HTTP 错误处理器：读取响应体并委托给 {@link KeycloakErrorDecoder} 解码。
     */
    private RestClient.ResponseSpec.ErrorHandler errorHandler() {
        return (req, resp) -> {
            String body = new String(resp.getBody().readAllBytes(), StandardCharsets.UTF_8);
            throw errorDecoder.decode(resp.getStatusCode().value(), body);
        };
    }
}
