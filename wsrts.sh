#!/bin/bash

set -euxo pipefail

image_tag=fkis/wsrts:latest

docker build -t "${image_tag}" -f docker/Dockerfile .

docker run --rm -p 8080:8080 "${image_tag}"