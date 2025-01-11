package com.data.projectiris;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "iris")
public class IrisProperties {
    private String db_uri;
    private String db_name;
    private String collection_name;
    private String alert_email;
    private List<String> alert_cc_emails;

    public String getDb_uri() {
        return db_uri;
    }

    public void setDb_uri(String db_uri) {
        this.db_uri = db_uri;
    }

    public String getDb_name() {
        return db_name;
    }

    public void setDb_name(String db_name) {
        this.db_name = db_name;
    }

public String getCollection_name() {
    return collection_name;
}

public void setCollection_name(String collection_name) {
    this.collection_name = collection_name;
}

public String getAlert_email() {return alert_email;}

public void setAlert_email(String alert_email) {this.alert_email = alert_email;}

public List<String> getAlert_cc_emails() {return alert_cc_emails;}

public void setAlert_cc_emails(List<String> alert_cc_emails) {this.alert_cc_emails = alert_cc_emails;}
}
