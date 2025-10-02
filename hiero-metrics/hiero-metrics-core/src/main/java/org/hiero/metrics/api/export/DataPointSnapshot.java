// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.export;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public interface DataPointSnapshot {

    @NonNull
    String labelValue(int idx);

    @Nullable
    String valueClassifier();

    int valuesSize();

    double valueAt(int idx);

    @Nullable
    String valueTypeAt(int idx);
}
