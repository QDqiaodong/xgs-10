package com.nostalgia.service;

import com.nostalgia.entity.PostImage;
import com.nostalgia.util.ImageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.UUID;

@Slf4j
@Service
public class ImageProcessingService {

    @Value("${app.upload.path:/app/uploads}")
    private String uploadPath;

    @Value("${app.image.compression.quality:0.8}")
    private float compressionQuality;

    @Value("${app.image.compression.maxWidth:1920}")
    private int maxCompressedWidth;

    @Value("${app.image.compression.maxHeight:1080}")
    private int maxCompressedHeight;

    @Value("${app.image.thumbnail.width:300}")
    private int thumbnailWidth;

    @Value("${app.image.thumbnail.height:300}")
    private int thumbnailHeight;

    public PostImage processImage(MultipartFile file, int sortOrder) {
        PostImage postImage = new PostImage();
        postImage.setSortOrder(sortOrder);
        postImage.setIsMain(sortOrder == 0);
        postImage.setProcessingStatus("PROCESSING");

        try {
            String originalFileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path uploadDir = Paths.get(uploadPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            Path originalPath = uploadDir.resolve("original_" + originalFileName);
            Files.copy(file.getInputStream(), originalPath);

            String fileExtension = getFileExtension(file.getOriginalFilename());
            postImage.setFormat(fileExtension);
            postImage.setFileSize(file.getSize());
            postImage.setOriginalUrl("/api/uploads/original_" + originalFileName);

            ImageInfo imageInfo = ImageInfo.getImageInfo(file);
            if (imageInfo != null) {
                postImage.setOriginalWidth(imageInfo.getWidth());
                postImage.setOriginalHeight(imageInfo.getHeight());
            }

            BufferedImage originalImage = ImageIO.read(originalPath.toFile());
            if (originalImage == null) {
                postImage.setUrl(postImage.getOriginalUrl());
                postImage.setWidth(postImage.getOriginalWidth());
                postImage.setHeight(postImage.getOriginalHeight());
                postImage.setProcessingStatus("COMPLETED");
                return postImage;
            }

            Path compressedPath = uploadDir.resolve("compressed_" + originalFileName);
            BufferedImage compressedImage = resizeImage(
                originalImage,
                maxCompressedWidth,
                maxCompressedHeight,
                false
            );
            long compressedSize = saveImageWithQuality(
                compressedImage,
                compressedPath.toFile(),
                fileExtension,
                compressionQuality
            );
            postImage.setCompressedUrl("/api/uploads/compressed_" + originalFileName);
            postImage.setCompressedFileSize(compressedSize);
            postImage.setWidth(compressedImage.getWidth());
            postImage.setHeight(compressedImage.getHeight());

            Path thumbnailPath = uploadDir.resolve("thumb_" + originalFileName);
            BufferedImage thumbnailImage = resizeImage(
                originalImage,
                thumbnailWidth,
                thumbnailHeight,
                true
            );
            saveImageWithQuality(
                thumbnailImage,
                thumbnailPath.toFile(),
                fileExtension,
                0.7f
            );
            postImage.setThumbnailUrl("/api/uploads/thumb_" + originalFileName);

            if (postImage.getWidth() != null && postImage.getHeight() != null && postImage.getHeight() != 0) {
                postImage.setDisplayRatio((double) postImage.getWidth() / postImage.getHeight());
            }

            postImage.setUrl(postImage.getCompressedUrl());
            postImage.setProcessingStatus("COMPLETED");

            log.info("图片处理完成: 原图={}x{}, 压缩图={}x{}, 压缩前={}KB, 压缩后={}KB",
                postImage.getOriginalWidth(), postImage.getOriginalHeight(),
                postImage.getWidth(), postImage.getHeight(),
                postImage.getFileSize() / 1024,
                postImage.getCompressedFileSize() / 1024);

        } catch (Exception e) {
            log.error("图片处理失败", e);
            postImage.setProcessingStatus("FAILED");
            if (postImage.getOriginalUrl() != null) {
                postImage.setUrl(postImage.getOriginalUrl());
            }
        }

        return postImage;
    }

    private BufferedImage resizeImage(BufferedImage originalImage, int maxWidth, int maxHeight, boolean crop) {
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        if (originalWidth <= maxWidth && originalHeight <= maxHeight && !crop) {
            return originalImage;
        }

        if (crop) {
            double scale = Math.max((double) maxWidth / originalWidth, (double) maxHeight / originalHeight);
            int scaledWidth = (int) (originalWidth * scale);
            int scaledHeight = (int) (originalHeight * scale);

            BufferedImage scaledImage = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = scaledImage.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null);
            g2d.dispose();

            int cropX = (scaledWidth - maxWidth) / 2;
            int cropY = (scaledHeight - maxHeight) / 2;
            return scaledImage.getSubimage(cropX, cropY, maxWidth, maxHeight);
        } else {
            double scale = Math.min((double) maxWidth / originalWidth, (double) maxHeight / originalHeight);
            int newWidth = (int) (originalWidth * scale);
            int newHeight = (int) (originalHeight * scale);

            BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = resizedImage.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
            g2d.dispose();

            return resizedImage;
        }
    }

    private long saveImageWithQuality(BufferedImage image, File outputFile, String format, float quality) throws IOException {
        if ("png".equalsIgnoreCase(format) || "gif".equalsIgnoreCase(format)) {
            ImageIO.write(image, format, outputFile);
            return outputFile.length();
        }

        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
        if (!writers.hasNext()) {
            ImageIO.write(image, "jpg", outputFile);
            return outputFile.length();
        }

        ImageWriter writer = writers.next();
        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(quality);

        try (ImageOutputStream ios = ImageIO.createImageOutputStream(outputFile)) {
            writer.setOutput(ios);
            writer.write(null, new IIOImage(image, null, null), param);
        } finally {
            writer.dispose();
        }

        return outputFile.length();
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "jpg";
        }
        String ext = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        if (ext.equals("jpeg")) {
            return "jpg";
        }
        return ext;
    }

    public PostImage createImageRecordFromUrl(String imageUrl, int sortOrder) {
        PostImage postImage = new PostImage();
        postImage.setUrl(imageUrl);
        postImage.setOriginalUrl(imageUrl);
        postImage.setCompressedUrl(imageUrl);
        postImage.setThumbnailUrl(imageUrl);
        postImage.setSortOrder(sortOrder);
        postImage.setIsMain(sortOrder == 0);
        postImage.setProcessingStatus("COMPLETED");
        return postImage;
    }
}
