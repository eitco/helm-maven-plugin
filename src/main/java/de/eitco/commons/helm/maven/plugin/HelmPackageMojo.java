/*
 * Copyright (c) 2021 EITCO GmbH
 * All rights reserved.
 *
 * Created on 19.05.2021
 *
 */
package de.eitco.commons.helm.maven.plugin;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProjectHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

@Mojo(name = "package", defaultPhase = LifecyclePhase.PACKAGE)
public class HelmPackageMojo extends AbstractHelmMojo {

    @Component
    private MavenProjectHelper projectHelper;

    @Override
    public void execute() throws MojoExecutionException {

        try {

            var proc = new ProcessBuilder(HelmBinaryHolder.INSTANCE.getBinary(), "package", getChartDirectory().getAbsolutePath(), "--destination", ".")
                .directory(targetDirectory)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start();

            proc.waitFor();

            getLog().debug("When executing " + HelmBinaryHolder.INSTANCE.getBinary() + " package got result code '" + proc.exitValue() + "'");

            new BufferedReader(new InputStreamReader(proc.getInputStream())).lines().forEach(line -> getLog().debug("Output: " + line));
            new BufferedReader(new InputStreamReader(proc.getErrorStream())).lines().forEach(line -> getLog().error("Output: " + line));

            if (proc.exitValue() != 0) {
                throw new MojoExecutionException("When executing " + HelmBinaryHolder.INSTANCE.getBinary() + " package got result code '" + proc.exitValue() + "'");
            }

            Artifact projectArtifact = project.getArtifact();

            projectArtifact.setFile(new File(targetDirectory, getChartFileName()));

            getLog().info("artifact file is " + project.getArtifact().getFile());

            projectHelper.attachArtifact(project, HELM_CHART_EXTENSION, getChartDescriptor());

        } catch (IOException | InterruptedException e) {

            throw new MojoExecutionException(e.getMessage(), e);
        }
    }
}
