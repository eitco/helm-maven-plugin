package de.eitco.commons.helm.maven.plugin.repository.index;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HelmRepositoryIndex {
    private Map<String, List<IndexEntry>> entries;

    public Map<String, List<IndexEntry>> getEntries() {
        return entries;
    }

    public void setEntries(Map<String, List<IndexEntry>> entries) {
        this.entries = entries;
    }
}
