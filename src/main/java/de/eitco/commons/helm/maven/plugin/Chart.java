/*
 * Copyright (c) 2021 EITCO GmbH
 * All rights reserved.
 *
 * Created on 20.05.2021
 *
 */
package de.eitco.commons.helm.maven.plugin;

import java.util.ArrayList;
import java.util.List;

public class Chart {

    private String apiVersion = "v2";
    private String name;
    private String description = "A Helm chart for Kubernetes";
    private String type = "application";
    private String version;
    private String appVersion;

    private List<HelmDependencyDeclaration> dependencies = new ArrayList<>();

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public List<HelmDependencyDeclaration> getDependencies() {
        return dependencies;
    }
}
