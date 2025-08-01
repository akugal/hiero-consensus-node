// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.api.job;

import java.util.Collection;
import org.hiero.metrics.demo.crawler.api.document.DocumentFetcher;
import org.hiero.metrics.demo.crawler.api.document.DocumentProcessor;

public record JobConfig(DocumentFetcher fetcher, Collection<DocumentProcessor> processors, int depth) {}
