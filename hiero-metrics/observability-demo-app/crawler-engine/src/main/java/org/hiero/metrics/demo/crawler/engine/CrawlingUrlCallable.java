package org.hiero.metrics.demo.crawler.engine;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hiero.metrics.api.core.MetricRegistry;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

public class CrawlingUrlCallable implements Callable<CrawlResult> {

    private static final Logger logger = LogManager.getLogger("Crawler");

    private final URI url;

    public CrawlingUrlCallable(URI url, MetricRegistry registry) {
        this.url = url;
    }

    public URI getUrl() {
        return url;
    }

    @Override
    public CrawlResult call() {
        logger.debug("Crawling URL: {}", url);

        Connection.Response response;
        try {
            response = Jsoup.connect(url.toString())
                    .timeout(1000) //TODO from config
                    .userAgent("Mozilla/5.0 (compatible; WebCrawler/1.0)") // TODO from config
                    .execute();
        } catch (Exception e) {
            logger.error("Failed to connect to URL: {}", url, e);
            throw new RuntimeException(e);
        }

        int statusCode = response.statusCode();
        if (statusCode < 200 || statusCode >= 300) {
            logger.warn("Received non-successful status code {} for URL: {}", statusCode, url);
            throw new RuntimeException("Non successful status code: " + statusCode);
        }

        try {
            return parse(response);
        } catch (Exception e) {
            logger.error("Error during parsing document from url={}", url, e);
            throw new RuntimeException("Error during parsing document from url=" + url, e);
        }
    }

    private CrawlResult parse(Connection.Response response) throws IOException {
        final Document document = response.parse();

        final Set<URI> links = new HashSet<>();
        final Elements linkElements = document.select("a[href]");

        for (Element link : linkElements) {
            String href = link.attr("abs:href"); // Get absolute URL
            if (!href.isEmpty() && (href.startsWith("http") || href.startsWith("https"))) {
                try {
                    URI linkUrl = URI.create(href);
                    links.add(linkUrl);
                } catch (Exception e) {
                    logger.warn("Invalid URL found in document - ignoring: {}", href);
                }
            }
        }

        return new CrawlResult(url, links);
    }
}