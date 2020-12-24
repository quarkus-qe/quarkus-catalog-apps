package io.quarkus.qe.enricher.maven;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;

public final class MavenContext {

    private final List<String> modules = new LinkedList<>();
    private final Map<String, String> properties = new HashMap<>();
    private final Map<String, DependencyContext> dependencyManagement = new HashMap<>();
    private final Map<String, DependencyContext> dependencies = new HashMap<>();
    private final Map<String, PluginContext> pluginManagement = new HashMap<>();
    private final Map<String, PluginContext> plugins = new HashMap<>();
    private final Map<String, MavenContext> moduleMavenContext = new HashMap<>();
    private MavenContext parent;

    private MavenContext(Model rawModel) {
        modules.addAll(rawModel.getModules());
        rawModel.getProperties().forEach((k, v) -> properties.put((String) k, (String) v));

        Optional.ofNullable(rawModel.getDependencyManagement()).ifPresent(dm -> {
            dependencyManagement.putAll(dependenciesAsMap(dm.getDependencies()));
        });

        dependencies.putAll(dependenciesAsMap(rawModel.getDependencies()));

        Optional.ofNullable(rawModel.getBuild()).ifPresent(build -> {
            if (build.getPluginManagement() != null) {
                pluginManagement.putAll(pluginsAsMap(build.getPluginManagement().getPlugins()));
            }

            plugins.putAll(pluginsAsMap(build.getPlugins()));
        });
    }

    public List<String> getModules() {
        return Collections.unmodifiableList(modules);
    }

    public List<DependencyContext> getAllDependencies() {
        List<DependencyContext> allDependencies = new LinkedList<>(dependencies.values());
        moduleMavenContext.values().stream().map(MavenContext::getAllDependencies).forEach(allDependencies::addAll);
        return allDependencies;
    }

    public Optional<PluginContext> getPlugin(String artifactId) {
        PluginContext plugin = plugins.get(artifactId);
        if (plugin == null && parent != null) {
            return parent.getPlugin(artifactId);
        }

        return Optional.ofNullable(plugin);
    }

    public Optional<DependencyContext> getDependencyFromDependencyManagement(String artifactId) {
        DependencyContext dependency = dependencyManagement.get(artifactId);
        if (dependency == null && parent != null) {
            return parent.getDependencyFromDependencyManagement(artifactId);
        }

        return Optional.ofNullable(dependency);
    }

    public Optional<PluginContext> getPluginFromPluginManagement(String artifactId) {
        PluginContext plugin = pluginManagement.get(artifactId);
        if (plugin == null && parent != null) {
            return parent.getPluginFromPluginManagement(artifactId);
        }

        return Optional.ofNullable(plugin);
    }

    public Optional<String> getPropertyByKey(String propertyKey) {
        String value = properties.get(propertyKey);
        if (value == null && parent != null) {
            return parent.getPropertyByKey(propertyKey);
        }

        return Optional.ofNullable(value);
    }

    public void addModuleContext(String module, MavenContext mavenContext) {
        mavenContext.parent = this;
        moduleMavenContext.put(module, mavenContext);
    }

    private Map<String, DependencyContext> dependenciesAsMap(List<Dependency> list) {
        if (list == null) {
            return Collections.emptyMap();
        }

        return list.stream().map(d -> new DependencyContext(d, this))
                .collect(Collectors.toMap(DependencyContext::getArtifactId, d -> d));
    }

    private Map<String, PluginContext> pluginsAsMap(List<Plugin> list) {
        if (list == null) {
            return Collections.emptyMap();
        }

        return list.stream().map(d -> new PluginContext(d, this))
                .collect(Collectors.toMap(PluginContext::getArtifactId, d -> d));
    }

    public static MavenContext fromModel(Model rawModel) {
        return new MavenContext(rawModel);
    }
}
