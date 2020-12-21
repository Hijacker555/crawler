package com.polytech.crawler.services;

import com.polytech.crawler.entity.UrlTask;
import org.springframework.stereotype.Service;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Service
public class UrlTaskQueue {
    private final BlockingQueue<UrlTask> urlTasks = new LinkedBlockingQueue<>();

    public void add(UrlTask urlTask) {
        urlTasks.add(urlTask);
    }

    public UrlTask take() throws InterruptedException {
        return urlTasks.take();
    }
}
