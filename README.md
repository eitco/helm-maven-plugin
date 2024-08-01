
[![License](https://img.shields.io/github/license/eitco/helm-maven-plugin.svg?style=for-the-badge)](https://opensource.org/license/mit)
[![Build status](https://img.shields.io/github/actions/workflow/status/eitco/helm-maven-plugin/deploy.yaml?branch=main&style=for-the-badge&logo=github)](https://github.com/eitco/helm-maven-plugin/actions/workflows/deploy.yaml)
[![Maven Central Version](https://img.shields.io/maven-central/v/de.eitco.cicd/helm-maven-plugin?style=for-the-badge&logo=apachemaven)](https://central.sonatype.com/artifact/de.eitco.cicd/helm-maven-plugin)

# helm maven plugin

This maven plugin provides a build lifecycle for building and deploying helm charts. It is built on top of
[Device Insights helm maven plugin](https://github.com/deviceinsight/helm-maven-plugin).


# usage

In order to make the `helm` build lifecycle available add this plugin to your build activating extensions:

```xml
...
<build>
    <plugins>
        <plugin>
            <groupId>de.eitco.cicd.helm</groupId>
            <artifactId>helm-maven-plugin</artifactId>
            <version>4.0.1</version>
            <extensions>true</extensions>
        </plugin>
    </plugins>
</build>
```

Now you can activate the `helm` build lifecycle by specifying the packaging `helm`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>your.group.id</groupId>
    <artifactId>your-artifact-id</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <packaging>helm</packaging>
...
</project>
```

One required parameter needs to be set, though - the version of helm to use:

```xml
...
<build>
    <plugins>
        <plugin>
            <groupId>de.eitco.cicd.helm</groupId>
            <artifactId>helm-maven-plugin</artifactId>
            <version>4.0.1</version>
            <extensions>true</extensions>
            <configuration>
                <helmVersion>3.15.3</helmVersion>
            </configuration>
        </plugin>
    </plugins>
</build>
```

Now you can add your files in `src/main/helm`. 

* a complete reference about the goals and paramters of this plugin can be found [here](https://eitco.github.io/helm-maven-plugin/plugin-info.html).
* Refer to the [integration test](./src/it/simple) for a simple example.

> 📘 Note that there is no `Chart.yaml` file in this example. The plugin will generate this file by itself from values
> configured in the `pom.xml`


