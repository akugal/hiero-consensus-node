// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.api.util;

public abstract class AbstractNamed implements Named {

    private final String name;

    protected AbstractNamed(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
