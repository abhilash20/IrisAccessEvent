package com.data.projectiris;

import org.bson.BsonDocument;
import org.bson.BsonBinary;


public class ResumeToken {

    private BsonDocument tokenDocument;

    // Constructor to create ResumeToken from BsonDocument
    public ResumeToken(BsonDocument tokenDocument) {
        this.tokenDocument = tokenDocument;
    }

    // Static method to create ResumeToken from a BsonDocument
    public static ResumeToken fromBsonDocument(BsonDocument document) {
        return new ResumeToken(document);
    }

    // Convert ResumeToken back to BsonDocument
    public BsonDocument toBsonDocument() {
        return tokenDocument;
    }

    // Convert ResumeToken to byte array (for saving to MongoDB)
    public byte[] toBytes() {
        return tokenDocument.toJson().getBytes();
    }

    // Static method to parse the byte array and create a ResumeToken
    public static ResumeToken parse(byte[] tokenBytes) {
        BsonDocument document = BsonDocument.parse(new String(tokenBytes));
        return new ResumeToken(document);
    }
}
