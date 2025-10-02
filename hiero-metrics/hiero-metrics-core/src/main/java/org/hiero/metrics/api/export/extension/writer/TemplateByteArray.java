// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.export.extension.writer;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class TemplateByteArray {

    private final byte[][] chunks;
    private final int placeholdersSize;

    private TemplateByteArray(Builder builder) {
        chunks = builder.chunks.toArray(new byte[0][]);
        placeholdersSize = builder.placeholdersSize;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Iterator<byte[]> iterator(final byte[]... variables) {
        return iterator(variables.length, variables);
    }

    public Iterator<byte[]> iterator(int varsCount, final byte[]... variables) {
        if (varsCount > variables.length) {
            throw new IllegalArgumentException(
                    "Vars count is greater than variables array length: " + varsCount + " > " + variables.length);
        }
        if (varsCount != placeholdersSize) {
            throw new IllegalArgumentException("Number of variables is not equal to number of placeholders: expected "
                    + placeholdersSize + " but got " + varsCount);
        }

        return new Iterator<>() {

            private int chunkIdx = 0;
            private int variableIdx = 0;

            @Override
            public boolean hasNext() {
                return chunkIdx < chunks.length;
            }

            @Override
            public byte[] next() {
                final byte[] chunk = chunks[chunkIdx++];
                if (chunk != null) {
                    return chunk;
                }

                return variables[variableIdx++];
            }
        };
    }

    public static final class Builder {

        private final List<byte[]> chunks = new ArrayList<>();

        private byte[] builderChunk = new byte[256];
        private int builderChunkSize = 0;
        private int placeholdersSize = 0;

        private void ensureCapacity(int dataSize) {
            if (builderChunkSize + dataSize >= builderChunk.length) {
                // keep 256 bytes additional space when grow
                byte[] copy = new byte[builderChunkSize + dataSize + 256];
                System.arraycopy(builderChunk, 0, copy, 0, builderChunkSize);
                builderChunk = copy;
            }
        }

        public Builder append(String data) {
            return append(data.getBytes(StandardCharsets.UTF_8));
        }

        public Builder append(byte b) {
            ensureCapacity(1);
            builderChunk[builderChunkSize++] = b;
            return this;
        }

        public Builder append(byte[] data) {
            if (data.length == 0) {
                return this;
            }
            ensureCapacity(data.length);
            System.arraycopy(data, 0, builderChunk, builderChunkSize, data.length);
            builderChunkSize += data.length;
            return this;
        }

        public Builder addPlaceholder() {
            finalizeBuilderChunk();
            chunks.add(null); // placeholder
            placeholdersSize++;
            return this;
        }

        private void finalizeBuilderChunk() {
            if (builderChunkSize == 0) {
                return;
            }
            final byte[] finalChunk = new byte[builderChunkSize];
            System.arraycopy(builderChunk, 0, finalChunk, 0, builderChunkSize);
            chunks.add(finalChunk);
            builderChunkSize = 0;
        }

        public TemplateByteArray build() {
            finalizeBuilderChunk();
            return new TemplateByteArray(this);
        }
    }
}
