// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.random;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.hiero.metrics.demo.crawler.TestUtils;
import org.hiero.metrics.demo.crawler.api.document.Document;

public class TestRandomDocument implements Document {

    private final URI uri;
    private final List<URI> links;

    public TestRandomDocument(URI uri, IdGenerator idGenerator, int linksMin, int linksMax) {
        this.uri = Objects.requireNonNull(uri);

        int linksCount = (int) TestUtils.randomLong(linksMin, linksMax);
        links = new ArrayList<>(linksCount);

        for (int i = 0; i < linksCount; i++) {
            URI newUri = URI.create("random://url/" + idGenerator.nextId());
            links.add(newUri);
        }
    }

    @Override
    public URI getUri() {
        return uri;
    }

    @Override
    public long sizeInBytes() {
        return TestUtils.randomLong(1000, 10000);
    }

    @Override
    public List<URI> getLinks() {
        return links;
    }
}
