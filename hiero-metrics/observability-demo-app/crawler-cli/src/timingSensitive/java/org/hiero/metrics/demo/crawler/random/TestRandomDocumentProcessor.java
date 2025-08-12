// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.random; // SPDX-License-Identifier: Apache-2.0

import org.hiero.metrics.demo.crawler.IdempotentTimedProcessor;
import org.hiero.metrics.demo.crawler.api.document.Document;
import org.hiero.metrics.demo.crawler.api.document.DocumentProcessor;
import org.hiero.metrics.demo.crawler.api.exception.DocumentProcessException;
import org.hiero.metrics.demo.crawler.api.util.TypedMap;

public class TestRandomDocumentProcessor implements DocumentProcessor {

    private final IdempotentTimedProcessor processor;

    public TestRandomDocumentProcessor(IdempotentTimedProcessor processor) {
        this.processor = processor;
    }

    @Override
    public void process(Document document, TypedMap context) throws DocumentProcessException {
        try {
            processor.process(document.getUri());
        } catch (InterruptedException e) {
            throw new DocumentProcessException("Interrupted while processing document: " + document.getUri(), e);
        }
    }

    @Override
    public String getName() {
        return "simulated-io";
    }
}
