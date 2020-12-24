package io.quarkus.qe.data;

import javax.persistence.Entity;
import javax.persistence.Id;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

@Entity(name = "quarkus_version")
public class QuarkusVersionEntity extends PanacheEntityBase {
    @Id
    public String id;

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof QuarkusVersionEntity))
            return false;
        QuarkusVersionEntity other = (QuarkusVersionEntity) obj;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return id;
    }
}
