package io.quarkus.qe;

import java.util.List;

import javax.inject.Inject;

import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.graphql.Source;

import io.quarkus.qe.exceptions.RepositoryNotFoundException;
import io.quarkus.qe.model.Repository;
import io.quarkus.qe.model.RepositoryList;
import io.quarkus.qe.model.requests.RepositoryQueryRequest;
import io.quarkus.qe.services.RepositoryService;

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
    public RepositoryList getAllRepositories(@Source RepositoryQueryRequest request) {
        return toRepositoryList(repositoryService.find(request));
    }

    private RepositoryList toRepositoryList(List<Repository> repositories) {
        RepositoryList response = new RepositoryList();
        response.setList(repositories);
        response.setTotalCount(repositories.size());
        return response;
    }
}
