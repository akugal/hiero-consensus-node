// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.random; // SPDX-License-Identifier: Apache-2.0

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import org.hiero.metrics.demo.crawler.TestUtils;
import org.hiero.metrics.demo.crawler.api.document.Document;

public class TestRandomDocument implements Document {

    public final AtomicLong nextId = new AtomicLong(1);
    private final URI uri;
    private final double repeatedLinksProbability;

    public TestRandomDocument(URI uri, double repeatedLinksProbability) {
        if (repeatedLinksProbability < 0 || repeatedLinksProbability > 1) {
            throw new IllegalArgumentException("Repeated links probability must be between 0 and 1");
        }
        this.uri = Objects.requireNonNull(uri);
        this.repeatedLinksProbability = repeatedLinksProbability;
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
        int linksCount = (int) TestUtils.randomLong(10, 50);
        List<URI> links = new ArrayList<>(linksCount);

        for (int i = 0; i < linksCount; i++) {
            if (nextId.get() > 1 && Math.random() < repeatedLinksProbability) {
                // Repeat an existing link
                URI repeated = links.get((int) TestUtils.randomLong(1, nextId.get()));
                links.add(repeated);
            } else {
                // Generate a new link
                URI newUri = URI.create("random://url/" + nextId.getAndIncrement());
                links.add(newUri);
            }
        }
        return links;
    }
}
