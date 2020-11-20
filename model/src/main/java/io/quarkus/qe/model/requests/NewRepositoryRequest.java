package io.quarkus.qe.model.requests;

import javax.validation.constraints.NotEmpty;

public class NewRepositoryRequest {
    @NotEmpty
    private String repoUrl;
    @NotEmpty
    private String branch;

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
}
