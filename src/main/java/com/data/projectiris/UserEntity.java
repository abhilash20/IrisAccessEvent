package com.data.projectiris;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;

@Document(collection = "transactionLogs")
public class UserEntity {

    @Id
    private String id;
    private String firstName;
    private String lastName;
    private Date timeStamp;
    private int userId;
    private String deviceName;
    private String deviceSn;


    public UserEntity() {
    }


    public UserEntity(String id, String firstName, String lastName, Date timeStamp, int userId, String deviceName, String deviceSn) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.timeStamp = timeStamp;
        this.userId = userId;
        this.deviceName = deviceName;
        this.deviceSn = deviceSn;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceSn() {
        return deviceSn;
    }

    public void setDeviceSn(String deviceSn) {
        this.deviceSn = deviceSn;
    }

    @Override
    public String toString() {
        return "UserEntity{id='" + id + "',first name='" + firstName + "',last name '" + lastName + "',timestamp='"+  timeStamp + "',userId='" + userId + "',device name='" + deviceName + "',Device Sn='" + deviceSn + "'}";
    }
}
