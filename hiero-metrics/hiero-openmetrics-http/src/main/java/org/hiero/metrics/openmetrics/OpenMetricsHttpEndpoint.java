// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.openmetrics;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.spi.HttpServerProvider;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hiero.metrics.api.export.MetricsSnapshot;
import org.hiero.metrics.api.export.extension.PullingMetricsExporterAdapter;
import org.hiero.metrics.api.export.extension.writer.OpenMetricsSnapshotsWriter;
import org.hiero.metrics.openmetrics.config.OpenMetricsHttpEndpointConfig;

public class OpenMetricsHttpEndpoint extends PullingMetricsExporterAdapter {

    private static final Logger logger = LogManager.getLogger(OpenMetricsHttpEndpoint.class);

    public static final String CONTENT_TYPE = "application/openmetrics-text; version=1.0.0; charset=utf-8";

    private final HttpServer server;
    private final AtomicInteger lastResponseSize = new AtomicInteger(4096);
    private final OpenMetricsSnapshotsWriter writer = new OpenMetricsSnapshotsWriter();

    public OpenMetricsHttpEndpoint(OpenMetricsHttpEndpointConfig config) throws IOException {
        super("open-metrics-http-endpoint");

        final HttpServerProvider provider = HttpServerProvider.provider();
        server = provider.createHttpServer(new InetSocketAddress(config.port()), config.backlog());
        server.createContext(config.path(), this::handleSnapshots);
        server.setExecutor(null);
        server.start();

        logger.info("OpenMetrics HTTP endpoint started. port={}, path={}", config.port(), config.path());
    }

    private void handleSnapshots(HttpExchange exchange) throws IOException {
        try {
            Optional<MetricsSnapshot> optionalSnapshot = getSnapshot();
            if (optionalSnapshot.isEmpty()) {
                exchange.sendResponseHeaders(204, 0); // No Content
                return;
            }

            ByteArrayOutputStream responseBuffer = new ByteArrayOutputStream(lastResponseSize.get() + 1024);
            writer.write(optionalSnapshot.get(), responseBuffer);
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
        } catch (RuntimeException e) {
            // TODO error handling
            logger.error("Error exporting metrics snapshot", e);
            exchange.sendResponseHeaders(500, 0);
        } finally {
            exchange.close();
        }
    }

    @Override
    public void close() throws IOException {
        super.close();
        server.stop(1);
    }
}
