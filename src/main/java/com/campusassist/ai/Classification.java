package com.campusassist.ai;

import com.campusassist.model.Category;
import com.campusassist.model.Priority;

/**
 * The answer a classifier gives: a category, a priority, and who decided it ("AI" or "RULES").
 * A record is a short way to write a class that only carries data (Java 16+).
 */
public record Classification(Category category, Priority priority, String source) { }
