// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.hiero.metrics.demo.crawler.api.document.Document;
import org.hiero.metrics.demo.crawler.api.document.DocumentProcessor;
import org.hiero.metrics.demo.crawler.api.exception.DocumentProcessException;
import org.hiero.metrics.demo.crawler.api.util.AbstractNamed;
import org.hiero.metrics.demo.crawler.api.util.TypedMap;

public class ReadFileContentDocumentProcessor extends AbstractNamed implements DocumentProcessor {

    public ReadFileContentDocumentProcessor() {
        super("read-file-content");
    }

    @Override
    public void process(Document document, TypedMap context) {
        Path path = Path.of(document.getUri());
        if (Files.isRegularFile(path)) {
            try {
                Files.readAllBytes(path);
            } catch (IOException e) {
                throw new DocumentProcessException("Failed to read file content: " + path, e);
            }
        }
    }
}
