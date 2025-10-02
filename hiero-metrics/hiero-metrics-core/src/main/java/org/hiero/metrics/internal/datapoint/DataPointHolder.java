// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.internal.datapoint;

import org.hiero.metrics.internal.export.BaseDataPointSnapshot;

public record DataPointHolder<D>(D dataPoint, BaseDataPointSnapshot snapshot) {}
