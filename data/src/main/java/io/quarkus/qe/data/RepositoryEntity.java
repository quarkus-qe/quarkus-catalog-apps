package io.quarkus.qe.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import io.quarkus.hibernate.orm.panache.PanacheEntity;

@Entity(name = "repository")
public class RepositoryEntity extends PanacheEntity {
    @Column(unique = true, nullable = false)
    public String repoUrl;
    public String someUpdate;
}
