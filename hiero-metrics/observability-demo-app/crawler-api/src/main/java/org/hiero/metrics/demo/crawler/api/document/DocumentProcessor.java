// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.api.document;

import org.hiero.metrics.demo.crawler.api.exception.DocumentProcessException;
import org.hiero.metrics.demo.crawler.api.util.Named;
import org.hiero.metrics.demo.crawler.api.util.TypedMap;

public interface DocumentProcessor extends Named {

    void process(Document document, TypedMap context) throws DocumentProcessException;
}
