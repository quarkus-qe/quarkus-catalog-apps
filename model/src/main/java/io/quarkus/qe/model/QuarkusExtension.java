package io.quarkus.qe.model;

public class QuarkusExtension {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof QuarkusExtension))
            return false;
        QuarkusExtension other = (QuarkusExtension) obj;
        return name.equals(other.name);
    }

    @Override
    public final int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}
