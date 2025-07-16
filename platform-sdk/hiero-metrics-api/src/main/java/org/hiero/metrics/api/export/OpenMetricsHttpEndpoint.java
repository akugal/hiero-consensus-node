// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.export;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.spi.HttpServerProvider;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.hiero.metrics.api.core.MetricRegistry;
import org.hiero.metrics.api.core.snapshot.MetricSnapshot;
import org.hiero.metrics.api.export.writer.OpenMetricsSnapshotsWriter;

public class OpenMetricsHttpEndpoint {

    public static final String CONTENT_TYPE = "application/openmetrics-text; version=1.0.0; charset=utf-8";

    private final AtomicInteger lastResponseSize = new AtomicInteger(4096);
    private final OpenMetricsSnapshotsWriter exporter = new OpenMetricsSnapshotsWriter();

    public OpenMetricsHttpEndpoint(int port) throws IOException {
        final HttpServerProvider provider = HttpServerProvider.provider();
        HttpServer server = provider.createHttpServer(new InetSocketAddress(port), 3);
        server.createContext("/metrics", this::handleSnapshots);
        server.setExecutor(null);
        server.start();
    }

    private void handleSnapshots(HttpExchange exchange) throws IOException {
        try {
            List<MetricSnapshot> snapshot = MetricRegistry.getDefault().snapshot();
            ByteArrayOutputStream responseBuffer = new ByteArrayOutputStream(lastResponseSize.get() + 1024);
            exporter.export(snapshot, responseBuffer);
            lastResponseSize.set(responseBuffer.size());
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
