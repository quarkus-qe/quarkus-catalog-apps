package io.quarkus.qe.model.requests;

import java.util.List;

import io.quarkus.qe.model.QuarkusExtension;

public class RepositoryQueryRequest {
    private String repoUrl;
    private String branch;
    private String relativePath;
    private String quarkusVersion;
    private List<QuarkusExtension> extensions;
    private List<String> labels;

    public String getRepoUrl() {
        return repoUrl;
    }

    public void setRepoUrl(String repoUrl) {
        this.repoUrl = repoUrl;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }

    public String getQuarkusVersion() {
        return quarkusVersion;
    }

    public void setQuarkusVersion(String quarkusVersion) {
        this.quarkusVersion = quarkusVersion;
    }

    public List<QuarkusExtension> getExtensions() {
        return extensions;
    }

    public void setExtensions(List<QuarkusExtension> extensions) {
        this.extensions = extensions;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }
}
