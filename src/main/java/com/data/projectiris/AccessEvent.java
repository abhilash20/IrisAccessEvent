package com.data.projectiris;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;

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
    private Date dateTime;
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

    public AccessEvent(String cardCode, String cardholderFirstName, String cardholderLastName, String cardholderIdNumber, String cardholderTypeName, Date dateTime, String inOutType, String journalUpdateDateTime, String readerName, String readerUID, String type) {
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

    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(Date dateTime) {
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
