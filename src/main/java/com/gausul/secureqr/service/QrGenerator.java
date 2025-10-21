package com.gausul.secureqr.service;


import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Service
public class QrGenerator {

    public String generateQr(String data, String filePath) throws Exception {
        byte[] png = generateQrBytes(data);
        Path path = FileSystems.getDefault().getPath(filePath);
        java.nio.file.Files.write(path, png);
        return "QR generated successfully at: " + path.toAbsolutePath();
    }

    // NEW: returns PNG bytes (use this for ResponseEntity)
    public byte[] generateQrBytes(String data) throws Exception {
        int width = 400;
        int height = 400;

        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.MARGIN, 1);

        BitMatrix matrix = new MultiFormatWriter().encode(
                data,
                BarcodeFormat.QR_CODE,
                width,
                height,
                hints
        );

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            MatrixToImageWriter.writeToStream(matrix, "PNG", baos);
            return baos.toByteArray();
        }
    }
}
