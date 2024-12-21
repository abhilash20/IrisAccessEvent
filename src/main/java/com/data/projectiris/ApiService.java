package com.data.projectiris;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.bson.Document;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ApiService {

    private final RestTemplate restTemplate;

    public ApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

//    public String getExternalApiData() {
//        String url = "{{baseUrl}}/odata/API_Cardholders";
//        return restTemplate.getForObject(url, String.class); // GET request
//    }


    public AccessEvent postToExternalApi(Document user) {
        String url = "http://localhost:10695/odata/API_AccessEventLogs";
        AccessEvent request = new AccessEvent();
        request.setCardCode("00000239");
        request.setCardholderFirstName(user.getString("firstName"));
        request.setCardholderIdNumber(null);
        request.setCardholderLastName(user.getString("lastName"));
        request.setCardholderTypeName("Employee");
        request.setDateTime("2022-12-05T11:26:44-05:00");
        request.setInOutType("Any");
        request.setJournalUpdateDateTime("2022-12-05T11:26:45.187-05:00");
        request.setReaderName("Network_TCP4_Controller1_Reader1");
        request.setReaderUID("ba1e6a82-4b30-4213-819f-65004c9f6cc4");
        request.setType("AccessGranted");


        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic YWRtaW46MDAwMDAwMDAtMDAwMC0wMDAwLTAwMDAtMDAwMDAwMDAwMDAx");
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

class AccessEvent {

    @JsonProperty("cardCode")
    private String cardCode;
    @JsonProperty("cardholderFirstName")
    private String cardholderFirstName;
    @JsonProperty("cardholderLastName")
    private String cardholderLastName;
    @JsonProperty("cardholderIdNumber")
    private String cardholderIdNumber;
    @JsonProperty("cardholderTypeName")
    private String cardholderTypeName;
    @JsonProperty("dateTime")
    private String dateTime;
    @JsonProperty("inOutType")
    private String inOutType;
    @JsonProperty("journalUpdateDateTime")
    private String journalUpdateDateTime;
    @JsonProperty("readerName")
    private String readerName;
    @JsonProperty("readerUID")
    private String readerUID;
    @JsonProperty("type")
    private String type;

    public AccessEvent() {
    }

    public AccessEvent(String cardCode, String cardholderFirstName, String cardholderLastName, String cardholderIdNumber, String cardholderTypeName, String dateTime, String inOutType, String journalUpdateDateTime, String readerName, String readerUID, String type) {
        this.cardCode = cardCode;
        this.cardholderFirstName = cardholderFirstName;
        this.cardholderLastName = cardholderLastName;
        this.cardholderIdNumber = cardholderIdNumber;
        this.cardholderTypeName = cardholderTypeName;
        this.dateTime = dateTime;
        this.inOutType = inOutType;
        this.journalUpdateDateTime = journalUpdateDateTime;
        this.readerName = readerName;
        this.readerUID = readerUID;
        this.type = type;
    }

    public String getCardCode() {
        return cardCode;
    }

    public void setCardCode(String cardCode) {
        this.cardCode = cardCode;
    }

    public String getCardholderFirstName() {
        return cardholderFirstName;
    }

    public void setCardholderFirstName(String cardholderFirstName) {
        this.cardholderFirstName = cardholderFirstName;
    }

    public String getCardholderLastName() {
        return cardholderLastName;
    }

    public void setCardholderLastName(String cardholderLastName) {
        this.cardholderLastName = cardholderLastName;
    }

    public String getCardholderIdNumber() {
        return cardholderIdNumber;
    }

    public void setCardholderIdNumber(String cardholderIdNumber) {
        this.cardholderIdNumber = cardholderIdNumber;
    }

    public String getCardholderTypeName() {
        return cardholderTypeName;
    }

    public void setCardholderTypeName(String cardholderTypeName) {
        this.cardholderTypeName = cardholderTypeName;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getInOutType() {
        return inOutType;
    }

    public void setInOutType(String inOutType) {
        this.inOutType = inOutType;
    }

    public String getJournalUpdateDateTime() {
        return journalUpdateDateTime;
    }

    public void setJournalUpdateDateTime(String journalUpdateDateTime) {
        this.journalUpdateDateTime = journalUpdateDateTime;
    }

    public String getReaderName() {
        return readerName;
    }

    public void setReaderName(String readerName) {
        this.readerName = readerName;
    }

    public String getReaderUID() {
        return readerUID;
    }

    public void setReaderUID(String readerUID) {
        this.readerUID = readerUID;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
