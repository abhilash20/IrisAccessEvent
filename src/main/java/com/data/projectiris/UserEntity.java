package com.data.projectiris;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
public class UserEntity {

    @Id
    private String id;
    private String firstName;
    private String lastName;
    private String emailAddr;

    // Getters and Setters

    public UserEntity() {
    }

    public UserEntity(String id, String firstName, String lastName, String emailAddr) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.emailAddr = emailAddr;
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

    @Override
    public String toString() {
        return "Person{id='" + id + "',first name='" + firstName + "',last name '" + lastName + "',emailAddr='"+ emailAddr +"'}";
    }
}
