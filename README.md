[![Build status](https://img.shields.io/github/workflow/status/brevilo/jolm/Build)](https://github.com/brevilo/jolm/actions/workflows/build.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.brevilo/jolm)](https://search.maven.org/artifact/io.github.brevilo/jolm)

# jOlm - Olm bindings for Java

jOlm provides Java bindings to the C-API of the [Olm cryptographic library](https://gitlab.matrix.org/matrix-org/olm) used by [Matrix](https://matrix.org/).

## Requirements

* Java SE 11
* Olm 3.2 or higher

jOlm uses Java Native Access (JNA) and currently requires Olm 3.2 (or higher) to be installed. How to get Olm:

* Use your favorite package manager to install it (Linux, [macOS](https://brew.sh/))
* Download the [latest version](https://gitlab.matrix.org/matrix-org/olm/-/releases) and [build it](https://gitlab.matrix.org/matrix-org/olm#building) yourself

## Maven integration

```
<dependency>
  <groupId>io.github.brevilo</groupId>
  <artifactId>jolm</artifactId>
  <version>1.0.0</version>
</dependency>
```

## Build manually

Using maven: `mvn install`

## Status

- [x] Implement upstream API
- [x] Preliminary documentation (Javadoc)
- [x] Run basic tests
- [x] Set up CI and release process
- [x] Provide packages for Maven
- [ ] Implement test suite
- [ ] Security audit
- [ ] Memory management audit
- [ ] Consider Gradle to integrate Olm

## Contribute

PRs welcome 👍
