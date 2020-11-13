package io.quarkus.qe.model.requests;

public class NewRepositoryRequest {
    private String repoUrl;

    public NewRepositoryRequest() {

    }

    public NewRepositoryRequest(String repoUrl) {
        this.repoUrl = repoUrl;
    }

    public String getRepoUrl() {
        return repoUrl;
    }

    public void setRepoUrl(String repoUrl) {
        this.repoUrl = repoUrl;
    }
}
