package de.eitco.commons.helm.maven.plugin.repository.index;


import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.versioning.ComparableVersion;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Inspired by
 * https://github.com/spinnaker/clouddriver/blob/master/clouddriver-artifacts/src/main/java/com/netflix/spinnaker/clouddriver/artifacts/helm/IndexParser.java
 * https://github.com/spinnaker/spinnaker/issues/4078
 */
public class HelmRepositoryInfo {
    public static String INDEX_YAML_RESOURCE_NAME = "/index.yaml";

    private final String repositoryId;
    private final String repositoryBaseUrl;
    private final HelmRepositoryIndex helmRepositoryIndex;

    public HelmRepositoryInfo(String repositoryId, String repositoryBaseUrl, HelmRepositoryIndex helmRepositoryIndex) {
        this.repositoryId = repositoryId;
        this.repositoryBaseUrl = repositoryBaseUrl;
        this.helmRepositoryIndex = helmRepositoryIndex;
    }

    public String getRepositoryId() {
        return repositoryId;
    }

    public String getRepositoryBaseUrl() {
        return repositoryBaseUrl;
    }

    public String findOriginRelativeResource(List<String> dependencyDownloadUrls) {
        for (String dependencyDownloadUrl : dependencyDownloadUrls) {
            if( dependencyDownloadUrl.startsWith(repositoryBaseUrl) ) {
                final String relativeResourceName = dependencyDownloadUrl.substring(repositoryBaseUrl.length());
                if( relativeResourceName.startsWith("/") || repositoryBaseUrl.endsWith("/") ) {
                    return relativeResourceName;
                }
            }
        }
        return null;
    }

    public List<String> findUrls(String name, String version) throws IOException {
        final List<IndexEntry> indexEntriesForChartName = findIndexEntriesByChartName(name);

        String validVersion = StringUtils.isBlank(version) ? findLatestVersion(indexEntriesForChartName) : version;

        return resolveReferenceUrls(findUrlsByVersion(indexEntriesForChartName, validVersion));
    }


    public List<String> findNames()  {
        return new ArrayList<>(helmRepositoryIndex.getEntries().keySet());
    }

    public List<String> findVersions(String name) throws IOException {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Helm chart name should not be empty");
        }
        List<IndexEntry> configs = findIndexEntriesByChartName(name);
        List<String> versions = new ArrayList<>();
        configs.forEach(e -> versions.add(e.getVersion()));
        return versions;
    }

    public List<String> findUrls(InputStream in, String name, String version) throws IOException {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Helm chart name should not be empty");
        }
        List<IndexEntry> configs = findIndexEntriesByChartName(name);
        String validVersion = StringUtils.isBlank(version) ? findLatestVersion(configs) : version;
        return resolveReferenceUrls(findUrlsByVersion(configs, validVersion));
    }

    public List<String> resolveReferenceUrls(List<String> urls) {
        return urls.stream().map(this::resolveReferenceUrl).collect(Collectors.toList());
    }

    private String resolveReferenceUrl(String ref) {
        String resolvedRef = ref;
        String base = repositoryBaseUrl;
        if (!base.endsWith("/")) {
            base = base.concat("/");
        }
        try {
            URL baseUrl = new URL(base);
            URL resolvedUrl = new URL(baseUrl, ref);
            resolvedRef = resolvedUrl.toExternalForm();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Failed to resolve reference url:" + ref, e);
        }
        return resolvedRef;
    }

    public List<String> findUrlsByVersion(List<IndexEntry> configs, String version) {
        List<String> urls = new ArrayList<>();
        configs.forEach(
            e -> {
                if (e.getVersion().equals(version)) {
                    urls.addAll(e.getUrls());
                }
            });
        if (urls.isEmpty()) {
            throw new IllegalArgumentException(
                "Could not find correct Helm chart entry with artifact version " + version);
        }
        return urls;
    }

    public String findLatestVersion(List<IndexEntry> indexEntries) {
        return indexEntries.stream()
            .map(c -> new ComparableVersion(c.getVersion()))
            .max(ComparableVersion::compareTo)
            .orElseGet(() -> new ComparableVersion(""))
            .toString();
    }

    public List<IndexEntry> findIndexEntriesByChartName(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Helm chart name should not be empty");
        }

        final List<IndexEntry> indexEntriesForChartName = helmRepositoryIndex.getEntries().get(name);
        if (indexEntriesForChartName == null || indexEntriesForChartName.isEmpty()) {
            throw new IllegalArgumentException("Could not find Helm chart with name " + name);
        }

        return indexEntriesForChartName;
    }

}



