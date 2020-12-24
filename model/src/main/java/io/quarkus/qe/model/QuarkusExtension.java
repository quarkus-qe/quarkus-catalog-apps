package io.quarkus.qe.model;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

public class QuarkusExtension {
    private String name;
    private String version;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof QuarkusExtension)) {
            return false;
        }

        QuarkusExtension other = (QuarkusExtension) obj;
        return name.equals(other.name) && Objects.equals(other.version, version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, version);
    }

    @Override
    public String toString() {
        return name + Optional.ofNullable(version).filter(StringUtils::isNotEmpty).map(v -> ":" + v).orElse(EMPTY);
    }
}
