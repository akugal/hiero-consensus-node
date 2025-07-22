// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.snapshot;

import edu.umd.cs.findbugs.annotations.NonNull;

public interface Exporter {

    @NonNull
    String getName();
}
