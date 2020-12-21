package com.polytech.crawler.services;

import com.polytech.crawler.entity.ResourceContext;
import com.polytech.crawler.entity.ResourceType;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

@Slf4j
@RequiredArgsConstructor
@Service
public class ImageProcessor {

    @Value("${image.folder.path}")
    private String imageFolderPath;

    @Value("${image.min.size.bytes}")
    private long imageMinSizeBytes;

    public void saveImage(ResourceContext resourceContext) {
        if (resourceContext.getResourceType() == ResourceType.IMAGE
                && resourceContext.getContent().length >= imageMinSizeBytes) {
            File imageFile = new File(imageFolderPath, imageName(resourceContext.getUri()));
            try(OutputStream outputStream = new FileOutputStream(imageFile)) {
                outputStream.write(resourceContext.getContent());
            } catch (IOException e) {
                log.error("image_save_failed uri={}", resourceContext.getUri(), e);
            }
        }
    }

    @SneakyThrows
    public static String imageName(URI uri) {
        final String replaceSlashes = uri.getHost() + "_" + StringUtils.replace(uri.getPath(), "/", "-").substring(1);
        return FilenameUtils.getName(replaceSlashes);
    }
}
