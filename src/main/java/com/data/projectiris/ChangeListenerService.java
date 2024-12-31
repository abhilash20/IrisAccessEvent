package com.data.projectiris;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.changestream.ChangeStreamDocument;
import org.bson.Document;
import org.springframework.stereotype.Service;

@Service
public class ChangeListenerService {

    private final MongoClient mongoClient;
    private final ApiService apiService;

    public ChangeListenerService(MongoClient mongoClient, ApiService apiService) {
        this.mongoClient = mongoClient;
        startChangeStreamListener();
        this.apiService = apiService;
    }

    private void startChangeStreamListener() {
        MongoDatabase database = mongoClient.getDatabase("tms");
        MongoCollection<Document> collection = database.getCollection("transactionLogs");


        new Thread(() -> {
            collection.watch().forEach((ChangeStreamDocument<Document> change) -> {
                String operationType = change.getOperationType().getValue();
                Document document = change.getFullDocument();

                switch (operationType) {
                    case "insert":
                        System.out.println("Insert detected: " + document);
                        apiService.postToExternalApi(document);
                        break;
                    case "update":
                        System.out.println("Update detected: " + document);
                        apiService.postToExternalApi(document);
                        break;
                    case "delete":
                        System.out.println("Delete detected: " + change.getDocumentKey());
                        break;
                    default:
                        System.out.println("Other operation: " + operationType);
                }
            });

        }).start();
    }
}