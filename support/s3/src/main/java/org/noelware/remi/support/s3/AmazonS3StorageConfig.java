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

package org.noelware.remi.support.s3;

import java.net.URI;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.jetbrains.annotations.Nullable;
import org.noelware.remi.core.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.BucketCannedACL;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;

public class AmazonS3StorageConfig implements Configuration {
    private final ObjectCannedACL defaultObjectAcl;
    private final BucketCannedACL defaultBucketAcl;
    private final boolean enableSignerV4Requests;
    private final boolean enforcePathAccessStyle;
    private final ExecutorService taskParallelExecutor;
    private final String secretAccessKey;
    private final String accessKeyId;
    private final String endpoint;
    private final String prefix;
    private final Region region;
    private final String bucket;

    protected AmazonS3StorageConfig(
            ObjectCannedACL defaultObjectAcl,
            BucketCannedACL defaultBucketAcl,
            boolean enableSignerV4Requests,
            boolean enforcePathAccessStyle,
            ExecutorService taskParallelExecutor,
            String secretAccessKey,
            String accessKeyId,
            String endpoint,
            String prefix,
            Region region,
            String bucket) {
        this.enableSignerV4Requests = enableSignerV4Requests;
        this.enforcePathAccessStyle = enforcePathAccessStyle;
        this.taskParallelExecutor = taskParallelExecutor;
        this.defaultBucketAcl = defaultBucketAcl;
        this.defaultObjectAcl = defaultObjectAcl;
        this.secretAccessKey = secretAccessKey;
        this.accessKeyId = accessKeyId;
        this.endpoint = endpoint;
        this.prefix = prefix;
        this.region = region;
        this.bucket = bucket;
    }

    public static Builder builder() {
        return new Builder();
    }

    public BucketCannedACL defaultBucketAcl() {
        return defaultBucketAcl;
    }

    public ObjectCannedACL defaultObjectAcl() {
        return defaultObjectAcl;
    }

    public boolean enforcePathAccessStyle() {
        return enforcePathAccessStyle;
    }

    public boolean enableSignerV4Requests() {
        return enableSignerV4Requests;
    }

    public ExecutorService taskParallelExecutor() {
        return taskParallelExecutor;
    }

    public String secretAccessKey() {
        return secretAccessKey;
    }

    public String accessKeyId() {
        return accessKeyId;
    }

    public String bucket() {
        return bucket;
    }

    public String endpoint() {
        return endpoint;
    }

    public String prefix() {
        return prefix;
    }

    public Region region() {
        return region;
    }

    public static class Builder {
        private ExecutorService taskParallelExecutor = Executors.newFixedThreadPool(16);
        private ObjectCannedACL defaultObjectAcl = ObjectCannedACL.BUCKET_OWNER_READ;
        private BucketCannedACL defaultBucketAcl = BucketCannedACL.PRIVATE;
        private boolean enableSignerV4Requests = false;
        private boolean enforcePathAccessStyle = false;
        private String secretAccessKey = null;
        private String accessKeyId = null;
        private String endpoint = null;
        private String prefix = null;
        private Region region = Region.US_EAST_1;
        private String bucket = null;

        public Builder withDefaultObjectAcl(ObjectCannedACL acl) {
            defaultObjectAcl = acl;
            return this;
        }

        public Builder withDefaultBucketAcl(BucketCannedACL acl) {
            defaultBucketAcl = acl;
            return this;
        }

        public Builder withEnabledSignerV4Requests() {
            return withEnabledSignerV4Requests(true);
        }

        public Builder withEnabledSignerV4Requests(boolean value) {
            enableSignerV4Requests = value;
            return this;
        }

        public Builder withEnforcedPathAccessStyle() {
            return withEnforcedPathAccessStyle(true);
        }

        public Builder withEnforcedPathAccessStyle(boolean value) {
            enforcePathAccessStyle = value;
            return this;
        }

        public Builder withTaskParallelExecutor(ExecutorService executor) {
            taskParallelExecutor = executor;
            return this;
        }

        public Builder withSecretAccessKey(String value) {
            secretAccessKey = value;
            return this;
        }

        public Builder withSecretAccessKeyFromEnv(@Nullable String envName) {
            return withSecretAccessKey(System.getenv(envName != null ? envName : "AWS_SECRET_ACCESS_KEY"));
        }

        public Builder withSecretAccessKeyFromProperties(@Nullable String propName) {
            return withSecretAccessKey(System.getProperty(propName != null ? propName : "aws.secret-access-key"));
        }

        public Builder withAccessKeyId(String value) {
            accessKeyId = value;
            return this;
        }

        public Builder withAccessKeyIdFromEnv(@Nullable String envName) {
            return withAccessKeyId(System.getenv(envName == null ? "AWS_ACCESS_KEY_ID" : envName));
        }

        public Builder withAccessKeyIdFromProperties(@Nullable String propName) {
            return withAccessKeyId(System.getProperty(propName != null ? propName : "aws.access-key-id"));
        }

        public Builder withEndpoint(URI uri) {
            return withEndpoint(uri.toString());
        }

        public Builder withEndpoint(String endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        public Builder withPrefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        public Builder withRegion(Region region) {
            this.region = region;
            return this;
        }

        public Builder withBucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        public AmazonS3StorageConfig build() {
            Objects.requireNonNull(secretAccessKey, "Missing secretAccessKey to connect to S3");
            Objects.requireNonNull(accessKeyId, "Missing accessKeyId to connect to S3");
            Objects.requireNonNull(bucket, "Unable to determine which bucket to use!");

            return new AmazonS3StorageConfig(
                    defaultObjectAcl,
                    defaultBucketAcl,
                    enableSignerV4Requests,
                    enforcePathAccessStyle,
                    taskParallelExecutor,
                    secretAccessKey,
                    accessKeyId,
                    endpoint,
                    prefix,
                    region,
                    bucket);
        }
    }
}
