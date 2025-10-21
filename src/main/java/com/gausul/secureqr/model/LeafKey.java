package com.gausul.secureqr.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "leaf_keys",
        indexes = {
                @Index(name = "idx_leaf_alias", columnList = "alias"),
                @Index(name = "idx_issuer_id", columnList = "issuer_id")
        })
public class LeafKey {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    @Column(name = "alias", nullable = true)
    private String alias; // optional human alias

    @Column(name = "issuer_id", nullable = false)
    private String issuerId;

    @Lob
    @Column(name = "public_key", columnDefinition = "text", nullable = false)
    private String publicKeyBase64;

    @Lob
    @Column(name = "private_key_encrypted", columnDefinition = "text")
    private String privateKeyEncrypted; // encrypted PKCS8 base64; production: encrypt or avoid storing

    @Lob
    @Column(name = "issuer_signature", columnDefinition = "text", nullable = false)
    private String issuerSignature; // root signature over leaf public

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public LeafKey() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = Instant.now();
    }

    // region Getters and Setters

    /**
     * Gets the unique identifier of the key.
     * @return The key's ID.
     */
    public String getId() {
        return id;
    }

    // Note: No setter for 'id' as it's typically set during object creation and marked 'updatable = false'

    /**
     * Gets the optional human-readable alias for the key.
     * @return The key's alias.
     */
    public String getAlias() {
        return alias;
    }

    /**
     * Sets the optional human-readable alias for the key.
     * @param alias The new alias.
     */
    public void setAlias(String alias) {
        this.alias = alias;
    }

    /**
     * Gets the ID of the issuer (root key) that signed this leaf key.
     * @return The issuer ID.
     */
    public String getIssuerId() {
        return issuerId;
    }

    /**
     * Sets the ID of the issuer (root key) that signed this leaf key.
     * @param issuerId The new issuer ID.
     */
    public void setIssuerId(String issuerId) {
        this.issuerId = issuerId;
    }

    /**
     * Gets the Base64 representation of the public key.
     * @return The public key in Base64 format.
     */
    public String getPublicKeyBase64() {
        return publicKeyBase64;
    }

    /**
     * Sets the Base64 representation of the public key.
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
     * Gets the issuer's signature over this leaf key's public key (Base64).
     * @return The issuer signature string.
     */
    public String getIssuerSignature() {
        return issuerSignature;
    }

    /**
     * Sets the issuer's signature over this leaf key's public key (Base64).
     * @param issuerSignature The new issuer signature string.
     */
    public void setIssuerSignature(String issuerSignature) {
        this.issuerSignature = issuerSignature;
    }

    /**
     * Gets the timestamp when this key was created.
     * @return The creation timestamp.
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    // Note: No setter for 'createdAt' as it's typically set during object creation and shouldn't be changed.

    // endregion
}