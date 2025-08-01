// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.http;

import java.net.URI;
import java.net.http.HttpResponse;
import org.hiero.metrics.demo.crawler.api.document.Document;
import org.hiero.metrics.demo.crawler.api.exception.DocumentFetchException;
import org.jsoup.Jsoup;

public class HtmlDocumentFetcher extends AbstractHttpHtmlDocumentFetcher {

    @Override
    protected Document parseDocument(URI url, HttpResponse<String> response) throws DocumentFetchException {
        try {
            return new JsoupDocument(url, Jsoup.parse(response.body()));
        } catch (Exception e) {
            throw new DocumentFetchException("Unable to parse html document", e);
        }
    }
}
