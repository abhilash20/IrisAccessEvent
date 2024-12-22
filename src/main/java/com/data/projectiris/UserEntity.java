package com.data.projectiris;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;


@Document(collection = "users")
public class UserEntity {

    @Id
    private String id;
    private String firstName;
    private String lastName;
    private String emailAddr;
    private Date timeStamp;


    // Getters and Setters

    public UserEntity() {
    }

    public UserEntity(String id, String firstName, String lastName, String emailAddr, Date timeStamp) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.emailAddr = emailAddr;
        this.timeStamp = timeStamp;
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

    public String getEmailAddr() {
        return emailAddr;
    }

    public void setEmailAddr(String emailAddr) {
        this.emailAddr = emailAddr;
    }

    public Date getTimeStamp() { return timeStamp; }
    public void setTimeStamp(Date timeStamp) { this.timeStamp = timeStamp;}

    @Override
    public String toString() {
        return "Person{id='" + id + "',first name='" + firstName + "',last name '" + lastName + "',emailAddr='"+ emailAddr +"'time stamp'"  + timeStamp + "'}";
    }
}
