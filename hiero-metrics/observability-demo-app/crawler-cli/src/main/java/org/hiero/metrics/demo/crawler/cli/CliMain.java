// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.cli;

import com.swirlds.config.api.Configuration;
import com.swirlds.config.api.ConfigurationBuilder;
import com.swirlds.config.extensions.sources.ClasspathFileConfigSource;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hiero.metrics.api.core.MetricRegistry;
import org.hiero.metrics.api.core.MetricsFacade;
import org.hiero.metrics.api.export.MetricsExportManager;
import org.hiero.metrics.demo.crawler.api.job.JobManager;
import org.hiero.metrics.demo.crawler.cli.internal.Command;
import org.hiero.metrics.demo.crawler.cli.internal.CommandManager;
import org.hiero.metrics.demo.crawler.cli.internal.CrawlCommand;
import org.hiero.metrics.demo.crawler.cli.internal.InputException;
import org.hiero.metrics.demo.crawler.cli.internal.JobCommand;

public class CliMain {

    private static final Logger logger = LogManager.getLogger(CliMain.class);

    public static void main(String[] args) throws IOException {
        JobManager jobManager = initializeJobManager();
        CommandManager commandManager =
                new CommandManager(List.of(new CrawlCommand(jobManager), new JobCommand(jobManager)));

        System.out.println("🕷️ Crawler Interactive CLI");
        System.out.println("Type 'help' for available commands or 'exit' to quit.\n");

        startInteractiveLoop(commandManager);

        jobManager.shutdown();
    }

    private static void startInteractiveLoop(CommandManager commandManager) {
        final Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("crawler> ");

            if (!scanner.hasNextLine()) {
                System.out.println("\nNo input available. Exiting...");
                break;
            }

            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                continue;
            }

            String[] args = input.split("\\s+");
            if (args.length == 0) {
                continue;
            }

            String commandName = args[0].toLowerCase();

            try {
                if ("exit".equals(commandName)) {
                    System.out.println("Goodbye! 👋");
                    break;
                }

                Command command = commandManager.getCommand(commandName);
                command.execute(System.out, Arrays.copyOfRange(args, 1, args.length));
            } catch (InputException e) {
                System.out.println("❌ " + e.getMessage());
            } catch (Exception e) {
                System.out.println("❌ " + e.getMessage());
                logger.error("Error executing command: " + commandName, e);
            }
        }

        scanner.close();
    }

    private static JobManager initializeJobManager() throws IOException {
        MetricRegistry registry = MetricsFacade.createRegistryWithDiscoveredProviders();
        MetricsExportManager exportManager = MetricsFacade.getDefaultExportManager();
        exportManager.manageMetricRegistry(registry);

        Configuration configuration = ConfigurationBuilder.create()
                .autoDiscoverExtensions()
                .withSources(new ClasspathFileConfigSource(Path.of("application.properties")))
                .build();

        JobManager jobManager = JobManager.create(configuration);
        jobManager.registerMetrics(registry);

        return jobManager;
    }
}
