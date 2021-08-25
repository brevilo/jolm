[![CI](https://github.com/brevilo/jolm/actions/workflows/java-ci.yml/badge.svg)](https://github.com/brevilo/jolm/actions/workflows/java-ci.yml)

# jOlm - Olm bindings for Java

The bindings for Java provide access to the C-API of the [Olm cryptographic library](https://gitlab.matrix.org/matrix-org/olm) used by [Matrix](https://matrix.org/).

## Requirements

* The bindings use Java Native Access (JNA) and require Olm 3.2 (or higher) to be installed
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
- [ ] Provide packages for Maven
- [ ] Implement test suite
- [ ] Security audit
- [ ] Memory management audit
- [ ] Consider Gradle to integrate `libolm`

## Contribute

PRs welcome 👍
