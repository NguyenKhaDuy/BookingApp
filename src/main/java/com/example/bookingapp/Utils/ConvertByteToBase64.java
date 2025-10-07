package com.example.bookingapp.Utils;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class ConvertByteToBase64 {
    public static String toBase64(byte[] image) {
        String base64Image = null;
        if (image != null && image.length > 0) {
            try {
                if (isAlreadyBase64(image)) {
                    base64Image = new String(image, StandardCharsets.UTF_8);
                } else {
                    base64Image = Base64.getEncoder().encodeToString(image);
                }
                // Kiểm tra kích thước base64
                if (isBase64TooLarge(base64Image)) {
                    // Nén ảnh nếu quá lớn
                    base64Image = compressAndEncodeImage(image);
                }
            } catch (Exception e) {
                base64Image = null;
            }
        }
        return base64Image;
    }

    private static boolean isAlreadyBase64(byte[] data) {
        try {
            String str = new String(data, StandardCharsets.UTF_8);
            // Kiểm tra format base64
            return str.matches("^[A-Za-z0-9+/]*={0,2}$") && str.length() % 4 == 0;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isBase64TooLarge(String base64) {
        // Giới hạn ~50KB cho base64 string (tương đương ~37KB binary)
        return base64.length() > 50000;
    }

    private static String compressAndEncodeImage(byte[] originalImageData) {
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(originalImageData);
            BufferedImage originalImage = ImageIO.read(bis);

            if (originalImage == null) {
                return null;
            }

            // Resize nếu quá lớn
            BufferedImage resizedImage = resizeImage(originalImage, 800, 600);

            // Nén với quality 70%
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
            ImageOutputStream ios = ImageIO.createImageOutputStream(baos);
            writer.setOutput(ios);

            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(0.7f);

            writer.write(null, new IIOImage(resizedImage, null, null), param);
            writer.dispose();
            ios.close();

            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            return null;
        }
    }

    private static BufferedImage resizeImage(BufferedImage original, int maxWidth, int maxHeight) {
        int originalWidth = original.getWidth();
        int originalHeight = original.getHeight();

        // Tính tỷ lệ scale
        double scaleWidth = (double) maxWidth / originalWidth;
        double scaleHeight = (double) maxHeight / originalHeight;
        double scale = Math.min(scaleWidth, scaleHeight);

        if (scale >= 1.0) {
            return original; // Không cần resize
        }

        int newWidth = (int) (originalWidth * scale);
        int newHeight = (int) (originalHeight * scale);

        BufferedImage resized = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resized.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(original, 0, 0, newWidth, newHeight, null);
        g2d.dispose();

        return resized;
    }
}
