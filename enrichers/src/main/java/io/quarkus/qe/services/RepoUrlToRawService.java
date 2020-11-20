package io.quarkus.qe.services;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import io.quarkus.qe.model.Repository;

public abstract class RepoUrlToRawService {

    private static final String GIT_EXTENSION = ".git";
    private static final String DEFAULT_BRANCH = "master";

    public abstract boolean isFor(Repository repository);

    public abstract String getRawUrl(Repository repository);

    protected String getBranchOrDefault(Repository repository) {
        return Optional.ofNullable(repository.getBranch()).orElse(DEFAULT_BRANCH);
    }

    protected static final String removeGitExtension(String str) {
        if (str.endsWith(GIT_EXTENSION)) {
            return StringUtils.removeEnd(str, GIT_EXTENSION);
        }

        return str;
    }
}
