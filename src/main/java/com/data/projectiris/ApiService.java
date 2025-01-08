package com.data.projectiris;

import jakarta.annotation.PostConstruct;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class ApiService {
    private final AmadeusProperties amadeusProperties;
    private final RestTemplate restTemplate;
    private static final Logger logger = LoggerFactory.getLogger(ApiService.class);
    private final SendEmail sendEmail;

    @Autowired
    public ApiService(AmadeusProperties amadeusProperties, RestTemplate restTemplate, SendEmail sendEmail) {
        this.amadeusProperties = amadeusProperties;
        this.restTemplate = restTemplate;
        this.sendEmail = sendEmail;
    }


    @PostConstruct// API connectivity check (runs every 30 seconds)
    @Scheduled(fixedRate = 300000)
    @Async
    public void checkApiConnectivity() {
        try {
            String ur= amadeusProperties.getEndpoint() ;
            // Set headers
            HttpHeaders header = new HttpHeaders();
            header.set("Authorization", "Basic " + amadeusProperties.getApiKey());
            header.set("Accept", "application/json");
            HttpEntity<String> ent = new HttpEntity<>(header);
            // Make a simple HTTP GET request to the health check endpoint of the API
            ResponseEntity<String> res = restTemplate.exchange(ur,HttpMethod.GET,ent, String.class);

        if (res.getStatusCode().is2xxSuccessful()) {
                logger.info("API connection successfully.");
        } else {
                logger.warn("API is not responsive. Status Code: {}", res.getStatusCode());
                sendEmail.sendEmailAlert("API connectivity issue", "API returned status: " + res.getStatusCode());
            }

        } catch (Exception e) {

                logger.error("API connectivity check failed: {}", e.getMessage(), e);
                sendEmail.sendEmailAlert("API connectivity issue", e.getMessage());

        }
    }



    public AccessEvent postToExternalApi(Document user) {
        String url = amadeusProperties.getEndpoint() + "/API_AccessEventLogs";
        int cc= user.getInteger("userId");
        String cardCode = String.valueOf(cc);
        AccessEvent request = new AccessEvent();
        request.setCardCode(cardCode);
        request.setCardholderFirstName(user.getString("firstName"));
        request.setCardholderIdNumber(null);
        request.setCardholderLastName(user.getString("lastName"));
        request.setCardholderTypeName("Employee");
        request.setDateTime(user.getDate("timestamp"));
        request.setInOutType("Any");
        request.setJournalUpdateDateTime("2022-12-05T11:26:45.187-05:00");
        request.setReaderName("Iris Reader");
        request.setReaderUID("ba1e6a82-4b30-4213-819f-65004c9f6cc4");
        request.setType("AccessGranted");

        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + amadeusProperties.getApiKey());
        headers.set("Accept", "application/json");

        // Create HttpEntity with headers
        HttpEntity<AccessEvent> entity = new HttpEntity<>(request, headers);

        try {
            // Make the API call and deserialize response to AccessEvent
            ResponseEntity<AccessEvent> response = restTemplate.exchange(
                    url,             // API endpoint
                    HttpMethod.POST,  // HTTP method
                    entity,          // HttpEntity containing headers
                    AccessEvent.class // Response type
            );

            // Check the HTTP status code for acknowledgment
            if (response.getStatusCode().is2xxSuccessful()) {
                // Log success acknowledgment
                logger.info("Successfully sent access event to API. Status: {}", response.getStatusCode());
                return response.getBody();
            } else {
                // Log error acknowledgment
                logger.error("API call failed with status code: {}", response.getStatusCode());
                return null;

            }

            // Log the response body (which could contain further acknowledgment data)
//            if (response.getBody() != null) {
//                logger.info("Response Body: {}", response.getBody());
//            } else {
//                logger.warn("Received an empty response body.");
//            }
//
//            return response.getBody();

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            // Handle specific HTTP errors
            logger.error("API call failed with HTTP error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            return null;
        } catch (RestClientException e) {
            // Handle other RestClient errors
            logger.error("RestClient error occurred while calling the API: {}", e.getMessage(), e);
            return null;
        } catch (Exception e) {
            // General exception catch
            logger.error("Error occurred while calling API: {}", e.getMessage(), e);
            return null;
        }


    }
}
