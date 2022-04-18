# module org.noelware.remi.s3
This implements the storage trailer for using a compatible **Amazon S3** server with Remi. This supports:

- Wasabi (using `S3Provider.Wasabi`)
- MinIO (using `S3Provider.Custom` and setting the `endpoint` config value)
- Amazon S3 (using `S3Provider.Amazon`)
