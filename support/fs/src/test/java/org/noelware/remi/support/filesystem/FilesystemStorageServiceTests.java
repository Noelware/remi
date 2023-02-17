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

package org.noelware.remi.support.filesystem;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import org.junit.jupiter.api.*;
import org.noelware.remi.core.Blob;
import org.noelware.remi.core.UploadRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FilesystemStorageServiceTests {
    private static final FilesystemStorageService service = new FilesystemStorageService("./.remi/data");
    private static final Logger LOG = LoggerFactory.getLogger(FilesystemStorageServiceTests.class);

    @BeforeAll
    public static void beforeRun() throws IOException {
        service.init();
        for (int i = 0; i < 1000; i++) {
            final File file = new File(service.normalizePath(String.format("./heck-%d", i)));
            Files.writeString(
                    file.toPath(),
                    String.format("[%d] i’m a tiny polar bear that goes woof woof bark bark bark *wags tail*", i));
        }
    }

    @AfterAll
    public static void afterRun() throws IOException {
        if (!service.delete(".")) {
            LOG.error("Unable to delete directory [{}]", service.config().directory());
        }
    }

    @DisplayName("Can we list all file names in the given storage path")
    @Order(1)
    @Test
    public void test_canWeListAllFiles() throws IOException {
        final List<Blob> blobs = service.blobs();
        assertEquals(1000, blobs.size());
    }

    @DisplayName("Can we delete file ./heck-727")
    @Order(2)
    @Test
    public void test_canWeDeleteFile() throws IOException {
        assertTrue(service.delete("./heck-727"));
        assertEquals(999, service.blobs().size());
    }

    @DisplayName("Does file ./heck-1001 exist?")
    @Test
    public void test_canWeCheckIf1001FileExists() throws IOException {
        assertFalse(service.exists("./heck-1001"));

        service.upload(UploadRequest.builder()
                .withPath("./heck-1001")
                .withInputStream(new ByteArrayInputStream(
                        "[1001] i’m a tiny polar bear that goes woof woof bark bark bark *wags tail*"
                                .getBytes(StandardCharsets.UTF_8)))
                .build());

        assertTrue(service.exists("./heck-1001"));
        assertEquals(1000, service.blobs().size());
    }
}
