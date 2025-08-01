// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.file;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import org.hiero.metrics.demo.crawler.api.document.Document;
import org.hiero.metrics.demo.crawler.api.document.DocumentProcessor;
import org.hiero.metrics.demo.crawler.api.util.AbstractNamed;
import org.hiero.metrics.demo.crawler.api.util.FactoryTypedKey;
import org.hiero.metrics.demo.crawler.api.util.TypedMap;

public class PathCounterDocumentProcessor extends AbstractNamed implements DocumentProcessor {

    private static final FactoryTypedKey<AtomicInteger> FILE_COUNTER_KEY =
            new FactoryTypedKey<>("file-counter", AtomicInteger.class, AtomicInteger::new);

    private static final FactoryTypedKey<AtomicInteger> DIR_COUNTER_KEY =
            new FactoryTypedKey<>("dir-counter", AtomicInteger.class, AtomicInteger::new);

    public PathCounterDocumentProcessor() {
        super("host-counter");
    }

    @Override
    public void process(Document document, TypedMap context) {
        AtomicInteger fileCounter = context.computeIfAbsent(FILE_COUNTER_KEY);
        AtomicInteger dirCounter = context.computeIfAbsent(DIR_COUNTER_KEY);

        for (URI reference : document.getLinks()) {
            Path path = Path.of(reference);
            if (Files.isDirectory(path)) {
                dirCounter.incrementAndGet();
            } else if (Files.isRegularFile(path)) {
                fileCounter.incrementAndGet();
            }
        }
    }
}
