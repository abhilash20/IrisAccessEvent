package com.data.projectiris;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;



@Service
public class ApiService {

    @Value("${amadeus.endpoint}")
    private String amadeusEndpoint;
    @Value("${amadeus.apiKey}")
    private String apiKey;
    private final RestTemplate restTemplate;

    public ApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public AccessEvent postToExternalApi(Document user) {
        String url = amadeusEndpoint + "/API_AccessEventLogs" ;
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
        headers.set("Authorization", "Basic "+apiKey);
        headers.set("Accept", "application/json");

        // Create HttpEntity with headers
        HttpEntity<AccessEvent> entity = new HttpEntity<>(request,headers);

        // Make the API call and deserialize response to CardHolder
        ResponseEntity<AccessEvent> response = restTemplate.exchange(
                url,             // API endpoint
                HttpMethod.POST,  // HTTP method
                entity,          // HttpEntity containing headers
                AccessEvent.class // Response type
        );
       return response.getBody();
    }


}
