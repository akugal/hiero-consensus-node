// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.internal;

import com.sun.management.UnixOperatingSystemMXBean;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.lang.management.BufferPoolMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.hiero.metrics.api.CallbackMetric;
import org.hiero.metrics.api.core.Metric;
import org.hiero.metrics.api.core.MetricsRegistrationProvider;

public class JvmMetricsRegistration implements MetricsRegistrationProvider {

    @NonNull
    @Override
    public Collection<Metric.Builder<?, ?>> getMetricsToRegister() {
        Collection<Metric.Builder<?, ?>> builders = new ArrayList<>();

        final OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        final BufferPoolMXBean directMemMxBean = getDirectMemMxBean();
        final String category = "jvm";

        builders.add(CallbackMetric.builder(CallbackMetric.key("memory").withCategory(category))
                .withDynamicLabelNames("type")
                .withDescription("JVM memory usage")
                .withUnit("bytes")
                .registerDataPoint(() -> Runtime.getRuntime().maxMemory(), Map.of("type", "max"))
                .registerDataPoint(() -> Runtime.getRuntime().totalMemory(), Map.of("type", "total"))
                .registerDataPoint(() -> Runtime.getRuntime().freeMemory(), Map.of("type", "free"))
                .registerDataPoint(
                        () -> directMemMxBean != null ? directMemMxBean.getMemoryUsed() : -1,
                        Map.of("type", "direct")));

        if (osBean instanceof UnixOperatingSystemMXBean mBean) {
            builders.add(CallbackMetric.builder(
                            CallbackMetric.key("open_file_descriptors").withCategory(category))
                    .withDescription("Number of open file descriptors")
                    .withUnit("count")
                    .registerDataPoint(mBean::getOpenFileDescriptorCount, Map.of()));
        }
        if (osBean instanceof com.sun.management.OperatingSystemMXBean mBean) {
            builders.add(CallbackMetric.builder(CallbackMetric.key("cpu_load").withCategory(category))
                    .withDescription("CPU load of the JVM process")
                    .withUnit("percent")
                    .registerDataPoint(mBean::getProcessCpuLoad, Map.of()));
        }

        builders.add(CallbackMetric.builder(
                        CallbackMetric.key("available_processors").withCategory(category))
                .withDescription("Available processors")
                .registerDataPoint(() -> Runtime.getRuntime().availableProcessors(), Map.of()));

        return builders;
    }

    private static @Nullable BufferPoolMXBean getDirectMemMxBean() {
        final List<BufferPoolMXBean> pools = ManagementFactory.getPlatformMXBeans(BufferPoolMXBean.class);
        for (final BufferPoolMXBean pool : pools) {
            if (pool.getName().equals("direct")) {
                return pool;
            }
        }
        return null;
    }
}
