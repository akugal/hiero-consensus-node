// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.file;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import org.hiero.metrics.demo.crawler.api.document.Document;
import org.hiero.metrics.demo.crawler.api.document.DocumentProcessor;
import org.hiero.metrics.demo.crawler.api.util.AbstractNamed;
import org.hiero.metrics.demo.crawler.api.util.FactoryTypedKey;
import org.hiero.metrics.demo.crawler.api.util.TypedMap;

public class PathCounterDocumentProcessor extends AbstractNamed implements DocumentProcessor {

    private static final BiFunction<Integer, Integer, Integer> SUM = Integer::sum;

    private static final FactoryTypedKey<AtomicInteger> FILE_COUNTER_KEY =
            new FactoryTypedKey<>("file-counter", AtomicInteger.class, AtomicInteger::new);

    private static final FactoryTypedKey<AtomicInteger> DIR_COUNTER_KEY =
            new FactoryTypedKey<>("dir-counter", AtomicInteger.class, AtomicInteger::new);

    private static final FactoryTypedKey<Map<String, Integer>> FILE_EXTENSION_COUNT_KEY =
            FactoryTypedKey.asMapThreadSafe("host-counter");

    public PathCounterDocumentProcessor() {
        super("path-counter");
    }

    @Override
    public void process(Document document, TypedMap context) {
        Path path = Path.of(document.getUri());
        if (Files.isDirectory(path)) {
            context.computeIfAbsent(DIR_COUNTER_KEY).incrementAndGet();
        } else if (Files.isRegularFile(path)) {
            context.computeIfAbsent(FILE_COUNTER_KEY).incrementAndGet();

            String extension = "";
            Path fileName = path.getFileName();
            if (fileName != null) {
                String name = fileName.toString();
                int lastDotIndex = name.lastIndexOf('.');
                if (lastDotIndex > 0 && lastDotIndex < name.length() - 1) {
                    extension = name.substring(lastDotIndex + 1);
                }
            }
            context.computeIfAbsent(FILE_EXTENSION_COUNT_KEY).merge(extension, 1, SUM);
        }
    }
}
