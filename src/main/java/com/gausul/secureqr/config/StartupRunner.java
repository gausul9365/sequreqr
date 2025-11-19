package com.gausul.secureqr.config;

import com.gausul.secureqr.service.IssuerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class StartupRunner implements ApplicationRunner {

    @Autowired
    private IssuerService issuerService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println("🟡 StartupRunner executing...");
        var root = issuerService.initRootIssuerIfMissing("Root Issuer", "ROOT-ISSUER-1");
        System.out.println("✅ Root issuer ensured: " + root.getId() + " (" + root.getDisplayName() + ")");
    }
}
