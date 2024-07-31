/*
 * Copyright (c) 2021 EITCO GmbH
 * All rights reserved.
 *
 * Created on 19.05.2021
 *
 */
package de.eitco.commons.helm.maven.plugin;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.filtering.MavenFilteringException;
import org.apache.maven.shared.filtering.MavenResourcesExecution;
import org.apache.maven.shared.filtering.MavenResourcesFiltering;

import java.util.List;

/**
 * This goal generates the helm sources.
 */
@Mojo(name = "generate-helm-sources", threadSafe = true, defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class GenerateHelmSourcesMojo extends AbstractHelmMojo {


    /**
     * This parameter specifies the encoding to use.
     */
    @Parameter(defaultValue = "${project.build.sourceEncoding}")
    protected String encoding;

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject project;

    @Component(role = MavenResourcesFiltering.class, hint = "default")
    protected MavenResourcesFiltering mavenResourcesFiltering;

    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    protected MavenSession session;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        try {

            Resource resource = new Resource();

            resource.setFiltering(true);
            resource.setDirectory(sourceDirectory.getAbsolutePath());


            MavenResourcesExecution mavenResourcesExecution =
                new MavenResourcesExecution(List.of(resource), getChartDirectory(), project, encoding, List.of(), List.of(), session);

            mavenResourcesFiltering.filterResources(mavenResourcesExecution);

        } catch (MavenFilteringException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }
}
