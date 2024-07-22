/*
 * Copyright (c) 2022 EITCO GmbH
 * All rights reserved.
 *
 * Created on 04.03.2022
 *
 */
package de.eitco.commons.helm.maven.plugin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectDependenciesResolver;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResolver;
import org.eclipse.aether.RepositorySystemSession;
import de.eitco.cicd.maven.plugin.utility.JsonDependencyMetaDataReader;

import java.util.List;

public class HelmDependencyMetaDataReader extends JsonDependencyMetaDataReader<Chart> {

    public HelmDependencyMetaDataReader(
        MavenSession session,
        List<ArtifactRepository> remoteRepositories,
        ArtifactResolver artifactResolver,
        MavenProject project,
        RepositorySystemSession repoSession,
        ProjectDependenciesResolver projectDependenciesResolver,
        ObjectMapper objectMapper
    ) {
        super(
            session,
            remoteRepositories,
            artifactResolver,
            project,
            repoSession,
            projectDependenciesResolver,
            AbstractHelmMojo.HELM_CHART_EXTENSION,
            AbstractHelmMojo.HELM_ARTIFACT_EXTENSION,
            Chart.class,
            objectMapper
        );
    }
}
