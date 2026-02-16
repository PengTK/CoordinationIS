package com.den41k;

import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@MicronautTest
public class SmokeTest {

    @Inject
    @Client("/")
    HttpClient client;

    @Test
    void applicationStartsAndServesHomePage() {
        var resp = client.toBlocking().retrieve("/");
        assert resp.contains("COORDIS");
    }
}