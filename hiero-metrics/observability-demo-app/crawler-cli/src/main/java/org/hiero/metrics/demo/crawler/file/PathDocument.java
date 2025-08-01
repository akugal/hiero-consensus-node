// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.file;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.hiero.metrics.demo.crawler.api.document.AbstractDocument;
import org.hiero.metrics.demo.crawler.api.exception.DocumentFetchException;

public class PathDocument extends AbstractDocument {

    private final Path path;
    private final List<URI> links;

    public PathDocument(URI uri) {
        super(uri);

        path = Path.of(uri);

        if (Files.isDirectory(path)) {
            try {
                links = Files.list(path).map(Path::toUri).toList();
            } catch (Exception e) {
                throw new DocumentFetchException(e);
            }
        } else {
            links = List.of();
        }
    }

    @Override
    public long sizeInBytes() {
        if (Files.isRegularFile(path)) {
            try {
                return Files.size(path);
            } catch (Exception e) {
                throw new DocumentFetchException(e);
            }
        }
        return 0;
    }

    @Override
    public List<URI> getLinks() {
        return links;
    }
}
