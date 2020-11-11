package io.quarkus.qe;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/catalog")
public class HelloCatalog {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/")
    public String helloWorld() {
        return "hello Catalog";
    }
}
