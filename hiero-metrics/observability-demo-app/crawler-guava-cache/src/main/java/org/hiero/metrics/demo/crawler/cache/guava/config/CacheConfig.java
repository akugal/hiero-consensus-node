// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.cache.guava.config;

import com.swirlds.config.api.ConfigData;
import com.swirlds.config.api.ConfigProperty;

@ConfigData("cache.doc.guava")
public record CacheConfig(@ConfigProperty(defaultValue = "maximumSize=100") String spec) {}
