package com.polytech.crawler.entity;

import lombok.Builder;
import lombok.Data;

import java.net.URI;


@Data
@Builder
public class ResourceContext {
    private final URI uri;
    private final int code;
    private final byte[] content;
    private final ResourceType resourceType;
    private final String exceptionName;
}
