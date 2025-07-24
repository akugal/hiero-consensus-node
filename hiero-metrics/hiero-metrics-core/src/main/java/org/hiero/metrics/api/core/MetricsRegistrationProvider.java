// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.core;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Collection;

public interface MetricsRegistrationProvider {

    @NonNull
    Collection<Metric.Builder<?, ?>> getMetricsToRegister();
}
