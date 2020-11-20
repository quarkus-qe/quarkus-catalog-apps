package io.quarkus.qe.data.marshallers;

import javax.enterprise.context.ApplicationScoped;

import io.quarkus.qe.data.LogEntity;
import io.quarkus.qe.model.Log;

@ApplicationScoped
public class LogMarshaller {

    public LogEntity fromModel(Log model) {
        LogEntity entity = new LogEntity();
        entity.level = model.getLevel().name();
        entity.timestamp = model.getTimestamp();
        entity.message = model.getMessage();
        return entity;
    }
}
