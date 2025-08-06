// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.api.document;

import java.util.concurrent.atomic.AtomicLong;
import org.hiero.metrics.demo.crawler.api.exception.DocumentProcessException;
import org.hiero.metrics.demo.crawler.api.util.FactoryTypedKey;
import org.hiero.metrics.demo.crawler.api.util.TypedMap;

public class DocumentSizeProcessor implements DocumentProcessor {

    private static final FactoryTypedKey<AtomicLong> DOCS_SIZE_KEY =
            new FactoryTypedKey<>("documents-size-bytes", AtomicLong.class, AtomicLong::new);

    @Override
    public void process(Document document, TypedMap context) throws DocumentProcessException {
        context.computeIfAbsent(DOCS_SIZE_KEY).addAndGet(document.sizeInBytes());
    }

    @Override
    public String getName() {
        return "document-size-bytes";
    }
}
