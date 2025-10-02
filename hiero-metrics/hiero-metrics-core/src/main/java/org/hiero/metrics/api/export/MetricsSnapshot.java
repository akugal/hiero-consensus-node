// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.export;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.time.Instant;

public interface MetricsSnapshot extends Iterable<MetricSnapshot> {

    @NonNull
    Instant createAt();
}
