// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.export.extension.writer;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.hiero.metrics.api.core.Label;
import org.hiero.metrics.api.export.DataPointSnapshot;
import org.hiero.metrics.api.export.MetricSnapshot;

public final class WriterUtils {

    public static final byte COMMA = ',';
    public static final byte QUOTE = '"';
    public static final byte SPACE = ' ';
    public static final byte NEW_LINE = '\n';
    public static final byte OPEN_BRACKET = '{';
    public static final byte CLOSE_BRACKET = '}';
    public static final byte[] EQUALS_QUOTE = "=\"".getBytes(StandardCharsets.UTF_8);

    private WriterUtils() {}

    public static void appendLabels(
            TemplateByteArray.Builder buffer,
            MetricSnapshot metricSnapshot,
            DataPointSnapshot dataPointSnapshot,
            byte opening,
            byte closing) {

        if (!metricSnapshot.constantLabels().isEmpty()
                || !metricSnapshot.dynamicLabelNames().isEmpty()
                || dataPointSnapshot.valueClassifier() != null) {

            buffer.append(opening);
            boolean first = true;

            for (Label label : metricSnapshot.constantLabels()) {
                if (!first) {
                    buffer.append(COMMA);
                }
                first = false;
                buffer.append(label.name())
                        .append(EQUALS_QUOTE)
                        .append(escape(label.value()))
                        .append(QUOTE);
            }

            List<String> labelNames = metricSnapshot.dynamicLabelNames();
            for (int i = 0; i < labelNames.size(); i++) {
                String labelValue = dataPointSnapshot.labelValue(i);
                if (!first) {
                    buffer.append(COMMA);
                }
                first = false;
                buffer.append(labelNames.get(i))
                        .append(EQUALS_QUOTE)
                        .append(escape(labelValue))
                        .append(QUOTE);
            }

            String valueClassifier = dataPointSnapshot.valueClassifier();
            if (valueClassifier != null) {
                if (!first) {
                    buffer.append(COMMA);
                }
                buffer.append(valueClassifier).append(EQUALS_QUOTE);
                buffer.addPlaceholder();
                buffer.append(QUOTE);
            }

            buffer.append(closing);
        }
    }

    /**
     * Escape newline {@code \n}, double quote {@code "} and backslash {@code \} characters in string values.
     *
     * @param value the string value to escape
     * @return the escaped string
     */
    public static String escape(final String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n");
    }
}
