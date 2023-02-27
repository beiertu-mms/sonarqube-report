# sonarqube-report
[![CI](https://github.com/beiertu-mms/cli-app-template/actions/workflows/ci.yaml/badge.svg)](./.github/workflows/ci.yaml)
[![License](https://img.shields.io/github/license/beiertu-mms/sonarqube-report)](./LICENSE)

A simple CLI to fetch SonarQube metric(s) for component(s) and write them to a Markdown file.

## Usage

Build the jar

```sh
./gradlew clean build
```

Use the jar

```sh
java -jar build/libs/sonarqube-report.jar [OPTIONS]
```

Run the jar with the `--help` option or without any options for more information.

## Build requirements
- JDK >= 17
- Gradle >= 8 (optional)

## Installation
**Install**:
The repository contains a simple Makefile, which can be used to build
and copy the built jar and script to the user's bin folder (default: `$HOME/.local/bin`).

```bash
make install
```

**Uninstall**:
The same Makefile also contains an uninstall step,
which will remove the built jar and script from the user's bin folder.

```bash
make uninstall
```

## License
Distributed under the MIT License. See [LICENSE](./LICENSE) for more information.
