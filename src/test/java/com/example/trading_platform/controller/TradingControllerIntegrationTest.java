package com.example.trading_platform.controller;

import com.example.tradingplatform.Application;
import com.example.tradingplatform.model.Order;
import com.example.tradingplatform.model.Side;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TradingControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testPlaceOrder() {
        Order order = new Order("1234", Side.BUY, 150.00, 100, "AAPL");
        ResponseEntity<String> response = restTemplate.postForEntity(
            createURLWithPort("/api/trading/orders"), order, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

 public void testGetMarketPrice() {
        // First, place an order
		Order order = new Order("1234", Side.BUY, 150.00, 100, "AAPL");
        restTemplate.postForEntity(createURLWithPort("/api/trading/orders"), order, String.class);

        // Now, get the market price
        ResponseEntity<String> response = restTemplate.getForEntity(
            createURLWithPort("/api/trading/market-price/AAPL"), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        // Assuming the response is a JSON object with a "price" field
        // You might need to adjust this based on your actual response format
        assertThat(response.getBody()).contains("price");
    }

    @Test
    public void testCancelOrder() {
        // First, place an order
        Order order = new Order("1234", Side.BUY, 150.00, 100, "AAPL");
        ResponseEntity<String> placeResponse = restTemplate.postForEntity(
            createURLWithPort("/api/trading/orders"), order, String.class);

        assertThat(placeResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(placeResponse.getBody()).isNotNull();

        // Extract the orderId from the response
        // This assumes your response is a JSON object with an "orderId" field
        // You might need to adjust this based on your actual response format
        String responseBody = placeResponse.getBody();
        String orderId = responseBody.split("\"orderId\":\"")[1].split("\"")[0];

        // Now, cancel the order
        ResponseEntity<String> cancelResponse = restTemplate.exchange(
            createURLWithPort("/api/trading/orders/" + orderId),
            HttpMethod.DELETE,
            null,
            String.class);

        assertThat(cancelResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private String createURLWithPort(String uri) {
        return "http://localhost:" + port + uri;
    }
}