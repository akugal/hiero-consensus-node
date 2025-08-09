import org.junit.jupiter.api.Test;

import java.time.Duration;

public class CrawlerTests {

    {
        //System.setProperty("log4j.configurationFile", "src/test/resources/log4j2-console.xml");
    }

    @Test
    public void testWithCacheAndWithout() throws InterruptedException {
        TestConfig config = createConfigUrlsDense();

        System.out.println("Running test with cache enabled");
        StressTestRunner.run("cache-enabled", config);

        Thread.sleep(1000); // Allow some time for metrics to be collected

        System.setProperty("cache.doc.guava.spec", ""); // Disable cache by setting an empty spec
        System.out.println("Running test with cache disabled");
        StressTestRunner.run("cache-disabled", config);
    }

    private static TestConfig createConfigFiles() {
        final String root = "file:///Users/akugal/projects/hiero/hiero-consensus-node-fork";

        return new TestConfig()
                .withTimeout(Duration.ofSeconds(10))
                .withThroughputPerSecond(4)
                .withItem(new TestItem(root + "/hiero-metrics").withDepth(50))
                .withItem(new TestItem(root).withDepth(50))
                .withItem(new TestItem(root + "/observability-demo-app").withDepth(50))
                .withItem(new TestItem(root + "/example-apps").withDepth(50))
                .withItem(new TestItem(root + "/hedera-node").withDepth(50));
    }

    private static TestConfig createConfigUrlsDense() {
        return new TestConfig()
                .withTimeout(Duration.ofSeconds(20))
                .withThroughputPerSecond(4)
                .withItem(new TestItem("http://python.org").withDepth(2))
                .withItem(new TestItem("http://docs.python.org/").withDepth(2))
                .withItem(new TestItem("https://mail.python.org").withDepth(2))
                .withItem(new TestItem("https://pycon.blogspot.com").withDepth(2));
    }

}
