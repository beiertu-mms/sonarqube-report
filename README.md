# Cli app template
![build on master][ci badge]
![License][license badge]

My simple template to create a command-line interface (cli) with Kotlin.

## Usage
When creating a new repository on GitHub, just choose this as a template.  
After the new repository is created, the clean up workflow will run and
it will replace/remove/update the repository using the new repository's name.

## Build requirements
- JDK 17
- Gradle >= 7 (optional)

## Installation
**Install**:
The repository contains a simple Makefile, which can be used to build
and copy the built jar and script to the user's bin folder (default: `$HOME/.local/bin`).

```bash
make install
```

**Uninstall**:
The same Makefile also contains an uninstall step,
which will remove the buildt jar and script from the user's bin folder.

```bash
make uninstall
```

## License
Distributed under the MIT License. See [LICENSE][license] for more information.

[ci badge]: https://img.shields.io/github/workflow/status/beiertu-mms/cli-app-template/CI/master
[license badge]: https://img.shields.io/github/license/beiertu-mms/cli-app-template
[license]: https://github.com/beiertu-mms/cli-app-template/blob/master/LICENSE
