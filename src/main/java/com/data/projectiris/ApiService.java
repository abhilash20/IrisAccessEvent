package com.data.projectiris;

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

    @Autowired
    public ApiService(AmadeusProperties amadeusProperties, RestTemplate restTemplate) {
        this.amadeusProperties = amadeusProperties;
        this.restTemplate = restTemplate;
    }

    @Retryable(
            value = { Exception.class },  // Retry on any exception
            maxAttempts = 3,             // Max retries
            backoff = @Backoff(delay = 10000)  // Delay between retries (in ms)
    )
    public AccessEvent postToExternalApi(Document user) {
        String url = amadeusProperties.getEndpoint() + "/API_AccessEventLogs";
        AccessEvent request = new AccessEvent();
        request.setCardCode("00000239");
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
