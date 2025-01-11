package com.data.projectiris;

import com.mongodb.MongoException;
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
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
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
private final EmailAlertService emailAlertService;
private Optional<ResumeToken> lastResumeToken = Optional.empty();// To store the resume token
private boolean isMongoDbConnected=true;


@Autowired
public ChangeListenerService(MongoClient mongoClient, ApiService apiService, IrisProperties irisProperties, EmailAlertService emailAlertService) {
this.mongoClient = mongoClient;
this.apiService = apiService;
this.irisProperties = irisProperties;
this.emailAlertService = emailAlertService;
}

    @PostConstruct
public void init() {
this.lastResumeToken = loadLastResumeToken(); // Load the resume token from the metadata collection
startChangeStreamListener();  // Start the change stream listener after dependencies are injected
}

    // MongoDB connectivity check
    @Scheduled(fixedRate = 1800000)
    @Async
    public void checkMongoDbConnectivity() {
        try {
            MongoDatabase database = mongoClient.getDatabase(irisProperties.getDb_name());
            database.runCommand(new Document("ping", 1));  // Ping the database
            logger.info("MongoDB connectivity check successful.");
            if (!isMongoDbConnected) { // MongoDB has been reconnected
                isMongoDbConnected = true; // Update connection state
                logger.info("Reconnecting to MongoDB...");
                if (listenerThread == null || !listenerThread.isAlive()) {
                    logger.info("Loading the Resume Token and Restarting the change stream listener...");
                    init();
                }
            }
        } catch (MongoException e) {
            logger.error("MongoDB connectivity check failed: {}", e.getMessage(), e);
            isMongoDbConnected=false;
            emailAlertService.sendEmailAlert("MongoDB connectivity issue", e.getMessage());
        }
    }



    /**
* Loads the last resume token from the MongoDB collection (change_stream_metadata)
*/
private Optional<ResumeToken> loadLastResumeToken() {
try {
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
}catch (MongoException e) {
    logger.error("Error while loading resume token from MongoDB: {}", e.getMessage(), e);
    // You may choose to return Optional.empty() or handle it differently
        logger.info("Stopping the change stream listener...");
        stopListener();
        checkMongoDbConnectivity();

} catch (Exception e) {
    logger.error("Unexpected error while loading resume token: {}", e.getMessage(), e);
    logger.info("Stopping the change stream listener");
    stopListener();
    checkMongoDbConnectivity();
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
        checkMongoDbConnectivity();
    }
});

listenerThread.start();
}

private void processChange(ChangeStreamDocument<Document> change) {
String operationType = change.getOperationType().getValue();
Document document = change.getFullDocument();
boolean apiCallSuccess = false;
try {
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
} catch (Exception e) {
    // Catch all other exceptions
    logger.error("Unexpected error while processing change: {}", e.getMessage(), e);
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
    try {
        listenerThread.interrupt();
        logger.info("Change stream listener stopped.");
    }catch (Exception e) {
        logger.error("Error stopping the listener thread: {}", e.getMessage(), e);
    }
}
}
}
