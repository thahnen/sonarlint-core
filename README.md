# SonarLint Core 9.8.1 (minimized version)

This is build upon [SonarSource/sonarlint-core](https://github.com/SonarSource/sonarlint-core) version 9.8. Minimized in
order to only include the standalone SonarLint engine (`StandaloneSonarLintEngineImpl`) and its dependencies and as many
tests as necessary.

Files might have been modified or new files were added.

## Building the library

To build this customized library, run the following command (append `-DskipTests` to skip the unit tests):
> mvn clean verify

To install it locally, run the following command (append `-DskipTests` to skip the unit tests):
> mvn clean install

## Copyright

Copyright 2016-2023 SonarSource.

Licensed under the [GNU Lesser General Public License, Version 3.0](http://www.gnu.org/licenses/lgpl.txt)
