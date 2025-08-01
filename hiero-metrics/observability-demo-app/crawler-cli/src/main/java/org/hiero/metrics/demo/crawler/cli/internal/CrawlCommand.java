// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.cli.internal;

import java.io.PrintStream;
import java.util.Arrays;
import org.hiero.metrics.demo.crawler.api.document.SchemeProcessor;
import org.hiero.metrics.demo.crawler.api.job.JobManager;
import org.hiero.metrics.demo.crawler.api.job.ScheduledJob;

public class CrawlCommand extends AbstractCommand {

    private final JobManager jobManager;

    private final String help;

    public CrawlCommand(JobManager jobManager) {
        super("crawl", "Crawl URI");
        this.jobManager = jobManager;

        StringBuilder helpBuilder = new StringBuilder();
        helpBuilder.append("Usage: crawl uri [depth] [processors...]\n");
        helpBuilder.append("Options:\n");
        helpBuilder.append("  uri          Required URI to crawl.\n");
        helpBuilder.append("  depth        Optional depth, default is 3.\n");
        helpBuilder.append(
                "  processors   Optional list of processors, in not specific - al available for URI scheme will be used.\n");

        helpBuilder.append("\nAvailable URI schemes and their processors:\n");

        for (SchemeProcessor scheme : jobManager.schemes()) {
            helpBuilder
                    .append("  ")
                    .append(scheme.getName())
                    .append(": ")
                    .append(scheme.processors().keySet())
                    .append('\n');
        }

        help = helpBuilder.toString();
    }

    @Override
    protected void executeCommand(PrintStream out, String... args) throws InputException {
        if (args.length == 0) {
            throw new InputException("Missing URI to crawl.");
        }

        String uri = args[0];

        int depth = 3; // Default depth;
        String[] processors = new String[0];

        if (args.length > 1) {
            int processorIdx = 1;
            try {
                depth = Integer.parseInt(args[1]);
                if (depth < 0) {
                    throw new InputException("Depth must be a positive integer.");
                }
                processorIdx++;
            } catch (NumberFormatException e) {
                // it is a processor name, not a depth
            }

            if (processorIdx < args.length) {
                processors = Arrays.copyOfRange(args, processorIdx, args.length - processorIdx);
            }
        }

        ScheduledJob scheduled = jobManager.schedule(uri, depth, processors);
        out.println("Scheduled job with id " + scheduled.jobId() + " for URI: " + uri);
    }

    @Override
    public String help() {
        return help;
    }
}
