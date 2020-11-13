package io.quarkus.qe.configuration;

import io.quarkus.kafka.client.serialization.JsonbDeserializer;
import io.quarkus.qe.model.requests.NewRepositoryRequest;

public class NewRepositoryRequestJsonbDeserializer extends JsonbDeserializer<NewRepositoryRequest> {

    public NewRepositoryRequestJsonbDeserializer() {
        super(NewRepositoryRequest.class);
    }

}
