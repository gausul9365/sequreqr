package com.gausul.secureqr.service;



import com.fasterxml.jackson.databind.ObjectMapper;
import com.gausul.secureqr.model.SignedQrRecord;
import com.gausul.secureqr.model.LeafKey;
import com.gausul.secureqr.repository.SignedQrRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class SignedQrService {

    @Autowired private CryptoService cryptoService;
    @Autowired private QrGenerator qrGenerator;
    @Autowired private IssuerService issuerService;
    @Autowired private SignedQrRecordRepository signedQrRecordRepository;

    private final ObjectMapper mapper = new ObjectMapper();

    public byte[] createSignedQrBytesFromAlias(String data, String alias) throws Exception {
        LeafKey lk = issuerService.getLeafByAlias(alias);
        if (lk == null) throw new IllegalArgumentException("alias not found");

        String leafPriv = lk.getPrivateKeyEncrypted(); // decrypt if encrypted
        String leafPub = lk.getPublicKeyBase64();
        String issuerSig = lk.getIssuerSignature();

        // sign payload
        String payloadSignature = cryptoService.signEcdsa(data, leafPriv);

        Map<String, String> qrJson = new HashMap<>();
        qrJson.put("payload", data);
        qrJson.put("signature", payloadSignature);
        qrJson.put("pub", leafPub);
        qrJson.put("issuerId", lk.getIssuerId());
        qrJson.put("issuerSignature", issuerSig);

        String qrText = mapper.writeValueAsString(qrJson);
        byte[] png = qrGenerator.generateQrBytes(qrText);

        // persist audit record
        SignedQrRecord rec = new SignedQrRecord();
        rec.setLeafKeyId(lk.getId());
        rec.setPayload(data);
        rec.setSignatureBase64(payloadSignature);
        // optionally save filePath if you wrote a file; here it's inline bytes
        signedQrRecordRepository.save(rec);

        return png;
    }
}

