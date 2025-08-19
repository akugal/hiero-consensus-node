// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.cli.internal;

import java.io.PrintStream;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import org.hiero.metrics.demo.crawler.api.job.JobManager;
import org.hiero.metrics.demo.crawler.api.job.JobResult;
import org.hiero.metrics.demo.crawler.api.job.ScheduledJob;
import org.hiero.metrics.demo.crawler.api.job.metrics.JobProcessingMetrics;
import org.hiero.metrics.demo.crawler.api.job.metrics.JobTaskMetrics;
import org.hiero.metrics.demo.crawler.api.util.TypedMap;

public class JobCommand extends AbstractCommand {

    private final JobManager jobManager;

    private final String help =
            """
            Print status or cancel scheduled jobs.
            Usage: job status|cancel job_id
            Options:
              job_id    Required bob id to print details for
            """;

    public JobCommand(JobManager jobManager) {
        super("job", "Prints job details");
        this.jobManager = jobManager;
    }

    @Override
    protected void executeCommand(PrintStream out, String... args) throws Exception {
        if (args.length == 0) {
            throw new InputException("Missing Job ID.");
        }

        String subCommand = args[0];
        int jobId = Integer.parseInt(args[1]);

        Optional<ScheduledJob> optionalJob = jobManager.getJob(jobId);
        if (optionalJob.isEmpty()) {
            out.println("No job found with ID: " + jobId);
        } else {
            ScheduledJob scheduledJob = optionalJob.get();

            if (subCommand.equals("cancel")) {
                scheduledJob.cancel();
            } else if (subCommand.equals("status")) {
                if (scheduledJob.isDone()) {
                    if (scheduledJob.isCancelled()) {
                        out.println("Job ID: " + jobId + " has been cancelled.");
                    } else {
                        JobResult result = scheduledJob.getResult();
                        StringBuilder sb = new StringBuilder();

                        sb.append("Job ")
                                .append(jobId)
                                .append(" for ")
                                .append(result.rootUri())
                                .append(" completed. ")
                                .append("\n");

                        // sb.append("Data:\n");
                        // nestedOutput(result.data().asMap(), "  ", sb);

                        sb.append("Processing metrics:\n");
                        printMetrics(sb, result.jobMetrics().processingMetrics(), "  ");
                        sb.append("Concurrency metrics:\n");
                        printMetrics(sb, result.jobMetrics().concurrencyMetrics(), "  ");

                        out.println(sb);
                    }
                } else {
                    out.println("Job " + jobId + " is still running.");
                }
            } else {
                out.println("❌ Unknown job command: " + subCommand);
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

    private void printMetrics(StringBuilder builder, JobProcessingMetrics metrics, String indent) {
        builder.append(indent).append("URI count:\n");
        builder.append(indent)
                .append(indent)
                .append("Distinct:    ")
                .append(metrics.distinctUriCount())
                .append('\n');
        builder.append(indent)
                .append(indent)
                .append("Duplicate:   ")
                .append(metrics.duplicateUriCount())
                .append('\n');
        builder.append(indent)
                .append(indent)
                .append("Unsupported: ")
                .append(metrics.unsupportedUriCount())
                .append('\n');

        builder.append(indent).append("Duration:\n");
        builder.append(indent)
                .append(indent)
                .append("Job:             ")
                .append(durationToString(metrics.jobDuration()))
                .append('\n');
        builder.append(indent)
                .append(indent)
                .append("Fetch success:   ")
                .append(durationToString(metrics.fetchSuccessTotalDuration()))
                .append('\n');
        builder.append(indent)
                .append(indent)
                .append("Fetch error:     ")
                .append(durationToString(metrics.fetchErrorTotalDuration()))
                .append('\n');
        builder.append(indent)
                .append(indent)
                .append("Process success: ")
                .append(durationToString(metrics.processSuccessTotalDuration()))
                .append('\n');

        builder.append(indent).append("Fetch count:\n");
        builder.append(indent)
                .append(indent)
                .append("Success:   ")
                .append(metrics.fetchSuccessCount())
                .append('\n');
        builder.append(indent)
                .append(indent)
                .append("Error:     ")
                .append(metrics.fetchErrorsCount())
                .append('\n');
        builder.append(indent)
                .append(indent)
                .append("Cache hit: ")
                .append(metrics.getUriCacheHitCount())
                .append('\n');

        builder.append(indent)
                .append("Concurrency factor as sum(task_duration)/job_duration: ")
                .append(metrics.concurrencyFactor())
                .append('x')
                .append('\n');
    }

    private void printMetrics(StringBuilder builder, JobTaskMetrics metrics, String indent) {
        builder.append(indent)
                .append("Total tasks count:           ")
                .append(metrics.totalTasksCount())
                .append('\n');
        builder.append(indent)
                .append("Rejected tasks count:        ")
                .append(metrics.rejectedTasksCount())
                .append('\n');

        builder.append(indent)
                .append("Tasks execution delay total: ")
                .append(durationToString(metrics.tasksExecutionDelayTotalDuration()))
                .append('\n');
        builder.append(indent)
                .append("Task execution delay avg:    ")
                .append(durationToString(metrics.taskExecutionDelayAverageDuration()))
                .append('\n');

        builder.append(indent)
                .append("Tasks execution total:       ")
                .append(durationToString(metrics.taskExecutionTotalDuration()))
                .append('\n');
        builder.append(indent)
                .append("Task execution avg:          ")
                .append(durationToString(metrics.taskExecutionAverageDuration()))
                .append('\n');
    }

    private String durationToString(Duration duration) {
        return String.format("%d ms", duration.toMillis());
    }

    @Override
    public String help() {
        return help;
    }
}
