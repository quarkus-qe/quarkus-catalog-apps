package io.quarkus.qe;

import io.quarkus.qe.model.QuarkusExtension;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.graphql.Source;

import io.quarkus.qe.exceptions.RepositoryNotFoundException;
import io.quarkus.qe.model.Repository;
import io.quarkus.qe.model.requests.RepositoryQueryRequest;
import io.quarkus.qe.services.RepositoryService;
import org.eclipse.microprofile.graphql.DefaultValue;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;

@GraphQLApi
public class SearchResource {

    @Inject
    RepositoryService repositoryService;

    @Query("repositoryById")
    @Description("Get repository by ID")
    public Repository getRepositoryByRepoUrl(@Name("id") long id) throws RepositoryNotFoundException {
        return repositoryService.findById(id);
    }

    @Query("repositories")
    @Description("Get repositories")
    public List<Repository> getAllRepositories(@Source RepositoryQueryRequest request) {
        return repositoryService.find(request);
    }

    @Query("repositoriesByExtensions")
    @Description("Get repositories using a list of extensions")
    public List<Repository> getRepositoriesByExtension(@Name("extensions") List<QuarkusExtension> extensions) {
        return repositoryService.findByExtensions(extensions);
    }

    @Query("repositoriesByExtensionsArtifactIds")
    @Description("Get repositories using a list of extensions artifacts ids")
    public List<Repository> getRepositoriesByExtensionArtifactIds(@Name("artifactIds") List<String> artifactIds) {
        return repositoryService.findByExtensionsArtifactIds(artifactIds);
    }
}
