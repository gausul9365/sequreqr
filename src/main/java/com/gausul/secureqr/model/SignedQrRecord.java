package com.gausul.secureqr.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "signed_qr_records",
        indexes = {
                @Index(name = "idx_qr_alias", columnList = "leaf_key_id")
        })
public class SignedQrRecord {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    @Column(name = "leaf_key_id", nullable = true)
    private String leafKeyId; // reference to LeafKey.id

    @Lob
    @Column(name = "payload", columnDefinition = "text")
    private String payload;

    @Lob
    @Column(name = "signature", columnDefinition = "text")
    private String signatureBase64;

    @Column(name = "file_path", nullable = true)
    private String filePath; // optional if you store files on disk

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public SignedQrRecord() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = Instant.now();
    }

    // region Getters and Setters

    /**
     * Gets the unique identifier of the record.
     * @return The record's ID.
     */
    public String getId() {
        return id;
    }

    // Note: No setter for 'id' as it's typically set during object creation and marked 'updatable = false'.

    /**
     * Gets the ID of the leaf key used to sign this QR record.
     * @return The leaf key ID.
     */
    public String getLeafKeyId() {
        return leafKeyId;
    }

    /**
     * Sets the ID of the leaf key used to sign this QR record.
     * @param leafKeyId The new leaf key ID.
     */
    public void setLeafKeyId(String leafKeyId) {
        this.leafKeyId = leafKeyId;
    }

    /**
     * Gets the raw data payload contained within the QR.
     * @return The payload string.
     */
    public String getPayload() {
        return payload;
    }

    /**
     * Sets the raw data payload contained within the QR.
     * @param payload The new payload string.
     */
    public void setPayload(String payload) {
        this.payload = payload;
    }

    /**
     * Gets the Base64 representation of the digital signature.
     * @return The signature string.
     */
    public String getSignatureBase64() {
        return signatureBase64;
    }

    /**
     * Sets the Base64 representation of the digital signature.
     * @param signatureBase64 The new signature string.
     */
    public void setSignatureBase64(String signatureBase64) {
        this.signatureBase64 = signatureBase64;
    }

    /**
     * Gets the optional file path where the generated QR image is stored.
     * @return The file path.
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * Sets the optional file path where the generated QR image is stored.
     * @param filePath The new file path.
     */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Gets the timestamp when this record was created.
     * @return The creation timestamp.
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    // Note: No setter for 'createdAt' as it's typically set during object creation.

    // endregion
}