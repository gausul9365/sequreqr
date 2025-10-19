package com.gausul.secureqr.service;


import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

@Service
public class QrReader {

    public String readQr(String filePath) throws Exception {
        File file = new File(filePath);
        BufferedImage bufferedImage = ImageIO.read(file);

        BinaryBitmap binaryBitmap = new BinaryBitmap(
                new HybridBinarizer(new BufferedImageLuminanceSource(bufferedImage))
        );

        Result result = new MultiFormatReader().decode(binaryBitmap);

        return result.getText();
    }
}
