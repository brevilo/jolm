# jOlm - Olm bindings for Java

These bindings for Java provide access to the C-API of the [Olm cryptographic library](https://gitlab.matrix.org/matrix-org/olm) used by [Matrix](https://matrix.org/).

## Requirements

* These bindings use Java Native Access (JNA) and expect `libolm` to be installed
* Ways to get `libolm`:
  * Use your favorite package manager to install it (Linux, [macOS](https://brew.sh/))
  * Download the [latest version](https://gitlab.matrix.org/matrix-org/olm/-/releases) and [build it](https://gitlab.matrix.org/matrix-org/olm#building) yourself

## Installation

Maven: `mvn install`

## Status

- [x] Implement upstream API
- [x] Preliminary documentation (Javadoc)
- [x] Run basic tests
- [ ] Set up CI and release process
- [ ] Provide packages for Maven
- [ ] Implement test suite
- [ ] Security audit
- [ ] Memory management audit
- [ ] Consider Gradle to integrate `libolm`

## Contribute

PRs welcome 👍
