package com.gausul.secureqr.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "issuers")
public class Issuer {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id; // e.g. issuer id (UUID or domain)

    @Column(name = "display_name")
    private String displayName;

    @Lob
    @Column(name = "public_key", columnDefinition = "text")
    private String publicKeyBase64;

    @Lob
    @Column(name = "private_key_encrypted", columnDefinition = "text")
    private String privateKeyEncrypted; // optional: encrypted private key storage (recommended)

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public Issuer() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = Instant.now();
    }

    // region Getters and Setters

    /**
     * Gets the unique identifier (ID) of the issuer.
     * @return The issuer's ID.
     */
    public String getId() {
        return id;
    }

    // Note: No setter for 'id' as it is set in the constructor and marked 'updatable = false'.

    /**
     * Gets the display name of the issuer.
     * @return The display name.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets the display name of the issuer.
     * @param displayName The new display name.
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Gets the Base64 representation of the issuer's public key.
     * @return The public key in Base64 format.
     */
    public String getPublicKeyBase64() {
        return publicKeyBase64;
    }

    /**
     * Sets the Base64 representation of the issuer's public key.
     * @param publicKeyBase64 The new public key in Base64 format.
     */
    public void setPublicKeyBase64(String publicKeyBase64) {
        this.publicKeyBase64 = publicKeyBase64;
    }

    /**
     * Gets the encrypted private key (Base64).
     * @return The encrypted private key string.
     */
    public String getPrivateKeyEncrypted() {
        return privateKeyEncrypted;
    }

    /**
     * Sets the encrypted private key (Base64).
     * @param privateKeyEncrypted The new encrypted private key string.
     */
    public void setPrivateKeyEncrypted(String privateKeyEncrypted) {
        this.privateKeyEncrypted = privateKeyEncrypted;
    }

    /**
     * Gets the timestamp when this issuer record was created.
     * @return The creation timestamp.
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setId(String issuerId) {
        this.id = id;
    }

    // Note: No setter for 'createdAt' as it is set in the constructor.

    // endregion
}