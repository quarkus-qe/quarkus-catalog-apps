package io.quarkus.qe;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

public class HealthIT extends BaseIT {

    private static final String HEALTH_PATH = "/health";

    @Test
    public void healthEndpointsShouldBeOk() {
        awaitFor(() -> givenStorageService().get(HEALTH_PATH).then().statusCode(HttpStatus.SC_OK));
        awaitFor(() -> givenRestApiService().get(HEALTH_PATH).then().statusCode(HttpStatus.SC_OK));
        awaitFor(() -> givenEnrichService().get(HEALTH_PATH).then().statusCode(HttpStatus.SC_OK));
    }
}
