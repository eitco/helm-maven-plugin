/*
 * Copyright (c) 2021 EITCO GmbH
 * All rights reserved.
 *
 * Created on 18.05.2021
 *
 */
package de.eitco.commons.helm.maven.plugin;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HelmDependencyDeclaration {

    @NotNull
    private final String name;

    @Nullable
    private final String alias;

    @NotNull
    private final String version;

    @Nullable
    private final String repository;

    public HelmDependencyDeclaration(@NotNull String name, @Nullable String alias, @NotNull String version, @Nullable String repository) {
        this.name = name;
        this.alias = alias;
        this.version = version;
        this.repository = repository;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @Nullable
    public String getAlias() {
        return alias;
    }

    @NotNull
    public String getVersion() {
        return version;
    }

    @Nullable
    public String getRepository() {
        return repository;
    }

    public void writeDeclaration(@NotNull StringBuilder builder) {

        builder.append("  - name: \"").append(name.replaceAll("\"", "\\\"")).append("\n")
            .append("    version: \"").append(version.replaceAll("\"", "\\\"")).append("\n");

        if (repository != null) {

            builder.append("    repository: ").append(repository).append("\n");
        }

        if (alias != null) {

            builder.append("    alias: \"").append(alias.replaceAll("\"", "\\\"")).append("\n");
        }

        builder.append("\n");
    }

}
