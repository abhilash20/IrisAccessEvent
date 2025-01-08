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

    // Getters and Setters

    public UserEntity() {
    }


    public UserEntity(String id, String firstName, String lastName, Date timeStamp, int userId) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.timeStamp = timeStamp;
        this.userId = userId;
    }

    public String getLastName() {
        return lastName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Date getTimeStamp() { return timeStamp; }
    public void setTimeStamp(Date timeStamp) { this.timeStamp = timeStamp;}

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "Person{id='" + id + "',first name='" + firstName + "',last name '" + lastName + "',timestamp='"+  timeStamp + "'}";
    }
}
