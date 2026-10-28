package com.campusassist.ai;

/**
 * Anything that can read a complaint and decide its category and priority.
 * Having an interface lets us swap the "brain" (AI, keyword rules, a test stub)
 * without touching the rest of the program.
 */
public interface ComplaintClassifier {
    Classification classify(String title, String description);
}
