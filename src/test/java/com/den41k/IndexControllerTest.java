package com.den41k;

import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest
public class IndexControllerTest {

    @Inject
    @Client("/")
    HttpClient client;

    @Test
    void testHomePageReturns200() {
        var response = client.toBlocking().exchange("/");
        assertEquals(HttpStatus.OK, response.status());
    }

    @Test
    void testChatsPageRedirectsForGuest() {
        var response = client.toBlocking().exchange("/chats", String.class);
        // Например, редирект на /auth
        assertEquals(HttpStatus.FOUND, response.status()); // 302
        assertTrue(response.getHeaders().contains("Location"));
    }
}