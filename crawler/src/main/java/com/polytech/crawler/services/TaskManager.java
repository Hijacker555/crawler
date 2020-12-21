package com.polytech.crawler.services;

import com.polytech.crawler.entity.ResourceContext;
import com.polytech.crawler.entity.ResourceType;
import com.polytech.crawler.entity.UrlTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.net.URISyntaxException;

@Slf4j
@RequiredArgsConstructor
@Service
public class TaskManager {
    private final UrlTaskQueue urlTaskQueue;
    private final ResourceContextQueue resourceContextQueue;

    private final LinkExtractor linkExtractor;
    private final ImageProcessor imageProcessor;

    @Value("${start.url}")
    private String url;

    @PostConstruct
    public void postConstruct() throws URISyntaxException {
        urlTaskQueue.add(new UrlTask(new URI(url), ResourceType.PAGE));
        new Thread(() -> {
            while (true) {
                ResourceContext resourceContext = null;
                try {
                    resourceContext = resourceContextQueue.take();
                    if (resourceContext.getResourceType() == ResourceType.PAGE) {
                        linkExtractor.extractAndSendLinksToCrawl(resourceContext);
                    }
                    if (resourceContext.getResourceType() == ResourceType.IMAGE) {
                        imageProcessor.saveImage(resourceContext);
                    }
                } catch (Exception e) {
                    log.error("processing_resource_failed uri={}", resourceContext.getUri(), e);
                }
            }
        }).start();
    }
}
