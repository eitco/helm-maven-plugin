/*
 * Copyright (c) 2021 EITCO GmbH
 * All rights reserved.
 *
 * Created on 06.05.2021
 *
 */
package de.eitco.commons.helm.maven.plugin;

import com.google.common.base.Strings;
import org.apache.maven.model.RepositoryBase;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.apache.maven.wagon.Wagon;
import org.codehaus.mojo.wagon.shared.WagonFactory;
import org.codehaus.mojo.wagon.shared.WagonFileSet;
import org.codehaus.mojo.wagon.shared.WagonUtils;

import java.net.URL;
import java.util.Map;


public abstract class AbstractHelmWagonMojo extends AbstractHelmMojo {

    @Component
    protected WagonFactory wagonFactory;

    /**
     * The current user system settings for use in Maven.
     */
    @Parameter(defaultValue = "${settings}", readonly = true)
    protected Settings settings;

    /**
     * Internal Maven's project.
     */
    @Parameter(defaultValue = "${project}", readonly = true)
    protected MavenProject project;

    /**
     * When <code>true</code>, skip the execution.
     *
     * @since 2.0.0
     */
    @Parameter(property = "wagon.skip")
    protected boolean skip = false;

    @Parameter
    protected Map<String, String> helmRepositories = Map.of("charts.helm.sh", "https://charts.helm.sh/stable");

    @Parameter(defaultValue = "charts.helm.sh")
    protected String defaultRepository;

    protected Wagon createWagon(String id, String url)
        throws MojoExecutionException {
        try {
            return wagonFactory.create(url, id, settings);
        } catch (Exception e) {
            throw new MojoExecutionException("Unable to create a Wagon instance for " + url, e);
        }

    }

    protected WagonFileSet getWagonFileSet(String fromDir, String includes, String excludes, boolean caseSensitive,
        String toDir) {
        return WagonUtils.getWagonFileSet(fromDir, includes, excludes, caseSensitive, toDir);
    }

    protected ClosableWagon newWagon(String id) throws MojoExecutionException {

        if (Strings.isNullOrEmpty(id)) {

            id = defaultRepository;
        }

        String url = getRepositoryUrl(id);

        if (url == null) {

            throw new MojoExecutionException("no url defined for repository " + id);
        }

        return new ClosableWagon(createWagon(id, url), url);
    }

    protected ClosableWagon newChartDownloadWagon(String repositoryServerId, URL dependencyDownloadServerBaseUrl)
        throws MojoExecutionException {

        String id = repositoryServerId + "--" + dependencyDownloadServerBaseUrl.getHost().replace(".", "-");

        final String repositoryUrlForServerId = getRepositoryUrl(id);
        if( repositoryUrlForServerId != null  && !repositoryUrlForServerId.equals(dependencyDownloadServerBaseUrl)) {
            getLog().warn("Url mismatch for wagon serverId '" + id + "' configured URL: '" + repositoryUrlForServerId + "' assumed/expected URL: '" + dependencyDownloadServerBaseUrl + "'.");
        }

        if( repositoryUrlForServerId == null ) {
            getLog().info("No configuration found for (synthetic) wagon serverId '" + id + "' assumed/expected URL: '" + dependencyDownloadServerBaseUrl + "'.");
        }

        final String chartDownloadUrlString = dependencyDownloadServerBaseUrl.toExternalForm();

        return new ClosableWagon(createWagon(id, chartDownloadUrlString), chartDownloadUrlString);
    }



    protected String getRepositoryUrl(String repositoryId) {

        String result = helmRepositories.get(repositoryId);

        if (result != null) {

            return result;
        }

        return project.getRepositories().stream()
            .filter(repository -> repository.getId().equals(repositoryId)).map(RepositoryBase::getUrl).findFirst()
            .orElse(null);
    }
}
