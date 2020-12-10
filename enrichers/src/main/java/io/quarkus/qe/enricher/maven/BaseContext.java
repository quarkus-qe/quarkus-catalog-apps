package io.quarkus.qe.enricher.maven;

import org.apache.commons.lang3.StringUtils;

public abstract class BaseContext {

    private static final String PROPERTY_OPEN = "${";
    private static final String PROPERTY_CLOSE = "}";

    private final MavenContext context;

    public BaseContext(MavenContext context) {
        this.context = context;
    }

    public abstract String getArtifactId();

    public MavenContext getContext() {
        return context;
    }

    /**
     * It can be a version 1.2.3.Final, or a property ${my.property}
     */
    protected String getOrLookupProperty(String value) {
        if (StringUtils.isNotEmpty(value) && value.startsWith(PROPERTY_OPEN)) {
            return context.getPropertyByKey(StringUtils.substringBetween(value, PROPERTY_OPEN, PROPERTY_CLOSE)).orElse(null);
        }

        return value;
    }
}
