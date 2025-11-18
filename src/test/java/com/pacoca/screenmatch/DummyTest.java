package com.pacoca.screenmatch;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.pacoca.screenmatch.services.APIService;

public class DummyTest {
    @Test
     void getData_shouldReturnNonNullBody_forValidUrl() {
        APIService service = new APIService();

        // This hits a public test endpoint; adjust if you want
        String body = service.getData("https://httpbin.org/get");

        assertNotNull(body);

    }
}
