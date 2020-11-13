package io.quarkus.qe.model;

import javax.validation.constraints.NotEmpty;

public class Repository {
    private Long id;
    @NotEmpty
    private String repoUrl;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRepoUrl() {
        return repoUrl;
    }

    public void setRepoUrl(String repoUrl) {
        this.repoUrl = repoUrl;
    }
}
