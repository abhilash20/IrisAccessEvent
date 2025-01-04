package com.data.projectiris;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "iris")
public class IrisProperties {
    private String db_uri;
    private String db_name;
    private String collection_name;

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
}
