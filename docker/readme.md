# Docker

Build the deployable jar before building the image:

```shell
mvn -B -pl challenger -am package
```

Build the image:

```shell
docker build -t eviltester/apichallenges -f docker/apichallenges/Dockerfile .
```

Run the image:

```shell
docker run --rm -p 4567:4567 eviltester/apichallenges
```

Run with persistent local challenger sessions:

```shell
mkdir challengersessions
docker run --rm -p 4567:4567 \
  -v "$(pwd)/challengersessions:/opt/app/challengersessions" \
  eviltester/apichallenges
```

The GitHub Actions Docker workflow builds and smoke-tests the image. It pushes
to Docker Hub when `DOCKERHUB_USERNAME` and `DOCKERHUB_TOKEN` repository secrets
are configured.
