package com.gausul.secureqr.controller;


import com.gausul.secureqr.service.CryptoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.KeyPair;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/crypto")
public class CryptoController {

    @Autowired
    private CryptoService cryptoService;

    // Simple in-memory store (for dev/test). Key: alias -> pair (not persisted).
    // For production, store in secure keystore / HSM.
    private final Map<String, Map<String,String>> keyStore = new HashMap<>();

    @PostMapping("/generate-keys")
    public Map<String, String> generateKeys(@RequestParam(required = false, defaultValue = "default") String alias) {
        try {
            KeyPair kp = cryptoService.generateRsaKeyPair();
            String pubB64 = cryptoService.encodePublicKeyToBase64(kp.getPublic());
            String privB64 = cryptoService.encodePrivateKeyToBase64(kp.getPrivate());

            Map<String, String> entry = new HashMap<>();
            entry.put("publicKey", pubB64);
            entry.put("privateKey", privB64);

            // store in-memory under alias
            keyStore.put(alias, entry);

            Map<String, String> resp = new HashMap<>();
            resp.put("alias", alias);
            resp.put("publicKey", pubB64);
            resp.put("privateKey", privB64);
            resp.put("note", "Stored in-memory under alias. For production persist safely.");
            return resp;
        } catch (Exception e) {
            Map<String, String> err = new HashMap<>();
            err.put("error", e.getMessage());
            return err;
        }
    }

    @PostMapping("/sign")
    public Map<String, String> sign(
            @RequestParam String data,
            @RequestParam(required = false) String privateKeyBase64,
            @RequestParam(required = false) String alias
    ) {
        try {
            String privKeyToUse = privateKeyBase64;
            if (alias != null && keyStore.containsKey(alias)) {
                privKeyToUse = keyStore.get(alias).get("privateKey");
            }

            if (privKeyToUse == null) {
                return Map.of("error", "No private key provided or alias not found");
            }

            String signature = cryptoService.sign(data, privKeyToUse);
            return Map.of("data", data, "signature", signature);
        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }

    @PostMapping("/verify")
    public Map<String, Object> verify(
            @RequestParam String data,
            @RequestParam String signature,
            @RequestParam(required = false) String publicKeyBase64,
            @RequestParam(required = false) String alias
    ) {
        try {
            String pubKeyToUse = publicKeyBase64;
            if (alias != null && keyStore.containsKey(alias)) {
                pubKeyToUse = keyStore.get(alias).get("publicKey");
            }

            if (pubKeyToUse == null) {
                return Map.of("error", "No public key provided or alias not found");
            }

            boolean valid = cryptoService.verify(data, signature, pubKeyToUse);
            return Map.of("data", data, "signature", signature, "valid", valid);
        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }

    // dev-only: list in-memory aliases
    @GetMapping("/keys")
    public Map<String, Map<String,String>> listKeys() {
        return keyStore;
    }




// Encrypt plaintext for a recipient public key (client provides recipient public key)
// Returns JSON envelope string (ephemeralPub, iv, cipher) as 'envelope'
@PostMapping("/encrypt")
public Map<String, String> encrypt(@RequestParam String plaintext, @RequestParam String recipientPublicKeyBase64) {
    try {
        String envelope = cryptoService.encryptForRecipientUsingEcdhAes(plaintext, recipientPublicKeyBase64);
        return Map.of("envelope", envelope);
    } catch (Exception e) {
        return Map.of("error", e.getMessage());
    }
}

// Decrypt envelope using recipient private key (alias or direct)
@PostMapping("/decrypt")
public Map<String, String> decrypt(@RequestParam String envelope,
                                   @RequestParam(required = false) String privateKeyBase64,
                                   @RequestParam(required = false) String alias) {
    try {
        String priv = privateKeyBase64;
        // If alias is provided, fetch leaf private from DB or in-memory store.
        // For dev: check existing alias map (if you kept one). If you use IssuerService -> LeafKeyRepository, fetch from DB.
        if (alias != null && !alias.isBlank()) {
            // Example: fetch from IssuerService (assuming it's autowired here)
            // Map<String,String> e = issuerService.getIssued(alias); // adjust to DB-backed method
            // priv = e.get("privateKey");
            return Map.of("error", "alias-based decryption not implemented in this controller; pass privateKeyBase64 or integrate with IssuerService");
        }
        if (priv == null || priv.isBlank()) {
            return Map.of("error", "privateKeyBase64 required (or implement alias fetch)");
        }
        String plain = cryptoService.decryptWithPrivateKeyEcdhAes(envelope, priv);
        return Map.of("plaintext", plain);
    } catch (Exception e) {
        return Map.of("error", e.getMessage());
    }
}
}
