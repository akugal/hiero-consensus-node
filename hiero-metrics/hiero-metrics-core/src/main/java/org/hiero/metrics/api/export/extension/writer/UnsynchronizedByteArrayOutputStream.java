package org.hiero.metrics.api.export.extension.writer;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Objects;

/**
 * An unsynchronized variant of {@link java.io.ByteArrayOutputStream} that does not use synchronization.
 * It is not thread-safe and should be used in single-threaded contexts only.
 */
public final class UnsynchronizedByteArrayOutputStream extends OutputStream {

    private byte[] buffer;
    private int size = 0;

    /**
     * Creates a new UnsynchronizedByteArrayOutputStream with the default initial capacity 32 bytes.
     */
    public UnsynchronizedByteArrayOutputStream() {
        this(32);
    }

    /**
     * Creates a new UnsynchronizedByteArrayOutputStream with the specified initial capacity.
     *
     * @param capacity the initial capacity of the buffer
     * @throws IllegalArgumentException if the specified capacity is negative
     */
    public UnsynchronizedByteArrayOutputStream(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive");
        }
        buffer = new byte[capacity];
    }

    @Override
    public void write(@NonNull byte[] data) {
        if (data.length != 0) {
            ensureCapacity(size + data.length);
            System.arraycopy(data, 0, buffer, size, data.length);
            size += data.length;
        }
    }

    @Override
    public void write(@NonNull byte[] data, int off, int len) {
        Objects.checkFromIndexSize(off, len, data.length);

        if (len != 0) {
            ensureCapacity(size + len);
            System.arraycopy(data, off, buffer, size, len);
            size += len;
        }
    }

    @Override
    public void write(int b) {
        write((byte) b);
    }

    @Override
    public String toString() {
        return new String(buffer, 0, size);
    }

    public void write(byte b) {
        ensureCapacity(size + 1);
        buffer[size++] = b;
    }

    /**
     * Returns a copy of the internal buffer containing the written data.
     *
     * @return a byte array containing the written data
     */
    @NonNull
    public byte[] toByteArray() {
        return Arrays.copyOf(buffer, size);
    }

    /**
     * Writes the contents of this stream to the specified output stream.
     *
     * @param out the output stream to write to
     * @throws IOException if an I/O error occurs
     */
    public void writeTo(OutputStream out) throws IOException {
        out.write(buffer, 0, size);
    }

    /**
     * Resets the stream to be empty. The internal buffer is not cleared, but the size is set to zero.
     */
    public void reset() {
        size = 0;
    }

    /**
     * @return the current size of the stream (the number of bytes written)
     */
    public int size() {
        return size;
    }

    /**
     * @return true if the stream is empty, false otherwise
     */
    public boolean isEmpty() {
        return size == 0;
    }

    private void ensureCapacity(int requiredCapacity) {
        if (requiredCapacity >= buffer.length) {
            byte[] copy = new byte[Integer.max(2 * buffer.length, requiredCapacity)];
            System.arraycopy(buffer, 0, copy, 0, size);
            buffer = copy;
        }
    }
}
