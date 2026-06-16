package com.nostalgia.util;

import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class ImageInfo {

    private int width;
    private int height;

    public ImageInfo(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public static ImageInfo getImageInfo(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        try (InputStream is = file.getInputStream()) {
            BufferedImage image = ImageIO.read(is);
            if (image != null) {
                return new ImageInfo(image.getWidth(), image.getHeight());
            }
        } catch (IOException e) {
            // 忽略异常，返回 null
        }
        return null;
    }

    public static boolean isLandscape(int width, int height) {
        if (width <= 0 || height <= 0) return false;
        return (double) width / height >= 1.3;
    }

    public static boolean isPortrait(int width, int height) {
        if (width <= 0 || height <= 0) return false;
        return (double) width / height <= 0.77;
    }

    public static boolean isSquare(int width, int height) {
        if (width <= 0 || height <= 0) return true;
        double ratio = (double) width / height;
        return ratio > 0.77 && ratio < 1.3;
    }
}
