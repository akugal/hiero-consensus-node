// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.openmetrics;

import com.google.auto.service.AutoService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.spi.HttpServerProvider;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hiero.metrics.api.snapshot.MetricsSnapshot;
import org.hiero.metrics.api.snapshot.extension.OpenMetricsSnapshotsWriter;
import org.hiero.metrics.api.snapshot.extension.PullingMetricsExporterAdapter;

@AutoService(PullingMetricsExporterAdapter.class)
public class OpenMetricsHttpEndpoint extends PullingMetricsExporterAdapter {

    private static final Logger logger = LogManager.getLogger(OpenMetricsHttpEndpoint.class);

    public static final String CONTENT_TYPE = "application/openmetrics-text; version=1.0.0; charset=utf-8";

    private final AtomicInteger lastResponseSize = new AtomicInteger(4096);
    private final OpenMetricsSnapshotsWriter exporter = new OpenMetricsSnapshotsWriter();
    private final AtomicReference<Supplier<MetricsSnapshot>> snapshotSupplierRef = new AtomicReference<>(null);

    public OpenMetricsHttpEndpoint() throws IOException {
        this(8888);
    }

    public OpenMetricsHttpEndpoint(int port) throws IOException {
        super("open-metrics-http-endpoint");

        final String path = "/metrics";
        final HttpServerProvider provider = HttpServerProvider.provider();
        HttpServer server = provider.createHttpServer(new InetSocketAddress(port), 3);
        server.createContext(path, this::handleSnapshots);
        server.setExecutor(null);
        server.start();

        System.out.println("OpenMetrics HTTP endpoint started. port=" + port + ", path=" + path);
        logger.info("OpenMetrics HTTP endpoint started. port={}, path={}", port, path);
    }

    private void handleSnapshots(HttpExchange exchange) throws IOException {
        System.out.println("Received request for OpenMetrics snapshots: " + exchange.getRequestURI());
        try {
            Optional<MetricsSnapshot> optionalSnapshot = getSnapshot();
            if (optionalSnapshot.isEmpty()) {
                System.out.println("No metrics snapshot available, returning 204 No Content");
                exchange.sendResponseHeaders(204, 0); // No Content
                return;
            }

            ByteArrayOutputStream responseBuffer = new ByteArrayOutputStream(lastResponseSize.get() + 1024);
            exporter.export(optionalSnapshot.get(), responseBuffer);
            lastResponseSize.set(responseBuffer.size());
            logger.debug("Exporting metrics snapshot, sizeBytes={}", lastResponseSize.get());

            exchange.getResponseHeaders().set("Content-Type", CONTENT_TYPE);
            // TODO see if gzip is supported

            int contentLength = responseBuffer.size();
            if (contentLength > 0) {
                exchange.getResponseHeaders().set("Content-Length", String.valueOf(contentLength));
            }

            exchange.sendResponseHeaders(200, contentLength);
            responseBuffer.writeTo(exchange.getResponseBody());
        } catch (IOException e) {
            // TODO error handling
            System.out.println("Error exporting metrics: " + e.getMessage());
            e.printStackTrace();
            exchange.sendResponseHeaders(500, 0);
        } catch (RuntimeException e) {
            // TODO error handling
            System.out.println("Error exporting metrics: " + e.getMessage());
            e.printStackTrace();
            exchange.sendResponseHeaders(500, 0);
        } finally {
            exchange.close();
        }
    }
}
