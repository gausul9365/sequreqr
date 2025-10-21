package com.gausul.secureqr.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.gausul.secureqr.model.LeafKey;
import com.gausul.secureqr.service.CryptoService;
import com.gausul.secureqr.service.IssuerService;
import com.gausul.secureqr.service.QrReader;
import com.gausul.secureqr.service.SignedQrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/qr/signed")
public class SignedQrController {

    @Autowired
    private SignedQrService signedQrService;

    @Autowired
    private IssuerService issuerService;

    @Autowired
    private CryptoService cryptoService;

    @Autowired
    private QrReader qrReader;

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Dev: Issue a leaf keypair under alias (root signs leaf public key).
     */
    @PostMapping("/issue")
    // Inside com.gausul.secureqr.service.IssuerService.java (or similar)

    public Map<String, String> issue(@RequestParam String issuerId, @RequestParam String alias) throws Exception {

        // Calls the service method, which must now return a LeafKey object.
        LeafKey issued = issuerService.issueLeaf(issuerId, alias);

        // Constructs the Map<String, String> response using the data from the LeafKey.
        return Map.of(
                "leafId", issued.getId(),
                "alias", issued.getAlias(),
                "publicKey", issued.getPublicKeyBase64(),
                "issuerSignature", issued.getIssuerSignature()
        );
    }

    /**
     * Generate signed QR and return PNG bytes directly.
     * You can either pass alias (dev) or pass leaf keys directly.
     *
     * POST /api/qr/signed/generate
     * form-data:
     * - data
     * - alias (optional) OR leafPrivateKeyBase64 & leafPublicKeyBase64
     */
    @PostMapping("/generate")
    public ResponseEntity<byte[]> generateSignedQr(
            @RequestParam String data,
            @RequestParam(required = false) String alias,
            @RequestParam(required = false) String leafPrivateKeyBase64,
            @RequestParam(required = false) String leafPublicKeyBase64
    ) {
        try {
            byte[] png;
            if (alias != null && !alias.isBlank()) {
                png = signedQrService.createSignedQrBytesFromAlias(data, alias);
            } else {
                if (leafPrivateKeyBase64 == null || leafPublicKeyBase64 == null) {
                    return ResponseEntity.badRequest().body(("need alias or leaf keys").getBytes());
                }
                // need issuerSignature for leafPub: in dev, assume external call created it and provided issuerSignature.
                // But to keep simple: attempt to fetch issued record matching leafPub (search)
                String issuerSig = null;
                // quick search in issuerService issued map (not exposed) — instead rely on client to use alias or provide issuerSignature.
                return ResponseEntity.badRequest().body(("for direct leaf keys, use alias issued by /issue or supply issuerSignature currently").getBytes());
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentLength(png.length);
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=qr-signed.png");

            return ResponseEntity.ok().headers(headers).body(png);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(("error: " + e.getMessage()).getBytes());
        }
    }

    /**
     * Read PNG from server path, decode QR JSON, verify:
     * - Verify payload signature using leaf pub
     * - Verify issuerSignature by verifying leaf pub using root pub (chain)
     */
    @PostMapping("/read-file")
    public Map<String, Object> readAndVerifyFromFile(@RequestParam String filePath) {
        try {
            String decoded = qrReader.readQr(filePath);
            Map<String, String> json = mapper.readValue(decoded, Map.class);

            String payload = json.get("payload");
            String signature = json.get("signature");
            String leafPub = json.get("pub");
            String issuerId = json.get("issuerId");
            String issuerSignature = json.get("issuerSignature");

            if (payload == null || signature == null || leafPub == null || issuerSignature == null) {
                return Map.of("error", "missing fields", "decoded", json);
            }

            // 1) verify payload signature using leaf public key
            boolean payloadValid = cryptoService.verifyEcdsa(payload, signature, leafPub);

            // 2) verify issuer signature -> root verifies leafPub
            String rootPub = issuerService.getRootPublicKey(); // in prod, verifier must have this already
            boolean issuerValid = cryptoService.verifyEcdsa(leafPub, issuerSignature, rootPub);

            return Map.of(
                    "decoded", json,
                    "payloadValid", payloadValid,
                    "issuerValid", issuerValid,
                    "issuerTrustedPublic", rootPub
            );
        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }

    /**
     * Upload PNG and verify (same logic).
     * POST multipart/form-data with file
     */
    @PostMapping(value = "/read-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> readAndVerifyFromUpload(@RequestPart MultipartFile file) {
        try {
            // save to temp file and reuse QrReader
            java.io.File tmp = java.io.File.createTempFile("qr-upload-", ".png");
            file.transferTo(tmp);
            String decoded = qrReader.readQr(tmp.getAbsolutePath());
            tmp.delete();

            Map<String, String> json = mapper.readValue(decoded, Map.class);

            String payload = json.get("payload");
            String signature = json.get("signature");
            String leafPub = json.get("pub");
            String issuerSignature = json.get("issuerSignature");

            if (payload == null || signature == null || leafPub == null || issuerSignature == null) {
                return Map.of("error", "missing fields", "decoded", json);
            }

            boolean payloadValid = cryptoService.verifyEcdsa(payload, signature, leafPub);
            String rootPub = issuerService.getRootPublicKey();
            boolean issuerValid = cryptoService.verifyEcdsa(leafPub, issuerSignature, rootPub);

            return Map.of(
                    "decoded", json,
                    "payloadValid", payloadValid,
                    "issuerValid", issuerValid,
                    "issuerTrustedPublic", rootPub
            );
        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }
}
