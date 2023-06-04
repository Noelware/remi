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

package org.noelware.remi.support.gcs;

import static java.lang.String.format;

import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.NoCredentials;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.noelware.remi.core.Configuration;

public class GoogleCloudStorageConfig implements Configuration {
    @Nullable
    private final File credentialsFile;

    private final String bucketName;
    private final String projectId;
    private final String hostName;

    public static GoogleCloudStorageConfig fromSystemProperty(
            @NotNull String bucket, @NotNull String projectId, @Nullable String sysProp) {
        final String credentialsPath = Objects.requireNonNull(
                System.getProperty(sysProp != null ? sysProp : "-Dcom.google.storage.credentialsFile"));

        final File credentialsFile = new File(credentialsPath);
        if (!credentialsFile.exists())
            throw new IllegalStateException(format("Path [%s] doesn't exist", credentialsPath));

        if (!credentialsFile.isFile())
            throw new IllegalStateException(format("Path [%s] was not a file", credentialsPath));

        return new GoogleCloudStorageConfig(credentialsFile, projectId, bucket);
    }

    public static GoogleCloudStorageConfig fromEnvironmentVariable(
            @NotNull String bucket, @NotNull String projectId, @Nullable String envVar) {
        final String credentialsPath =
                Objects.requireNonNull(System.getenv(envVar != null ? envVar : "GOOGLE_APPLICATION_CREDENTIALS"));

        final File credentialsFile = new File(credentialsPath);
        if (!credentialsFile.exists())
            throw new IllegalStateException(format("Path [%s] doesn't exist", credentialsPath));

        if (!credentialsFile.isFile())
            throw new IllegalStateException(format("Path [%s] was not a file", credentialsPath));

        return new GoogleCloudStorageConfig(credentialsFile, projectId, bucket);
    }

    public GoogleCloudStorageConfig(@Nullable File credentialsFile, String projectId, String bucketName) {
        this.credentialsFile = credentialsFile;
        this.bucketName = bucketName;
        this.projectId = projectId;
        this.hostName = null;
    }

    public GoogleCloudStorageConfig(
            @Nullable File credentialsFile, String projectId, String bucketName, String hostName) {
        this.credentialsFile = credentialsFile;
        this.bucketName = bucketName;
        this.projectId = projectId;
        this.hostName = hostName;
    }

    public Credentials credentials() {
        if (credentialsFile == null) return NoCredentials.getInstance();
        try {
            return GoogleCredentials.fromStream(new FileInputStream(credentialsFile));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String bucketName() {
        return bucketName;
    }

    public String projectId() {
        return projectId;
    }

    public String hostName() {
        return hostName;
    }
}
