package com.data.projectiris;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.changestream.ChangeStreamDocument;
import jakarta.annotation.PostConstruct;
import org.bson.BsonDocument;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.annotation.PreDestroy;

import java.util.Optional;

@Service
public class ChangeListenerService {

    private static final Logger logger = LoggerFactory.getLogger(ChangeListenerService.class);

    private final MongoClient mongoClient;
    private final ApiService apiService;
    private Thread listenerThread;
    private final IrisProperties irisProperties;
    private Optional<ResumeToken> lastResumeToken = Optional.empty();  // To store the resume token

    @Autowired
    public ChangeListenerService(MongoClient mongoClient, ApiService apiService, IrisProperties irisProperties) {
        this.mongoClient = mongoClient;
        this.apiService = apiService;
        this.irisProperties = irisProperties;
    }

    @PostConstruct
    public void init() {
        this.lastResumeToken = loadLastResumeToken(); // Load the resume token from the metadata collection
        startChangeStreamListener();  // Start the change stream listener after dependencies are injected
    }

    /**
     * Loads the last resume token from the MongoDB collection (change_stream_metadata)
     */
    private Optional<ResumeToken> loadLastResumeToken() {
        MongoDatabase database = mongoClient.getDatabase(irisProperties.getDb_name());
        MongoCollection<Document> metadataCollection = database.getCollection("change_stream_metadata");

        // Fetch the document containing the resume token
        Document metadataDoc = metadataCollection.find(new Document("_id", "resume_token")).first();
        if (metadataDoc != null) {
            // Retrieve the token as a Binary type and convert to byte[]
            org.bson.types.Binary tokenBinary = metadataDoc.get("token", org.bson.types.Binary.class);
            if (tokenBinary != null) {
                return Optional.of(ResumeToken.parse(tokenBinary.getData()));// Convert to byte[] and parse
            }
        }
        return Optional.empty();
    }

    /**
     * Saves the resume token to the MongoDB metadata collection
     */
    private void saveResumeToken(ResumeToken token) {
        MongoDatabase database = mongoClient.getDatabase(irisProperties.getDb_name());
        MongoCollection<Document> metadataCollection = database.getCollection("change_stream_metadata");

        // Convert the ResumeToken to a byte array to store in the document
        Document metadataDoc = new Document("_id", "resume_token")
                .append("token", token.toBytes()); // Store token as a byte array

        // Update or insert the resume token document
        metadataCollection.replaceOne(new Document("_id", "resume_token"), metadataDoc, new com.mongodb.client.model.ReplaceOptions().upsert(true));
    }

    private void startChangeStreamListener() {
        MongoDatabase database = mongoClient.getDatabase(irisProperties.getDb_name());
        MongoCollection<Document> collection = database.getCollection(irisProperties.getCollection_name());

        listenerThread = new Thread(() -> {
            try {
                // If there is a stored resume token, resume from that point
                if (lastResumeToken.isPresent()) {
                    BsonDocument bsonResumeToken = lastResumeToken.get().toBsonDocument();
                    collection.watch().resumeAfter(bsonResumeToken).forEach(this::processChange);
                } else {
                    collection.watch().forEach(this::processChange);
                }
            } catch (Exception e) {
                logger.error("Error in change stream listener", e);
            }
        });

        listenerThread.start();
    }

    private void processChange(ChangeStreamDocument<Document> change) {
        String operationType = change.getOperationType().getValue();
        Document document = change.getFullDocument();


        boolean apiCallSuccess = false;
        switch (operationType) {
            case "insert":
                logger.info("Insert detected: {}", document);
                AccessEvent response = apiService.postToExternalApi(document);
                apiCallSuccess = (response != null); // Check if API response is non-null
                break;
            case "update":
                logger.info("Update detected: {}", document);
                response = apiService.postToExternalApi(document);
                apiCallSuccess = (response != null); // Check if API response is non-null
                break;
            case "delete":
                logger.info("Delete detected: {}", change.getDocumentKey());
                break;
            default:
                logger.info("Other operation detected: {}", operationType);
        }

        // Save the last resume token after processing the change
        if (apiCallSuccess)
        {
            ResumeToken resumeToken = ResumeToken.fromBsonDocument(change.getResumeToken());
            saveResumeToken(resumeToken);
        }
    }

    @PreDestroy
    public void stopListener() {
        if (listenerThread != null && listenerThread.isAlive()) {
            listenerThread.interrupt();
            logger.info("Change stream listener stopped.");
        }
    }
}
