package com.example.eatable.infrastructure.restful;

import com.example.eatable.infrastructure.restful.model.InvoiceRequest;
import com.example.eatable.infrastructure.restful.model.InvoiceResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.Rule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testcontainers.containers.MockServerContainer;
import org.testcontainers.utility.DockerImageName;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@SpringBootTest
class RestClientTest {

    public static final DockerImageName MOCKSERVER_IMAGE = DockerImageName.parse("mockserver/mockserver");

    @Rule
    private static final MockServerContainer mockServer = new MockServerContainer(MOCKSERVER_IMAGE);
    private static MockServerClient mockServerClient;

    @Autowired
    private RestClient restClient;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    static void setUp() {
        mockServer.start();
        mockServerClient = new MockServerClient(mockServer.getHost(), mockServer.getServerPort());
    }

    @AfterAll
    static void tearDown() {
        mockServerClient.close();
        mockServer.stop();
    }

    @SneakyThrows
    @Test
    void should_get_200_respond_when_call_post_request_to_available_server() {
        InvoiceRequest request = InvoiceRequest.builder().amount("2000").contactId(1L).build();
        InvoiceResponse expect = InvoiceResponse.builder().nid(1L).build();
        mockServerClient
                .when(request()
                        .withMethod("post")
                        .withPath("/invoice")
                        .withBody(objectMapper.writeValueAsString(request)))
                .respond(response()
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(objectMapper.writeValueAsString(expect)));

        ResponseEntity<InvoiceResponse> response = restClient.post(mockServer.getEndpoint() + "/invoice", request, InvoiceResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expect, response.getBody());

    }
}
