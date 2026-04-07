package com.aryanjais.aijobagent.util;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkillNormalizerTest {

    @Test
    void normalize_knownAlias_returnsCanonical() {
        assertEquals("Node.js", SkillNormalizer.normalize("node.js"));
        assertEquals("Node.js", SkillNormalizer.normalize("nodejs"));
        assertEquals("Node.js", SkillNormalizer.normalize("node"));
        assertEquals("Spring Boot", SkillNormalizer.normalize("springboot"));
        assertEquals("Spring Boot", SkillNormalizer.normalize("spring-boot"));
    }

    @Test
    void normalize_unknownSkill_preservesOriginal() {
        assertEquals("Kubernetes", SkillNormalizer.normalize("Kubernetes"));
        assertEquals("Some Custom Skill", SkillNormalizer.normalize("Some Custom Skill"));
    }

    @Test
    void normalize_nullOrBlank_returnsEmpty() {
        assertEquals("", SkillNormalizer.normalize(null));
        assertEquals("", SkillNormalizer.normalize(""));
        assertEquals("", SkillNormalizer.normalize("   "));
    }

    @Test
    void normalizeSet_returnsLowercaseCanonical() {
        Set<String> skills = Set.of("nodejs", "Python", "springboot");
        Set<String> normalized = SkillNormalizer.normalizeSet(skills);

        assertTrue(normalized.contains("node.js"));
        assertTrue(normalized.contains("python"));
        assertTrue(normalized.contains("spring boot"));
    }

    @Test
    void normalizeSet_nullInput_returnsEmpty() {
        assertEquals(Set.of(), SkillNormalizer.normalizeSet(null));
    }

    @Test
    void normalize_caseInsensitive() {
        assertEquals("AWS", SkillNormalizer.normalize("AWS"));
        assertEquals("AWS", SkillNormalizer.normalize("aws"));
    }

    @Test
    void normalize_trimInput() {
        assertEquals("React", SkillNormalizer.normalize("  reactjs  "));
    }
}
