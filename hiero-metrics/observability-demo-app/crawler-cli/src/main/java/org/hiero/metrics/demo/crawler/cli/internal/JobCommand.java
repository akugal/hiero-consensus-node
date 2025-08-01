// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.cli.internal;

import java.io.PrintStream;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Future;
import org.hiero.metrics.demo.crawler.api.job.JobManager;
import org.hiero.metrics.demo.crawler.api.job.JobMetrics;
import org.hiero.metrics.demo.crawler.api.job.JobResult;
import org.hiero.metrics.demo.crawler.api.job.ScheduledJob;
import org.hiero.metrics.demo.crawler.api.util.TypedMap;

public class JobCommand extends AbstractCommand {

    private final JobManager jobManager;

    private final String help =
            """
            Usage: job job_id
            Options:
              job_id    Job id to print details for""";

    public JobCommand(JobManager jobManager) {
        super("job", "Prints job details");
        this.jobManager = jobManager;
    }

    @Override
    protected void executeCommand(PrintStream out, String... args) throws Exception {
        if (args.length == 0) {
            throw new InputException("Missing Job ID.");
        }

        int jobId = Integer.parseInt(args[0]);

        Optional<ScheduledJob> job = jobManager.getJob(jobId);
        if (job.isEmpty()) {
            out.println("No job found with ID: " + jobId);
        } else {
            Future<JobResult> jobFuture = job.get().future();
            if (jobFuture.isDone()) {
                if (jobFuture.isCancelled()) {
                    out.println("Job ID: " + jobId + " has been cancelled.");
                } else {
                    JobResult result = jobFuture.get();
                    StringBuilder sb = new StringBuilder();

                    sb.append("Job ")
                            .append(jobId)
                            .append(" for ")
                            .append(result.rootUri())
                            .append(" completed. ")
                            .append("\n");

                    sb.append("Data:\n");
                    nestedOutput(result.data().asMap(), "  ", sb);

                    sb.append("Metrics:\n");
                    printMetrics(sb, result.jobMetrics(), "  ");

                    out.println(sb);
                }
            } else {
                out.println("Job " + jobId + " is still running.");
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void nestedOutput(Map<String, Object> data, String indent, StringBuilder builder) {
        for (Map.Entry<String, Object> dataItem : data.entrySet()) {
            if (dataItem.getValue() instanceof Map map) {
                builder.append(indent).append(dataItem.getKey()).append(": ").append('\n');
                nestedOutput(map, indent + indent, builder);
            }
            if (dataItem.getValue() instanceof TypedMap typedMap) {
                builder.append(indent).append(dataItem.getKey()).append(": ").append('\n');
                nestedOutput(typedMap.asMap(), indent + indent, builder);
            } else {
                // Otherwise, just print the key-value pair
                builder.append(indent)
                        .append(dataItem.getKey())
                        .append(": ")
                        .append(dataItem.getValue())
                        .append("\n");
            }
        }
    }

    private void printMetrics(StringBuilder builder, JobMetrics jobMetrics, String indent) {
        builder.append(indent).append("URI count:\n");
        builder.append(indent)
                .append(indent)
                .append("Distinct:    ")
                .append(jobMetrics.distinctUriCount())
                .append('\n');
        builder.append(indent)
                .append(indent)
                .append("Duplicate:   ")
                .append(jobMetrics.duplicateUriCount())
                .append('\n');
        builder.append(indent)
                .append(indent)
                .append("Unsupported: ")
                .append(jobMetrics.unsupportedUriCount())
                .append('\n');

        builder.append(indent).append("Duration (ms):\n");
        builder.append(indent)
                .append(indent)
                .append("Job:             ")
                .append(jobMetrics.durationMs())
                .append('\n');
        builder.append(indent)
                .append(indent)
                .append("Fetch success:   ")
                .append(jobMetrics.fetchSuccessTotalTimeMs())
                .append('\n');
        builder.append(indent)
                .append(indent)
                .append("Fetch error:     ")
                .append(jobMetrics.fetchErrorTotalTimeMs())
                .append('\n');
        builder.append(indent)
                .append(indent)
                .append("Process success: ")
                .append(jobMetrics.processSuccessTotalTimeMs())
                .append('\n');

        builder.append(indent).append("Fetch count:\n");
        builder.append(indent)
                .append(indent)
                .append("Success:   ")
                .append(jobMetrics.fetchSuccessCount())
                .append('\n');
        builder.append(indent)
                .append(indent)
                .append("Error:     ")
                .append(jobMetrics.fetchErrorsCount())
                .append('\n');
        builder.append(indent)
                .append(indent)
                .append("Cache hit: ")
                .append(jobMetrics.getUriCacheHitCount())
                .append('\n');

        builder.append(indent)
                .append("Process success count: ")
                .append(jobMetrics.processSuccessTotalTimeMs())
                .append('\n');
        builder.append(indent)
                .append("Parallel time improvement ratio: ")
                .append(jobMetrics.parallelImprovementRatio())
                .append('x')
                .append('\n');
    }

    @Override
    public String help() {
        return help;
    }
}
