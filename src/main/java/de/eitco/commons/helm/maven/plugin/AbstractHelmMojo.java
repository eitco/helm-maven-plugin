/*
 * Copyright (c) 2021 EITCO GmbH
 * All rights reserved.
 *
 * Created on 20.05.2021
 *
 */
package de.eitco.commons.helm.maven.plugin;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.regex.Pattern;

public abstract class AbstractHelmMojo extends AbstractMojo {

    public static final String HELM_PACKAGE_SUFFIX = ".tgz";
    public static final String HELM_ARTIFACT_EXTENSION = "helm";
    public static final String HELM_CHART_EXTENSION = "chart";
    public static final String CHART_FILE_NAME = "Chart.yaml";

    public static final Pattern DISALLOWED_VERSION_CHARACTERS = Pattern.compile("[^A-Za-z0-9-.+]");

    @Parameter(defaultValue = "${project.artifactId}", property = "helm.chart.name")
    private String chartName;

    @Parameter(defaultValue = "${project.version}", property = "helm.chart.version")
    private String chartVersion;

    @Parameter(defaultValue = "${project.version}", property = "helm.app.version")
    private String appVersion;

    @Parameter(defaultValue = "application", property = "helm.chart.type")
    private String chartType;

    @Parameter(defaultValue = "${project.description}", property = "helm.chart.description")
    private String chartDescription;

    @Parameter(readonly = true, defaultValue = "${project.build.directory}/helm")
    protected File buildDirectory;

    @Parameter(readonly = true, defaultValue = "${project.build.directory}")
    protected File targetDirectory;

    @Parameter(defaultValue = "src/main/helm", property = "helm.source.directory")
    protected File sourceDirectory;

    @Parameter(defaultValue = "${project}", readonly = true)
    protected MavenProject project;

    @Parameter(defaultValue = "${session}", readonly = true)
    protected MavenSession session;

    private Chart chart = null;

    public Chart getChart() {

        if (chart == null) {

            chart = new Chart();

            chart.setName(chartName);

            chart.setVersion(DISALLOWED_VERSION_CHARACTERS.matcher(chartVersion).replaceAll("-"));

            chart.setAppVersion(appVersion);

            chart.setType(chartType);

            chart.setDescription(chartDescription);
        }

        return chart;
    }

    @NotNull
    public String getChartFileName(Chart chart) {
        return chart.getName() + "-" + chart.getVersion() + HELM_PACKAGE_SUFFIX;
    }

    public String getChartFileName() {

        return getChartFileName(getChart());
    }

    public File getChartDirectory(Chart chart) {

        return new File(buildDirectory, chart.getName());
    }

    public File getChartDirectory() {

        return getChartDirectory(getChart());
    }

    public File getChartDescriptor() {

        return new File(getChartDirectory(), CHART_FILE_NAME);
    }
}
