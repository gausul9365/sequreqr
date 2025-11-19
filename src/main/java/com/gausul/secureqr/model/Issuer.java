package com.gausul.secureqr.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "issuers")
public class Issuer {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    @Column(name = "display_name")
    private String displayName;

    @Lob
    @Column(name = "public_key", columnDefinition = "text")
    private String publicKeyBase64;

    @Lob
    @Column(name = "private_key_encrypted", columnDefinition = "text")
    private String privateKeyEncrypted;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public Issuer() {
        this.createdAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String issuerId) {
        this.id = issuerId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPublicKeyBase64() {
        return publicKeyBase64;
    }

    public void setPublicKeyBase64(String publicKeyBase64) {
        this.publicKeyBase64 = publicKeyBase64;
    }

    public String getPrivateKeyEncrypted() {
        return privateKeyEncrypted;
    }

    public void setPrivateKeyEncrypted(String privateKeyEncrypted) {
        this.privateKeyEncrypted = privateKeyEncrypted;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
