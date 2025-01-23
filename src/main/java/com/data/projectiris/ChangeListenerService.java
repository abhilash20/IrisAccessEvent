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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


@Service
public class ChangeListenerService {

    private static final Logger logger = LoggerFactory.getLogger(ChangeListenerService.class);

    private final MongoClient mongoClient;
    private final ApiService apiService;
    private final IrisProperties irisProperties;
    private final EmailAlertService emailAlertService;
    private Optional<ResumeToken> lastResumeToken = Optional.empty(); // To store the resume token
    private boolean isMongoDbConnected = true;

    // Executor service to handle the listener thread
    private ExecutorService executorService;

    private ScheduledExecutorService scheduledExecutorService;


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
        if(isMongoDbConnected) {
            executorService = Executors.newSingleThreadExecutor();  // Create an executor service with a single thread
            this.startChangeStreamListener();  // Start the change stream listener after dependencies are injected
        }
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
                // Restart the change stream listener
                restartChangeStreamListener();
            }
        } catch (MongoException e) {
            logger.error("MongoDB connectivity check failed: {}", e.getMessage(), e);
            isMongoDbConnected = false;
            emailAlertService.sendEmailAlert("MongoDB connectivity issue", e.getMessage());

            if (scheduledExecutorService == null || scheduledExecutorService.isShutdown()) {
                logger.info("No active executor found, starting new scheduledExecutorService");
                 scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
                scheduledExecutorService.scheduleAtFixedRate(() -> reconnectChangeStreamListener(scheduledExecutorService), 1, 1, TimeUnit.MINUTES);
            } else {
                logger.info("SchedulerExecutor service is already running.");
            }
//set up timeout for this operation
        }
    }


    public void reconnectChangeStreamListener(ScheduledExecutorService scheduledExecutorService) {
        try {
            MongoDatabase database = mongoClient.getDatabase(irisProperties.getDb_name());
            database.runCommand(new Document("ping", 1));  // Ping the database
            logger.info("MongoDB connectivity check from reconnect");
            checkMongoDbConnectivity();
            scheduledExecutorService.shutdown();

        } catch (MongoException e) {
            logger.error("MongoDB connectivity check failed: {}", e.getMessage(), e);
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
                    return Optional.of(ResumeToken.parse(tokenBinary.getData())); // Convert to byte[] and parse
                }
            }
        } catch (MongoException e) {
            logger.error("Error while loading resume token from MongoDB: {}", e.getMessage(), e);
            // You may choose to return Optional.empty() or handle it differently
            logger.info("Stopping the change stream listener...");
            stopListener();
            logger.info("MongoDB connectivity check from load resume token");
            checkMongoDbConnectivity();
        } catch (Exception e) {
            logger.error("Unexpected error while loading resume token: {}", e.getMessage(), e);
            logger.info("Stopping the change stream listener");
            stopListener();
            logger.info("MongoDB connectivity check from load resume token normal exception");
            checkMongoDbConnectivity();
        }
        return Optional.empty();
    }

    /**
     * Saves the resume token to the MongoDB metadata collection
     */
    private void saveResumeToken(ResumeToken token) {
        try {
            MongoDatabase database = mongoClient.getDatabase(irisProperties.getDb_name());
            MongoCollection<Document> metadataCollection = database.getCollection("change_stream_metadata");

            // Convert the ResumeToken to a byte array to store in the document
            Document metadataDoc = new Document("_id", "resume_token")
                    .append("token", token.toBytes()); // Store token as a byte array

            // Update or insert the resume token document
            metadataCollection.replaceOne(new Document("_id", "resume_token"), metadataDoc, new com.mongodb.client.model.ReplaceOptions().upsert(true));
            logger.info("Resume token saved successfully.");
        }catch (MongoException e) {
            // MongoDB-specific exceptions
            logger.error("Error while saving resume token to MongoDB: {}", e.getMessage(), e);

        } catch (Exception e) {
            // Catch any other unexpected errors
            logger.error("Unexpected error while saving resume token: {}", e.getMessage(), e);
        }
    }

    private void startChangeStreamListener() {
        if (isMongoDbConnected) {
            executorService.submit(() -> {
                try {
                    MongoDatabase database = mongoClient.getDatabase(irisProperties.getDb_name());
                    MongoCollection<Document> collection = database.getCollection(irisProperties.getCollection_name());
                    logger.info("MongoDB connectivity check before starting watch");
                    checkMongoDbConnectivity();
                    if (lastResumeToken.isPresent()) {
                        BsonDocument bsonResumeToken = lastResumeToken.get().toBsonDocument();
                        logger.info("Resuming from token: {}", bsonResumeToken);
                        collection.watch().resumeAfter(bsonResumeToken).forEach(this::processChange);
                    } else {
                        logger.info("Starting change stream from the beginning.");
                        collection.watch().forEach(this::processChange);
                    }
                } catch (MongoException e) {
                    logger.error("MongoDB Change Stream error: {}", e.getMessage(), e);
                    logger.info("MongoDB connectivity check from stream errors");
                    checkMongoDbConnectivity();
                } catch (Exception e) {
                    logger.error("Unexpected error while processing change stream: {}", e.getMessage(), e);
                }
            });
        }
    }

    private void processChange(ChangeStreamDocument<Document> change) {
        boolean apiCallSuccess = false;
        try{
        String operationType = change.getOperationType().getValue();
        Document document = change.getFullDocument();

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
        }
        catch (Exception e) {

            logger.error("Unexpected error while processing change: {}", e.getMessage(), e);
        }

        // Save the last resume token after processing the change
        if (apiCallSuccess) {
            try {
                ResumeToken resumeToken = ResumeToken.fromBsonDocument(change.getResumeToken());
                saveResumeToken(resumeToken);
            }catch (Exception e) {
                logger.error("Error while saving resume token: {}", e.getMessage(), e);
            }
        }

    }

    @PreDestroy
    public void stopListener() {
        if (executorService != null) {
            try {
                // Gracefully shutdown the executor service
                executorService.shutdown();  // Initiates an orderly shutdown
                if (!executorService.awaitTermination(5, java.util.concurrent.TimeUnit.MINUTES)) {
                    // Timeout occurred while waiting for the listener to finish
                    logger.error("Timeout while waiting for change stream listener to terminate.");
                    executorService.shutdownNow();  // Forcefully shutdown remaining tasks
                }
                logger.info("Change stream listener stopped.");
            } catch (InterruptedException e) {
                logger.error("Error stopping the listener service: {}", e.getMessage(), e);
                Thread.currentThread().interrupt(); // Restore interrupted state
            }
        }
    }

    public void restartChangeStreamListener() {
        logger.info("Change stream listener is going to restart");
        stopListener();
        this.lastResumeToken = loadLastResumeToken(); // Load the resume token from the metadata collection
        executorService = Executors.newSingleThreadExecutor();  // Create an executor service with a single thread
        startChangeStreamListener();  // Start the change stream listener after dependencies are injected
    }
}
