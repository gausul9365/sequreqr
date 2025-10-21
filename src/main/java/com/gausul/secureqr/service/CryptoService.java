package com.gausul.secureqr.service;



import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.*;
import java.util.Base64;



import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.interfaces.ECPublicKey;
import java.util.Base64;
import java.security.spec.X509EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECGenParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Service
public class CryptoService {

    // --- ECDSA (P-256) keygen ---
    public KeyPair generateEcdsaKeyPair() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
        kpg.initialize(new ECGenParameterSpec("secp256r1")); // P-256
        return kpg.generateKeyPair();
    }

    // --- Generic ECDSA sign / verify (SHA256withECDSA) ---
    public String signEcdsa(String data, String privateKeyBase64) throws Exception {
        PrivateKey privateKey = loadPrivateKeyFromBase64(privateKeyBase64, "EC");
        Signature signature = Signature.getInstance("SHA256withECDSA");
        signature.initSign(privateKey);
        signature.update(data.getBytes(StandardCharsets.UTF_8));
        byte[] sigBytes = signature.sign();
        return Base64.getEncoder().encodeToString(sigBytes);
    }

    public boolean verifyEcdsa(String data, String signatureBase64, String publicKeyBase64) throws Exception {
        PublicKey publicKey = loadPublicKeyFromBase64(publicKeyBase64, "EC");
        Signature signature = Signature.getInstance("SHA256withECDSA");
        signature.initVerify(publicKey);
        signature.update(data.getBytes(StandardCharsets.UTF_8));
        byte[] sigBytes = Base64.getDecoder().decode(signatureBase64);
        return signature.verify(sigBytes);
    }

    // --- Key encode helpers (X.509 / PKCS#8) ---
    public String encodePublicKeyToBase64(PublicKey publicKey) {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded()); // X.509
    }

    public String encodePrivateKeyToBase64(PrivateKey privateKey) {
        return Base64.getEncoder().encodeToString(privateKey.getEncoded()); // PKCS#8
    }

    // --- Load keys from base64 for both "EC" and "RSA" (flexible) ---
    private PrivateKey loadPrivateKeyFromBase64(String base64Pkcs8, String algorithm) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(base64Pkcs8);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance(algorithm);
        return kf.generatePrivate(spec);
    }

    private PublicKey loadPublicKeyFromBase64(String base64X509, String algorithm) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(base64X509);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance(algorithm);
        return kf.generatePublic(spec);
    }

    // Convenience wrappers for RSA (if you still want RSA for other uses)
    public KeyPair generateRsaKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        return kpg.generateKeyPair();
    }

    public String signRsa(String data, String privateKeyBase64) throws Exception {
        PrivateKey privateKey = loadPrivateKeyFromBase64(privateKeyBase64, "RSA");
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(data.getBytes(StandardCharsets.UTF_8));
        byte[] sigBytes = signature.sign();
        return Base64.getEncoder().encodeToString(sigBytes);
    }

    public boolean verifyRsa(String data, String signatureBase64, String publicKeyBase64) throws Exception {
        PublicKey publicKey = loadPublicKeyFromBase64(publicKeyBase64, "RSA");
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(publicKey);
        signature.update(data.getBytes(StandardCharsets.UTF_8));
        byte[] sigBytes = Base64.getDecoder().decode(signatureBase64);
        return signature.verify(sigBytes);
    }









    // --------- Hybrid encryption helpers using ECDH (P-256) + AES-GCM ----------
    // Encrypt plaintext (UTF-8) to base64 ciphertext using recipient's public key (X.509 Base64).
    // Returns JSON string containing iv + ciphertext (both base64). We use 12-byte IV for GCM.
    public String encryptForRecipientUsingEcdhAes(String plaintext, String recipientPublicKeyBase64) throws Exception {
        // load recipient public key
        byte[] pubBytes = Base64.getDecoder().decode(recipientPublicKeyBase64);
        KeyFactory kf = KeyFactory.getInstance("EC");
        X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(pubBytes);
        PublicKey recipientPublic = kf.generatePublic(pubSpec);

        // generate ephemeral key pair for sender side (ECDH)
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
        kpg.initialize(new ECGenParameterSpec("secp256r1"));
        KeyPair ephemeral = kpg.generateKeyPair();

        // derive shared secret: ephemeralPrivate + recipientPublic
        KeyAgreement ka = KeyAgreement.getInstance("ECDH");
        ka.init(ephemeral.getPrivate());
        ka.doPhase(recipientPublic, true);
        byte[] sharedSecret = ka.generateSecret();

        // derive AES-256 key from sharedSecret using SHA-256
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        byte[] aesKeyBytes = sha256.digest(sharedSecret);

        SecretKeySpec aesKey = new SecretKeySpec(aesKeyBytes, 0, 32, "AES");

        // AES-GCM encrypt
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        byte[] iv = new byte[12];
        SecureRandom rnd = new SecureRandom();
        rnd.nextBytes(iv);
        GCMParameterSpec gcm = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.ENCRYPT_MODE, aesKey, gcm);
        byte[] cipherBytes = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

        // We must send ephemeral public key so recipient can derive the same shared secret.
        String ephemeralPubB64 = Base64.getEncoder().encodeToString(ephemeral.getPublic().getEncoded());
        String ivB64 = Base64.getEncoder().encodeToString(iv);
        String cipherB64 = Base64.getEncoder().encodeToString(cipherBytes);

        // JSON-like string (you can use Jackson on frontend). Simple compact JSON:
        String out = String.format("{\"ephemeralPub\":\"%s\",\"iv\":\"%s\",\"cipher\":\"%s\"}", ephemeralPubB64, ivB64, cipherB64);
        return out;
    }

    // Decrypt the JSON produced by encryptForRecipientUsingEcdhAes.
    // privateKeyBase64 is recipient's private key (PKCS#8 base64).
    public String decryptWithPrivateKeyEcdhAes(String envelopeJson, String recipientPrivateKeyBase64) throws Exception {
        // quick parse (no heavy JSON lib). envelopeJson expected as {"ephemeralPub":"...","iv":"...","cipher":"..."}
        // For robustness, use Jackson in real code. Here simple parse:
        java.util.Map<String,String> m = new java.util.HashMap<>();
        String trimmed = envelopeJson.trim();
        trimmed = trimmed.substring(1, trimmed.length()-1); // remove { }
        for (String part : trimmed.split(",")) {
            int colon = part.indexOf(':');
            if (colon < 0) continue;
            String key = part.substring(0, colon).replaceAll("[\"\\s]",""); // remove quotes/spaces
            String val = part.substring(colon+1).trim();
            val = val.replaceAll("^\"|\"$",""); // remove surrounding quotes
            m.put(key, val);
        }
        String ephemeralPubB64 = m.get("ephemeralPub");
        String ivB64 = m.get("iv");
        String cipherB64 = m.get("cipher");
        if (ephemeralPubB64 == null || ivB64 == null || cipherB64 == null) throw new IllegalArgumentException("invalid envelope");

        // load ephemeral public key
        byte[] ephPubBytes = Base64.getDecoder().decode(ephemeralPubB64);
        KeyFactory kf = KeyFactory.getInstance("EC");
        X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(ephPubBytes);
        PublicKey ephemeralPublic = kf.generatePublic(pubSpec);

        // load recipient private key
        byte[] privBytes = Base64.getDecoder().decode(recipientPrivateKeyBase64);
        PKCS8EncodedKeySpec privSpec = new PKCS8EncodedKeySpec(privBytes);
        PrivateKey recipientPrivate = kf.generatePrivate(privSpec);

        // derive shared secret: recipientPrivate + ephemeralPublic
        KeyAgreement ka = KeyAgreement.getInstance("ECDH");
        ka.init(recipientPrivate);
        ka.doPhase(ephemeralPublic, true);
        byte[] sharedSecret = ka.generateSecret();

        // derive AES key
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        byte[] aesKeyBytes = sha256.digest(sharedSecret);
        SecretKeySpec aesKey = new SecretKeySpec(aesKeyBytes, 0, 32, "AES");

        // decrypt
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        byte[] iv = Base64.getDecoder().decode(ivB64);
        GCMParameterSpec gcm = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.DECRYPT_MODE, aesKey, gcm);
        byte[] cipherBytes = Base64.getDecoder().decode(cipherB64);
        byte[] plainBytes = cipher.doFinal(cipherBytes);

        return new String(plainBytes, StandardCharsets.UTF_8);
    }


    public boolean verify(String data, String signatureBase64, String publicKeyBase64) throws Exception {

        // 1. Decode the Base64 Public Key
        PublicKey publicKey = decodePublicKey(publicKeyBase64); // You'll need to implement this helper

        // 2. Decode the Base64 Signature
        byte[] signatureBytes = Base64.getDecoder().decode(signatureBase64);

        // 3. Initialize the Signature object (e.g., using SHA256withRSA or the appropriate algorithm)
        Signature signature = Signature.getInstance("SHA256withRSA"); // Adjust algorithm as needed
        signature.initVerify(publicKey);

        // 4. Update the Signature object with the original data bytes
        signature.update(data.getBytes("UTF-8")); // Ensure character encoding consistency

        // 5. Perform the verification
        return signature.verify(signatureBytes);
    }

    // You will also need a helper method to convert Base64 string to a PublicKey object.
    private PublicKey decodePublicKey(String publicKeyBase64) throws Exception {
        // Implementation uses Java's KeyFactory and X509EncodedKeySpec
        // ... (this logic is usually handled elsewhere or in a helper class)
        throw new UnsupportedOperationException("PublicKey decoding logic needs to be implemented here.");
    }

    public String sign(String data, String privateKeyBase64) throws Exception {

        // 1. Decode the Base64 Private Key
        PrivateKey privateKey = decodePrivateKey(privateKeyBase64); // You'll need to implement this helper

        // 2. Initialize the Signature object (e.g., using SHA256withRSA)
        Signature signature = Signature.getInstance("SHA256withRSA"); // Adjust algorithm as needed
        signature.initSign(privateKey);

        // 3. Update the Signature object with the original data bytes
        signature.update(data.getBytes("UTF-8")); // Ensure consistent character encoding

        // 4. Generate the signature
        byte[] signatureBytes = signature.sign();

        // 5. Encode the signature to Base64 for safe transport
        return Base64.getEncoder().encodeToString(signatureBytes);
    }

    // You will need a helper method to convert a Base64 string into a PrivateKey object.
    private PrivateKey decodePrivateKey(String privateKeyBase64) throws Exception {
        // Implementation uses Java's KeyFactory and PKCS8EncodedKeySpec
        // ... (this logic must be implemented correctly to handle key formats)
        throw new UnsupportedOperationException("PrivateKey decoding logic needs to be implemented here.");
    }
}

//This uses ephemeral sender keypair for forward secrecy.
//
//Recipient must have the private key to decrypt. In your flow, leaf keys are stored encrypted in DB — decrypt via KMS before using here (dev code may use raw storage).
//
//Envelope JSON is compact. For production, use Jackson objects.

