// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.cli.internal;

import java.io.PrintStream;
import java.time.Duration;
import java.util.Arrays;
import org.hiero.metrics.demo.crawler.api.document.SchemeCrawler;
import org.hiero.metrics.demo.crawler.api.job.JobManager;
import org.hiero.metrics.demo.crawler.api.job.ScheduledJob;

public class CrawlCommand extends AbstractCommand {

    private final JobManager jobManager;

    private final String help;

    public CrawlCommand(JobManager jobManager) {
        super("crawl", "Crawl URI");
        this.jobManager = jobManager;

        StringBuilder helpBuilder = new StringBuilder();
        helpBuilder.append("Usage: crawl uri [timeout] [depth] [processors...]\n");
        helpBuilder.append("Options:\n");
        helpBuilder.append("  uri          Required URI to crawl.\n");
        helpBuilder.append("  timeout      Required job execution timeout in milliseconds.\n");
        helpBuilder.append("  depth        Required depth.\n");
        helpBuilder.append(
                "  processors   Optional list of processors, in not specific - al available for URI scheme will be used.\n");

        helpBuilder.append("\nAvailable URI schemes and their processors:\n");

        for (SchemeCrawler scheme : jobManager.schemes()) {
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
        if (args.length < 3) {
            throw new InputException("Missing required arguments See help for details.");
        }

        String uri = args[0];

        long timeout = parseLong(args[1], "timeout");
        int depth = (int) parseLong(args[2], "depth");
        String[] processors = Arrays.copyOfRange(args, 3, args.length);

        ScheduledJob scheduled = jobManager.schedule(uri, Duration.ofMillis(timeout), depth, processors);
        out.println("Scheduled job with id " + scheduled.getJobId() + " for URI: " + uri);
    }

    private long parseLong(String arg, String argument) throws InputException {
        try {
            long value = Long.parseLong(arg);
            if (value <= 0) {
                throw new InputException("Argument '" + argument + "' must be a positive.");
            }
            return value;
        } catch (NumberFormatException e) {
            throw new InputException("Argument '" + argument + "' must be a number.");
        }
    }

    @Override
    public String help() {
        return help;
    }
}
