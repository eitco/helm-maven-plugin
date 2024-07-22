/*
 * Copyright (c) 2021 EITCO GmbH
 * All rights reserved.
 *
 * Created on 06.05.2021
 *
 */
package de.eitco.commons.helm.maven.plugin;

public class HelmDependency {

    private String name;
    private String version;
    private String repositoryId;
    private String alias = "";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getRepositoryId() {
        return repositoryId;
    }

    public void setRepositoryId(String repositoryId) {
        this.repositoryId = repositoryId;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public HelmDependencyDeclaration toDeclaration(String repoUrl) {

        return new HelmDependencyDeclaration(name, alias, version, repoUrl);
    }
    public HelmDependencyDeclaration toDeclaration(String repoUrl, String finalVersion) {

        return new HelmDependencyDeclaration(name, alias, finalVersion, repoUrl);
    }

}
