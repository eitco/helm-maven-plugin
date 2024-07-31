/*
 * Copyright (c) 2021 EITCO GmbH
 * All rights reserved.
 *
 * Created on 06.05.2021
 *
 */
package de.eitco.commons.helm.maven.plugin;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.google.common.base.Strings;
import de.eitco.commons.helm.maven.plugin.repository.index.HelmRepositoryIndex;
import de.eitco.commons.helm.maven.plugin.repository.index.HelmRepositoryInfo;
import de.eitco.commons.helm.maven.plugin.repository.index.IndexEntry;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.DependencyResolutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectDependenciesResolver;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResolver;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResolverException;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.authorization.AuthorizationException;
import org.eclipse.aether.RepositorySystemSession;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This goal downloads the helm dependencies configured to the {@code charts} directory.
 */
@Mojo(name = "dependency-update", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class DownloadDependenciesMojo extends AbstractHelmWagonMojo {

    /**
     * This parameter specifies a list of helm dependencies. These dependencies identify their remote
     * location by a server id, referring to the keys of the map {@link AbstractHelmWagonMojo#helmRepositories}
     */
    @Parameter
    private List<HelmDependency> helmDependencies = new ArrayList<>();

    private List<HelmDependencyDeclaration> dependencyDeclarations = new ArrayList<>();

    @Parameter(defaultValue = "${repositorySystemSession}", readonly = true)
    private RepositorySystemSession repoSession;

    @Parameter(defaultValue = "${project.remoteArtifactRepositories}", readonly = true, required = true)
    private List<ArtifactRepository> remoteRepositories;

    @Component
    private ProjectDependenciesResolver projectDependenciesResolver;

    @Component
    private ArtifactResolver artifactResolver;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        if (skip) {

            getLog().info("Skip execution.");
            return;
        }

        try {

            File chartDirectory = getChartDirectory();
            File dependencyDirectory = new File(chartDirectory, "charts");

            FileUtils.forceMkdir(dependencyDirectory);

            YAMLMapper mapper = new YAMLMapper();

            HashMap<String, HelmRepositoryInfo> helmRepositoryInfoCache = new HashMap<>();

            for (HelmDependency helmDependency : helmDependencies) {

                String repositoryId = !Strings.isNullOrEmpty(helmDependency.getRepositoryId()) ? helmDependency.getRepositoryId() : defaultRepository;

                /*
                 * get helm repository 'index.yaml', download if not already cached
                 */
                HelmRepositoryInfo helmRepositoryInfo = helmRepositoryInfoCache.get(repositoryId);
                if (helmRepositoryInfo == null) {
                    try (ClosableWagon wagon = newWagon(repositoryId)) {
                        final File helmRepoIndexTmpFile = File.createTempFile("helmRepoIndex", ".yaml");
                        wagon.get(HelmRepositoryInfo.INDEX_YAML_RESOURCE_NAME, helmRepoIndexTmpFile);

                        HelmRepositoryIndex helmRepositoryIndex = mapper.readValue(helmRepoIndexTmpFile, HelmRepositoryIndex.class);
                        helmRepositoryInfo = new HelmRepositoryInfo(repositoryId, wagon.getTargetUrl(), helmRepositoryIndex);
                        helmRepositoryInfoCache.put(repositoryId, helmRepositoryInfo);

                    } catch (WagonDisconnectionException e) {

                        getLog().debug("Error disconnecting wagon - ignored", e);

                    } catch (AuthorizationException | TransferFailedException | ResourceDoesNotExistException e) {

                        // maybe fall back to maven dependency resolution here if reactorArtifact != null

                        throw new MojoExecutionException("Error handling resource", e);

                    } catch (JsonParseException | JsonMappingException e) {

                        throw new MojoExecutionException("Invalid index.yaml file in repository " + repositoryId, e);
                    }
                }

                /*
                 * select the download URL & create the Wagon for fetching the dependent helm chart archive
                 */
                final String helmDependencyChartName = helmDependency.getName();
                final List<IndexEntry> indexEntriesForChartName = helmRepositoryInfo.findIndexEntriesByChartName(helmDependencyChartName);
                // use latest version if no version specified
                String finalVersion = StringUtils.isBlank(helmDependency.getVersion()) ? helmRepositoryInfo.findLatestVersion(indexEntriesForChartName) : helmDependency.getVersion();
                final List<String> dependencyDownloadUrls = helmRepositoryInfo.resolveReferenceUrls(helmRepositoryInfo.findUrlsByVersion(indexEntriesForChartName, finalVersion));
                if (dependencyDownloadUrls == null || dependencyDownloadUrls.isEmpty()) {
                    throw new IllegalArgumentException("Could not find download URL for Helm chart with name " + helmDependencyChartName);
                }
                if (dependencyDownloadUrls.size() > 1) {
                    getLog().warn("Found multiple download URL's for Helm chart with name " + helmDependencyChartName + " - Will only try a single URL preferably a URL on the server that hosts the repository's 'index.yaml'! - Candidate dependencyDownloadUrls: " + StringUtils.join(dependencyDownloadUrls, " "));
                }

                String dependencyFileName = helmDependencyChartName + "-" + finalVersion + HELM_PACKAGE_SUFFIX;
                String dependencyOriginResourceName = helmRepositoryInfo.findOriginRelativeResource(dependencyDownloadUrls);

                ClosableWagon closableWagon;
                if (dependencyOriginResourceName != null) {

                    getLog().info("Found " + dependencyDownloadUrls.size() + " download URL's for Helm chart with name " + helmDependencyChartName + " - Will try to download this resource from origin server: " + dependencyOriginResourceName);
                    closableWagon = newWagon(helmRepositoryInfo.getRepositoryId());

                } else {

                    final URL dependencyDownloadUrl = new URL(dependencyDownloadUrls.get(0));
                    getLog().info("Found " + dependencyDownloadUrls.size() + " download URL's for Helm chart with name " + helmDependencyChartName + " - Selected download URL: " + dependencyDownloadUrl);

                    final URL dependencyDownloadBaseUrl = new URL(dependencyDownloadUrl.getProtocol(), dependencyDownloadUrl.getHost(), dependencyDownloadUrl.getPort(), "/");

                    dependencyOriginResourceName = dependencyDownloadUrl.getFile();
                    closableWagon = newChartDownloadWagon(helmRepositoryInfo.getRepositoryId(), dependencyDownloadBaseUrl);
                }

                /*
                 * download the dependent helm chart archive
                 */
                try (ClosableWagon wagon = closableWagon) {

                    downLoadFromHelmRepository(wagon, dependencyDirectory, dependencyFileName, dependencyOriginResourceName);

                    dependencyDeclarations.add(helmDependency.toDeclaration(helmRepositoryInfo.getRepositoryBaseUrl(), finalVersion));

                } catch (WagonDisconnectionException e) {

                    getLog().debug("Error disconnecting wagon - ignored", e);

                } catch (AuthorizationException | TransferFailedException | ResourceDoesNotExistException e) {

                    // maybe fall back to maven dependency resolution here if reactorArtifact != null

                    throw new MojoExecutionException("Error handling resource", e);
                }
            }

            HelmDependencyMetaDataReader metaDataReader = new HelmDependencyMetaDataReader(
                session,
                remoteRepositories,
                artifactResolver,
                project,
                repoSession,
                projectDependenciesResolver,
                mapper
            );



            metaDataReader.forEachDependency((dependency, dependencyChart) -> {

                File dependencyFile = dependency.getArtifact().getFile();

                File dependencyChartTargetFile = new File(
                    dependencyDirectory,
                    dependencyChart.getName() + "-" + dependencyChart.getVersion() + HELM_PACKAGE_SUFFIX
                );

                Files.copy(dependencyFile.toPath(), dependencyChartTargetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                dependencyDeclarations.add(new HelmDependencyDeclaration(dependencyChart.getName(), null, dependencyChart.getVersion(), null));
            });

            Files.write(getChartDescriptor().toPath(), mapper.writeValueAsBytes(getChart()), StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        } catch (IOException | DependencyResolutionException | ArtifactResolverException e) {

            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    private void appendProperty(StringBuilder builder, String propertyName, String propertyValue) {

        builder.append(propertyName).append(": ").append(propertyValue).append("\n");
    }

    @Nullable
    private Artifact searchArtifactInReactor(@Nullable MavenArtifact mavenArtifact) {

        if (mavenArtifact == null) {

            return null;
        }

        List<Artifact> attachedArtifacts = session.getProjects().stream().filter(reactorProject ->
            reactorProject.getArtifactId().equals(mavenArtifact.getArtifactId()) &&
                reactorProject.getVersion().equals(mavenArtifact.getVersion()) &&
                reactorProject.getGroupId().equals(mavenArtifact.getGroupId())
        ).map(MavenProject::getAttachedArtifacts).reduce(new ArrayList<>(), (x, y) -> {
            x.addAll(y);
            return x;
        });

        return attachedArtifacts.stream()
            .filter(artifact -> nullOrEqual(artifact.getClassifier(), mavenArtifact.getClassifier()) && artifact.getType().equals(mavenArtifact.getType())).findFirst().orElse(null);
    }

    private boolean nullOrEqual(String classifier1, String classifier2) {

        if (Strings.isNullOrEmpty(classifier1) && Strings.isNullOrEmpty(classifier2)) {

            return true;
        }

        if (classifier1 == null) {

            return false;
        }

        return classifier1.equals(classifier2);
    }

    private void downLoadFromHelmRepository(ClosableWagon wagon, File chartDirectory, String dependencyFileName, String dependencyResourceName) throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {

        File chartFile = new File(chartDirectory, dependencyFileName);

        wagon.get(dependencyResourceName, chartFile);
    }

}
