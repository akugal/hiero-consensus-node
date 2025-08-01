// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.http;

import edu.umd.cs.findbugs.annotations.Nullable;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hiero.metrics.demo.crawler.api.document.Document;
import org.hiero.metrics.demo.crawler.api.document.DocumentFetcher;
import org.hiero.metrics.demo.crawler.api.exception.DocumentFetchException;

public abstract class AbstractHttpHtmlDocumentFetcher implements DocumentFetcher {

    public static final String CONTENT_TYPE_HTML = "text/html";

    // TODO configure http client
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(2))
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .build();

    protected final Logger logger = LogManager.getLogger(getClass());

    @Override
    public final Optional<Document> fetch(URI url) throws DocumentFetchException {
        if (!url.getScheme().equalsIgnoreCase("http") && !url.getScheme().equalsIgnoreCase("https")) {
            logger.debug("Unsupported scheme. Expected http/https. scheme={}, url={}", url.getScheme(), url);
            return Optional.empty();
        }

        logger.info("Fetching html document. url={}", url);

        HttpResponse<String> response = connect(url);
        if (response == null) {
            return Optional.empty();
        }

        int statusCode = response.statusCode();
        if (!supportStatusCode(statusCode)) {
            throw new DocumentFetchException("Non successful status code: " + statusCode);
        }

        Optional<String> contentType = response.headers().firstValue("Content-Type");

        if (contentType.isPresent() && !contentType.get().contains(CONTENT_TYPE_HTML)) {
            logger.warn(
                    "Unsupported content type. expected={}, actual={}, url={}", CONTENT_TYPE_HTML, contentType, url);
            return Optional.empty();
        }

        return Optional.of(parseDocument(url, response));
    }

    // TODO use jdk http client library to connect
    @Nullable
    protected HttpResponse<String> connect(URI url) throws DocumentFetchException {
        try {
            return httpClient.send(HttpRequest.newBuilder(url).build(), HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new DocumentFetchException(e);
        }
    }

    protected abstract Document parseDocument(URI url, HttpResponse<String> response) throws DocumentFetchException;

    protected boolean supportStatusCode(int statusCode) {
        return statusCode >= 200 && statusCode < 300;
    }
}
