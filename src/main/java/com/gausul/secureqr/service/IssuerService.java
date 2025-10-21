package com.gausul.secureqr.service;



import com.gausul.secureqr.model.Issuer;
import com.gausul.secureqr.model.LeafKey;
import com.gausul.secureqr.repository.IssuerRepository;
import com.gausul.secureqr.repository.LeafKeyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.KeyPair;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

@Service
public class IssuerService {

    private final CryptoService cryptoService;
    private final IssuerRepository issuerRepository;
    private final LeafKeyRepository leafKeyRepository;

    @Autowired
    public IssuerService(CryptoService cryptoService,
                         IssuerRepository issuerRepository,
                         LeafKeyRepository leafKeyRepository) {
        this.cryptoService = cryptoService;
        this.issuerRepository = issuerRepository;
        this.leafKeyRepository = leafKeyRepository;
    }

    // Create or load root issuer. For dev: create if not exists.
    public Issuer initRootIssuerIfMissing(String displayName, String issuerId) throws Exception {
        // try load
        return issuerRepository.findById(issuerId).orElseGet(() -> {
            try {
                KeyPair rootKp = cryptoService.generateEcdsaKeyPair();
                Issuer iss = new Issuer();
                iss.setId(issuerId);
                iss.setDisplayName(displayName);
                iss.setPublicKeyBase64(cryptoService.encodePublicKeyToBase64(rootKp.getPublic()));
                // TODO: encrypt private key with app master key or KMS before storing
                iss.setPrivateKeyEncrypted(cryptoService.encodePrivateKeyToBase64(rootKp.getPrivate()));
                return issuerRepository.save(iss);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    // Issue a leaf under the given issuerId
    public LeafKey issueLeaf(String issuerId, String alias) throws Exception {
        return null;
    }

    public Issuer getIssuer(String issuerId) { return issuerRepository.findById(issuerId).orElse(null); }

    public LeafKey getLeafByAlias(String alias) { return leafKeyRepository.findByAlias(alias).orElse(null); }

    public String getRootPublicKey() {
        // Strategy: Find the single root issuer (e.g., the one created first)
        Optional<Issuer> rootIssuer = issuerRepository.findFirstByOrderByCreatedAtAsc();

        // If an Issuer is found, return its public key, otherwise return null
        return rootIssuer.map(Issuer::getPublicKeyBase64).orElse(null);
    }
}
