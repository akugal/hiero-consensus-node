// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.cli.internal;

public record CommandArgument(String name, String description, boolean required) {

    public CommandArgument(String name, String description) {
        this(name, description, false);
    }

    public CommandArgument(String name) {
        this(name, "", false);
    }
}
