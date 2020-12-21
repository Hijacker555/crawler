package com.polytech.crawler.services;

import com.polytech.crawler.entity.ResourceContext;
import com.polytech.crawler.entity.UrlTask;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Service
public class HttpClientService {
    @Value("${crawler.threads}")
    private int threadsCount;


    private final UrlTaskQueue urlTaskQueue;
    private final ResourceContextQueue resourceContextsQueue;

    private final HttpClient httpClient = HttpClientBuilder.create().build();

    @PostConstruct
    public void postConstruct() {
        ExecutorService ces = Executors.newFixedThreadPool(threadsCount);
        for (int i = 0; i < threadsCount; i++) {
            ces.submit((Runnable) () -> {
                while (true) {
                    UrlTask urlTask = null;
                    try {
                        urlTask = urlTaskQueue.take();
                        Thread.sleep(1000); // TODO this is needed not to receive 429 because we don't follow crawl delays
                        crawl(urlTask);
                    } catch (Exception e) {
                        log.error("crawling_failed url={}", urlTask, e);
                    }
                }
            });
        }
    }

    @SneakyThrows
    public void crawl(UrlTask urlTask) {
        HttpGet request = new HttpGet(urlTask.uri());
        log.info("crawl URI={}", urlTask.uri());
        CompletableFuture.supplyAsync(() -> execute(request))
                .whenComplete((response, t) -> {
                    ResourceContext.ResourceContextBuilder resourceContextBuilder = ResourceContext.builder();
                    try {
                        resourceContextBuilder
                                .uri(urlTask.uri())
                                .resourceType(urlTask.resourceType())
                                .content(EntityUtils.toByteArray(response.getEntity()))
                                .code(response.getStatusLine().getStatusCode());
                    } catch (IOException e) {
                        resourceContextBuilder.exceptionName(e.getClass().getName());
                        request.releaseConnection();
                    }
                    resourceContextsQueue.add(resourceContextBuilder.build());
                }).get(3, TimeUnit.SECONDS);
    }

    private HttpResponse execute(HttpGet request) {
        try {
            return httpClient.execute(request);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
