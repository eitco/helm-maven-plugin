/*
 * Copyright (c) 2021 EITCO GmbH
 * All rights reserved.
 *
 * Created on 04.03.2021
 *
 */
package de.eitco.commons.helm.maven.plugin;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.wagon.WagonException;

import java.io.File;

@Mojo(name = "deploy-helm", defaultPhase = LifecyclePhase.DEPLOY)
public class DeployHelmFileMojo extends AbstractHelmWagonMojo {

    /**
     * settings.xml's server id for the URL. This is used when wagon needs extra authentication information.
     */
    @Parameter(property = "helm.distribution.server.id")
    private String distributionServerId;

    @Parameter
    @Deprecated
    private String artifactFile;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        if (skip) {
            getLog().info("Skip execution.");
            return;
        }

        try (ClosableWagon wagon = newWagon(distributionServerId)) {

            String fileName = artifactFile != null ? artifactFile : getChartFileName();

            File sourceFile = new File(targetDirectory, fileName);

            getLog().info("Uploading: " + fileName + " " + wagon.getRepository().getUrl() + "/" + fileName);
            wagon.put(sourceFile, fileName);

        } catch (WagonException e) {

            throw new MojoExecutionException("Error handling resource", e);

        } catch (WagonDisconnectionException e) {

            getLog().debug("Error disconnecting wagon - ignored", e);
        }
    }
}
