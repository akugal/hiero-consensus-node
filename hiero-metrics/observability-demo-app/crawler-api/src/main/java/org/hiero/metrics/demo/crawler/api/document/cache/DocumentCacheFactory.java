// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.api.document.cache;

import com.swirlds.config.api.Configuration;

@FunctionalInterface
public interface DocumentCacheFactory {

    DocumentCacheFactory NO_OP = configuration -> NoOpDocumentCache.INSTANCE;

    DocumentCache createDocumentCache(Configuration configuration);
}
