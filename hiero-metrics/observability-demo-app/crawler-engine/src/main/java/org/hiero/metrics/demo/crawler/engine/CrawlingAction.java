package org.hiero.metrics.demo.crawler.engine;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

public class CrawlingAction extends RecursiveAction {

    private final URI url;
    private final int depth;
    private final JobResult.Builder jobResult;

    public CrawlingAction(URI url, int depth, JobResult.Builder jobResult) {
        this.url = url;
        this.depth = depth;
        this.jobResult = jobResult;
    }

    @Override
    protected void compute() {
        if (depth == 0) {
            return; // Base case: no more depth to crawl
        }

        Optional<CrawlResult> optionalResult = jobResult.crawl(url);

        if (optionalResult.isPresent()) {
            CrawlResult crawlResult = optionalResult.get();

            List<ForkJoinTask<Void>> forks = crawlResult.references().stream()
                    .map(ref -> new CrawlingAction(ref, depth - 1, jobResult))
                    .map(ForkJoinTask::fork)
                    .toList();

            for (ForkJoinTask<Void> fork : forks) {
                fork.join();
            }
        }
    }
}
