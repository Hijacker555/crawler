package com.polytech.crawler.services;

import com.polytech.crawler.entity.ResourceContext;
import com.polytech.crawler.entity.ResourceType;
import com.polytech.crawler.entity.UrlTask;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class LinkExtractor {
    private static final String HREF = "href";
    private static final String SRC = "src";

    private final UrlTaskQueue urlTaskQueue;
    private final Set<URI> crawledUrls = new HashSet<>();

    public void extractAndSendLinksToCrawl(ResourceContext resourceContext) {
        if (resourceContext.getResourceType() == ResourceType.PAGE) {
            // for simplicity we assume UTF-8 (which is obviously not true)
            String html = new String(resourceContext.getContent(), Charset.defaultCharset());
            Document document = Jsoup.parse(html, resourceContext.getUri().toString());

            extractPageLinks(document, resourceContext.getUri().getHost());
            extractImageLinks(document);
        }
    }

    private void extractImageLinks(Document document) {
        document.getElementsByTag("img")
                .stream()
                .filter(e -> e.hasAttr(SRC) && !e.attr(SRC).isEmpty())
                .map(e1 -> mapToUri(e1, SRC))
                .filter(uri -> !crawledUrls.contains(uri))
                .peek(crawledUrls::add)
                .map(uri -> new UrlTask(uri, ResourceType.IMAGE))
                .forEach(urlTaskQueue::add);
    }

    private void extractPageLinks(Document document, String host) {
        document.getElementsByTag("a")
                .stream()
                .filter(e -> e.hasAttr(HREF) && !e.attr(HREF).isEmpty())
                .map(e1 -> mapToUri(e1, HREF))
                .filter(uri -> !crawledUrls.contains(uri))
                .peek(crawledUrls::add)
                // we won't crawl subdomains, only same domain
                .filter(uri -> uri.getHost().equals(host))
                .map(uri -> new UrlTask(uri, ResourceType.PAGE))
                .limit(2) // TODO this is only for demonstration that pages are saved (or we'll wait too much before images will be crawled)
                .forEach(urlTaskQueue::add);
    }

    @SneakyThrows
    private URI mapToUri(Element e, String attributeKey) {
        return new URI(e.absUrl(attributeKey));
    }
}
