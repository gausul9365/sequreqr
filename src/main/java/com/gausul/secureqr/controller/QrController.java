package com.gausul.secureqr.controller;


import com.gausul.secureqr.service.QrGenerator;
import com.gausul.secureqr.service.QrReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/qr")
public class QrController {

    @Autowired
    private QrGenerator qrGenerator;

    @Autowired
    private QrReader qrReader;

    @PostMapping("/generate")
    public String generateQr(@RequestParam String data) {
        try {
            String filePath = "src/main/resources/qr-imgs/MessageforTanzeela.png";
            return qrGenerator.generateQr(data, filePath);
        } catch (Exception e) {
            return "Error generating QR: " + e.getMessage();
        }
    }

    @GetMapping("/read")
    public String readQr() {
        try {
            String filePath = "src/main/resources/qr-imgs/MessageforTanzeela.png";
            String content = qrReader.readQr(filePath);
            return "Decoded QR content: " + content;
        } catch (Exception e) {
            return "Error reading QR: " + e.getMessage();
        }
    }
}
