# SecureQR
### Cryptographically Signed & Verified QR Code Platform

SecureQR is a full-stack cryptographic QR system that generates tamper-proof, verifiable, and optionally encrypted QR codes using a root–leaf trust hierarchy inspired by real-world certificate authorities.

It allows anyone to:

Generate signed QR codes

Verify QR authenticity

Sign / Verify plain text

Encrypt / Decrypt messages

Establish trust using cryptographic chains



## Tech Stack

### Frontend

React (Vite)

Axios

HTML5 / CSS

### Backend

Java 21

Spring Boot 3.3.5

Spring Data JPA

MySQL (DigitalOcean Managed)

Cryptography

ECDSA (for signing)

ECDH + AES-GCM (for encryption)

RSA (optional support)

QR Engine

ZXing (QR generation & reading)

Infrastructure

Docker

GitHub Actions (CI/CD)

DigitalOcean Droplet

Nginx Reverse Proxy

🧠 Problem Statement

Standard QR codes are:

Easy to copy

Easy to modify

Impossible to verify

SecureQR fixes this by embedding cryptographic proof directly inside QR codes.

Every QR generated is:
✅ Signed
✅ Verifiable
✅ Trusted by a Root Issuer
✅ Optionally Encrypted


## High-Level Architecture


User (Browser / Scanner App)

        |
        | 
       HTTPS
        ▼
    React Frontend
        |
        | 
    REST API
        ▼
    Spring Boot Backend
        |
        | 
    JPA / Hibernate
        ▼
    DigitalOcean MySQL


### Core Design: Root → Leaf → Payload Trust Chain
Your system works like a mini Certificate Authority:

Root Issuer
    ->
Leaf Key (per user / alias)
    ->
Signed Payload inside QR

### QR Code Structure
Each QR stores this cryptographic JSON:

{   
  "payload": "Your message here",    
  "signature": "Base64 ECDSA Signature",            
  "issuerSignature": "Signature from Root on leaf key",  
  "pub": "Leaf public key Base64",
  "issuerId": "ROOT-ISSUER-1"   
}

### Example Use Case – Two Users Secure Messaging
### User 1:

Gets public key of User 2

Encrypts message using Encrypt tool

Generates signed QR with encrypted content

### User 2:

Scans QR

Verifies authenticity

Decrypts using their private key

Result:
✅ Trusted
✅ Confidential
✅ Unforgeable