// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.http;

import java.net.URI;
import java.util.Map;
import java.util.function.BiFunction;
import org.hiero.metrics.demo.crawler.api.document.Document;
import org.hiero.metrics.demo.crawler.api.document.DocumentProcessor;
import org.hiero.metrics.demo.crawler.api.util.AbstractNamed;
import org.hiero.metrics.demo.crawler.api.util.FactoryTypedKey;
import org.hiero.metrics.demo.crawler.api.util.TypedMap;

public class HostCounterDocumentProcessor extends AbstractNamed implements DocumentProcessor {

    private static final BiFunction<Integer, Integer, Integer> SUM = Integer::sum;

    private static final FactoryTypedKey<Map<String, Integer>> HOST_COUNTER_KEY =
            FactoryTypedKey.asMapThreadSafe("host-counter");

    public HostCounterDocumentProcessor() {
        super("host-counter");
    }

    @Override
    public void process(Document document, TypedMap context) {
        Map<String, Integer> hostCounters = context.computeIfAbsent(HOST_COUNTER_KEY);

        for (URI reference : document.getLinks()) {
            String host = reference.getHost();
            if (host != null) {
                if (host.startsWith("www.")) {
                    host = host.substring(4); // Normalize by removing 'www.'
                }

                hostCounters.merge(host, 1, SUM);
            }
        }
    }
}
