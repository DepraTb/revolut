package com.revolut.challenge;

import java.math.BigDecimal;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revolut.challenge.domain.Account;

public class AppTest {
    private static App app;
    private ObjectMapper objectMapper;
    private HttpClient httpClient;

    @BeforeClass
    public static void startApp() {
        app = new App();
        app.start();
    }

    @AfterClass
    public static void stopApp() {
        app.stop();
    }

    @Before
    public void init() throws Exception {
        objectMapper = new ObjectMapper();
        httpClient = new HttpClient();
        httpClient.start();
    }

    @After
    public void destroy() throws Exception {
        httpClient.stop();
    }

    @Test
    public void testCreate_201() throws Exception {
        ContentResponse response = httpClient.newRequest("http://localhost:8080/account/500")
                .method(HttpMethod.POST)
                .send();

        Account account = objectMapper.readValue(response.getContent(), Account.class);

        Assert.assertEquals(HttpStatus.CREATED_201, response.getStatus());
        Assert.assertEquals(new BigDecimal(500), account.getBalance());
    }

    @Test
    public void testCreate_400() throws Exception {
        ContentResponse response = httpClient.newRequest("http://localhost:8080/account/-500")
                .method(HttpMethod.POST)
                .send();

        Assert.assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
    }

    @Test
    public void testFind_200() throws Exception {
        ContentResponse response = httpClient.newRequest("http://localhost:8080/account/1")
                .method(HttpMethod.GET)
                .send();

        Account account = objectMapper.readValue(response.getContent(), Account.class);

        Assert.assertEquals(HttpStatus.OK_200, response.getStatus());
        Assert.assertNotNull(account);
    }

    @Test
    public void testFind_404() throws Exception {
        ContentResponse response = httpClient.newRequest("http://localhost:8080/account/-1")
                .method(HttpMethod.GET)
                .send();

        Assert.assertEquals(HttpStatus.NOT_FOUND_404, response.getStatus());
    }

    @Test
    public void testDelete_204() throws Exception {
        ContentResponse response = httpClient.newRequest("http://localhost:8080/account/1")
                .method(HttpMethod.DELETE)
                .send();

        Assert.assertEquals(HttpStatus.NO_CONTENT_204, response.getStatus());
        Assert.assertEquals("", response.getContentAsString());
    }

    @Test
    public void testDelete_404() throws Exception {
        ContentResponse response = httpClient.newRequest("http://localhost:8080/account/-1")
                .method(HttpMethod.DELETE)
                .send();

        Assert.assertEquals(HttpStatus.NOT_FOUND_404, response.getStatus());
    }

    @Test
    public void testTransfer_204() throws Exception {
        ContentResponse response = httpClient.newRequest("http://localhost:8080/account/1/transfer/2/200")
                .method(HttpMethod.POST)
                .send();

        Assert.assertEquals(HttpStatus.NO_CONTENT_204, response.getStatus());
        Assert.assertEquals("", response.getContentAsString());
    }

    @Test
    public void testTransfer_404() throws Exception {
        ContentResponse response = httpClient.newRequest("http://localhost:8080/account/-1/transfer/2/200")
                .method(HttpMethod.POST)
                .send();

        Assert.assertEquals(HttpStatus.NOT_FOUND_404, response.getStatus());
    }

}
