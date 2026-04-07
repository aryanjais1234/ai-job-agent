package com.aryanjais.aijobagent.util;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Normalises skill name variants to a canonical form for consistent matching.
 */
public final class SkillNormalizer {

    private SkillNormalizer() {
    }

    private static final Map<String, String> ALIASES = Map.ofEntries(
            Map.entry("node.js", "Node.js"),
            Map.entry("nodejs", "Node.js"),
            Map.entry("node", "Node.js"),
            Map.entry("react.js", "React"),
            Map.entry("reactjs", "React"),
            Map.entry("vue.js", "Vue.js"),
            Map.entry("vuejs", "Vue.js"),
            Map.entry("angular.js", "Angular"),
            Map.entry("angularjs", "Angular"),
            Map.entry("spring boot", "Spring Boot"),
            Map.entry("springboot", "Spring Boot"),
            Map.entry("spring-boot", "Spring Boot"),
            Map.entry("javascript", "JavaScript"),
            Map.entry("js", "JavaScript"),
            Map.entry("typescript", "TypeScript"),
            Map.entry("ts", "TypeScript"),
            Map.entry("python3", "Python"),
            Map.entry("golang", "Go"),
            Map.entry("postgresql", "PostgreSQL"),
            Map.entry("postgres", "PostgreSQL"),
            Map.entry("mongodb", "MongoDB"),
            Map.entry("mongo", "MongoDB"),
            Map.entry("mysql", "MySQL"),
            Map.entry("aws", "AWS"),
            Map.entry("amazon web services", "AWS"),
            Map.entry("gcp", "GCP"),
            Map.entry("google cloud", "GCP"),
            Map.entry("google cloud platform", "GCP"),
            Map.entry("azure", "Azure"),
            Map.entry("microsoft azure", "Azure"),
            Map.entry("k8s", "Kubernetes"),
            Map.entry("kubernetes", "Kubernetes"),
            Map.entry("docker", "Docker"),
            Map.entry("ci/cd", "CI/CD"),
            Map.entry("cicd", "CI/CD"),
            Map.entry("machine learning", "Machine Learning"),
            Map.entry("ml", "Machine Learning"),
            Map.entry("rest api", "REST APIs"),
            Map.entry("rest apis", "REST APIs"),
            Map.entry("restful", "REST APIs"),
            Map.entry("microservices", "Microservices"),
            Map.entry("micro services", "Microservices"),
            Map.entry("c++", "C++"),
            Map.entry("cpp", "C++"),
            Map.entry("c#", "C#"),
            Map.entry("csharp", "C#"),
            Map.entry(".net", ".NET"),
            Map.entry("dotnet", ".NET")
    );

    /**
     * Normalise a single skill name to its canonical form.
     */
    public static String normalize(String skill) {
        if (skill == null || skill.isBlank()) {
            return "";
        }
        String trimmed = skill.trim();
        String key = trimmed.toLowerCase();
        return ALIASES.getOrDefault(key, trimmed);
    }

    /**
     * Normalise a set of skill names, returning lowercase canonical forms suitable for comparison.
     */
    public static Set<String> normalizeSet(Set<String> skills) {
        if (skills == null) {
            return Set.of();
        }
        return skills.stream()
                .map(SkillNormalizer::normalize)
                .map(String::toLowerCase)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }
}
