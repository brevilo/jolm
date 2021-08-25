[![Build status](https://github.com/brevilo/jolm/actions/workflows/build.yml/badge.svg)](https://github.com/brevilo/jolm/actions/workflows/build.yml)

# jOlm - Olm bindings for Java

jOlm provides Java bindings to the C-API of the [Olm cryptographic library](https://gitlab.matrix.org/matrix-org/olm) used by [Matrix](https://matrix.org/).

## Requirements

* jOlm uses Java Native Access (JNA) and requires Olm 3.2 (or higher) to be installed
* How to get Olm:
  * Use your favorite package manager to install it (Linux, [macOS](https://brew.sh/))
  * Download the [latest version](https://gitlab.matrix.org/matrix-org/olm/-/releases) and [build it](https://gitlab.matrix.org/matrix-org/olm#building) yourself

## Installation

Maven: `mvn install`

## Status

- [x] Implement upstream API
- [x] Preliminary documentation (Javadoc)
- [x] Run basic tests
- [x] Set up CI and release process
- [x] Provide packages for Maven
- [ ] Implement test suite
- [ ] Security audit
- [ ] Memory management audit
- [ ] Consider Gradle to integrate `libolm`

## Contribute

PRs welcome 👍
