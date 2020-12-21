package com.polytech.crawler.services;

import com.polytech.crawler.entity.ResourceContext;
import org.springframework.stereotype.Service;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Service
public class ResourceContextQueue {

    private final BlockingQueue<ResourceContext> queue = new LinkedBlockingQueue<>();

    public void add(ResourceContext resourceContext) {
        queue.add(resourceContext);
    }

    public ResourceContext take() throws InterruptedException {
        return queue.take();
    }
}
