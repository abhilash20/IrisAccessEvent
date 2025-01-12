package com.data.projectiris;


import org.bson.BsonDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;

import java.nio.charset.StandardCharsets;

public class ResumeToken {

    public static final Logger logger = LoggerFactory.getLogger(ResumeToken.class);
    private BsonDocument tokenDocument;

    // Constructor to create ResumeToken from BsonDocument
    public ResumeToken(@NonNull BsonDocument tokenDocument) {
        this.tokenDocument = tokenDocument;
    }

    // Static method to create ResumeToken from a BsonDocument
    public static ResumeToken fromBsonDocument(@NonNull BsonDocument document) {
        return new ResumeToken(document);
    }

    // Convert ResumeToken back to BsonDocument
    public BsonDocument toBsonDocument() {
        if (tokenDocument == null) {
            logger.error("Token document is null.");
            throw new NullPointerException("Token document is null.");
        }
        return tokenDocument;
    }

    // Convert ResumeToken to byte array (for saving to MongoDB)
    public byte[] toBytes() {
        if (tokenDocument == null) {
            logger.error("Token document is null");
            throw new NullPointerException("Token document is null");
        }
        return tokenDocument.toJson().getBytes(StandardCharsets.UTF_8);
    }

    // Static method to parse the byte array and create a ResumeToken
    public static ResumeToken parse(byte[] tokenBytes) {
        if (tokenBytes == null) {
            logger.error("Input byte array is null.");
            throw new NullPointerException("Byte array cannot be null.");
        }

        try {
            String json = new String(tokenBytes, StandardCharsets.UTF_8);
            BsonDocument document = BsonDocument.parse(json);
            return new ResumeToken(document);
        } catch (Exception e) {
            logger.error("Error parsing byte array to BsonDocument. Error: {}", e.getMessage());
            throw new IllegalArgumentException("Failed to parse resume token from byte array.", e);

        }
    }
}
