#!/bin/bash

# 🧶 Remi: Library to handling files for persistent storage with Google Cloud Storage
# and Amazon S3-compatible server, made in Kotlin!
#
# Copyright 2022 Noelware <team@noelware.org>
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# Why does this exist?
# In order to support the Amazon S3 storage unit, we will use MinIO to
# use as a test S3 server for now.

set -e

echo "Now updating documentation..."
rm -rf docs
./gradlew dokkaHtmlMultiModule
echo "remi.noelware.org" >> docs/CNAME
echo "done!"
