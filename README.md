[![Build status](https://img.shields.io/github/workflow/status/brevilo/jolm/Build)](https://github.com/brevilo/jolm/actions/workflows/build.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.brevilo/jolm)](https://search.maven.org/artifact/io.github.brevilo/jolm)

# jOlm - Olm bindings for Java

jOlm provides Java bindings to the C-API of the [Olm cryptographic library](https://gitlab.matrix.org/matrix-org/olm) used by [Matrix](https://matrix.org/).

## Requirements

* Java SE 1.8
* Olm 3.2.8 or higher

jOlm uses Java Native Access (JNA) and currently requires Olm to be installed. How to get Olm:

* Use your favorite package manager to install it (Linux, [macOS](https://brew.sh/))
* Download the [latest version](https://gitlab.matrix.org/matrix-org/olm/-/releases) and [build it](https://gitlab.matrix.org/matrix-org/olm#building) yourself

## Maven integration

```
<dependency>
  <groupId>io.github.brevilo</groupId>
  <artifactId>jolm</artifactId>
  <version>1.1.0</version>
</dependency>
```

## Build manually

Using maven: `mvn install`

## Status

- Maturity: **stable**
- Open TODOs:
  - [ ] Security audit
  - [ ] Memory management audit

## Contribute

PRs welcome 👍
