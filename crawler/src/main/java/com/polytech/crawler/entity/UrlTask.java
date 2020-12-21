package com.polytech.crawler.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.net.URI;

@Data
@Accessors(fluent = true)
public class UrlTask {
    private final URI uri;
    private final ResourceType resourceType;
}
