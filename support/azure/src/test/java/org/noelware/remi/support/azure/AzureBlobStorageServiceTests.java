/*
 * 🧶 Remi: Robust, and simple Java-based library to handle storage-related communications with different storage provider.
 * Copyright (c) 2022 Noelware <team@noelware.org>
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

package org.noelware.remi.support.azure;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.noelware.remi.core.Blob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Only enable this when we can do CI on this (since it's not testable atm)
@Disabled
public class AzureBlobStorageServiceTests {
    // constants
    private static final String AZURE_SAS_TOKEN_ENV_KEY = "AZURE_SAS_TOKEN";
    private static final String AZURE_ENDPOINT_ENV_KEY = "AZURE_ENDPOINT";

    private static final Logger LOG = LoggerFactory.getLogger(AzureBlobStorageServiceTests.class);
    private static AzureBlobStorageService service;

    @BeforeAll
    public static void beforeRun() {
        //        final String azureSasToken = System.getenv("AZURE_SAS_TOKEN");
        //        if (azureSasToken == null) throw new IllegalStateException("Missing `AZURE_SAS_TOKEN` in test");
        //
        //        final String azureEndpoint = System.getenv("AZURE_ENDPOINT");
        //        if (azureEndpoint == null) throw new IllegalStateException("Missing `AZURE_ENDPOINT` in test");

        LOG.info("Initializing Azure Blob Storage service provider...");
        service = new AzureBlobStorageService(new AzureBlobStorageConfig(
                "remi-test", System.getenv(AZURE_ENDPOINT_ENV_KEY), System.getenv(AZURE_SAS_TOKEN_ENV_KEY)));

        service.init();
    }

    @DisplayName("Can we list all blobs")
    @Test
    public void test_canWeListAllBlobs() {
        final List<Blob> blobs = service.blobs();
        assertEquals(0, blobs.size());
    }
}
