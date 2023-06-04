/*
 * 🧶 remi: Simple Java library to handle communication between applications and storage providers.
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

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a resolver for resolving content types.
 */
public interface ContentTypeResolver {
    /**
     * Returns the content type of this {@link InputStream}.
     * @param stream The stream to check the content type of
     */
    @Nullable
    String resolve(InputStream stream) throws IOException;

    /**
     * Returns the content type of the given byte contents.
     * @param bytes Byte array to use
     */
    @Nullable
    String resolve(byte[] bytes) throws IOException;

    /**
     * Returns the content type of this {@link ByteBuffer}.
     * @param buffer {@link ByteBuffer} to use.
     */
    @Nullable
    String resolve(ByteBuffer buffer) throws IOException;
}
