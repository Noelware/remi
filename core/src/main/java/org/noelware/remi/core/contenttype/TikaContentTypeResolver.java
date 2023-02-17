/*
 * 🧶 remi: Robust, and simple Java-based library to handle storage-related communications with different storage provider.
 * Copyright (c) 2022-2023 Noelware, LLC. <team@noelware.org>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.noelware.remi.core.contenttype;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import org.apache.tika.Tika;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a {@link ContentTypeResolver} that uses Apache Tika to resolve
 * content types.
 */
public class TikaContentTypeResolver implements ContentTypeResolver {
    private final Tika TIKA = new Tika();

    @Override
    public @Nullable String resolve(InputStream stream) throws IOException {
        return TIKA.detect(stream);
    }

    @Override
    public @Nullable String resolve(byte[] bytes) throws IOException {
        try (final ByteArrayInputStream stream = new ByteArrayInputStream(bytes)) {
            return resolve(stream);
        }
    }

    @Override
    public @Nullable String resolve(ByteBuffer buffer) throws IOException {
        // We need to convert the ByteBuffer into a InputStream.
        try (final ByteArrayInputStream stream = new ByteArrayInputStream(buffer.array())) {
            return resolve(stream);
        }
    }
}
