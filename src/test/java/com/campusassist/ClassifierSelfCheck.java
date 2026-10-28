package com.campusassist;

import com.campusassist.ai.Classification;
import com.campusassist.ai.KeywordClassifier;
import com.campusassist.ai.LlmClassifier;
import com.campusassist.model.Category;
import com.campusassist.model.Priority;

/**
 * A tiny self-check that needs no database, no internet and no test library.
 * Run it to confirm the classifier logic works. Exits with an error if any check fails.
 */
public class ClassifierSelfCheck {

    private static int failures = 0;

    public static void main(String[] args) {
        KeywordClassifier rules = new KeywordClassifier();

        // --- keyword rules ---
        check("hostel food is bad -> HOSTEL",
                rules.classify("Mess food", "The hostel mess food quality is poor").category() == Category.HOSTEL);
        check("fee refund -> FEES",
                rules.classify("Refund", "My fee payment refund is pending").category() == Category.FEES);
        check("wifi down -> INFRASTRUCTURE",
                rules.classify("No wifi", "The lab wifi is not working").category() == Category.INFRASTRUCTURE);
        check("urgent words -> HIGH",
                rules.classify("Unsafe wiring", "Electric shock risk in room, urgent").priority() == Priority.HIGH);
        check("suggestion -> LOW",
                rules.classify("Idea", "I suggest more books").priority() == Priority.LOW);
        check("unknown text -> OTHER",
                rules.classify("Hello", "Something unrelated").category() == Category.OTHER);
        check("rules label the source RULES",
                rules.classify("x", "y").source().equals("RULES"));

        // --- validating AI replies (the most important safety step) ---
        check("valid AI reply parses",
                LlmClassifier.parseReply("HOSTEL|HIGH").map(Classification::category).orElse(null) == Category.HOSTEL);
        check("AI reply is case/space tolerant",
                LlmClassifier.parseReply("  fees | low \n").map(Classification::priority).orElse(null) == Priority.LOW);
        check("invented category rejected",
                LlmClassifier.parseReply("CANTEEN|HIGH").isEmpty());
        check("wrong format rejected",
                LlmClassifier.parseReply("I think this is a hostel issue").isEmpty());
        check("null reply rejected",
                LlmClassifier.parseReply(null).isEmpty());
        check("AI result labelled AI",
                LlmClassifier.parseReply("LIBRARY|MEDIUM").map(Classification::source).orElse("").equals("AI"));

        // --- reading the API's JSON reply ---
        String fakeJson = "{\"id\":\"msg_1\",\"type\":\"message\",\"content\":[{\"type\":\"text\",\"text\":\"FEES|MEDIUM\"}],\"stop_reason\":\"end_turn\"}";
        check("text extracted from JSON", LlmClassifier.extractText(fakeJson).equals("FEES|MEDIUM"));
        check("quotes and newlines escaped",
                LlmClassifier.jsonEscape("say \"hi\"\nnow").equals("say \\\"hi\\\"\\nnow"));

        // --- no API key: must silently fall back to rules ---
        LlmClassifier noKey = new LlmClassifier(null, "any-model", rules);
        check("no key -> falls back to RULES",
                noKey.classify("Bus late", "The college bus is always late").source().equals("RULES"));

        // --- bad key / no network: must fall back, never crash ---
        LlmClassifier broken = new LlmClassifier("bad-key", "any-model", rules);
        Classification c = broken.classify("Library books", "Not enough books in the library");
        check("API failure -> falls back to RULES", c.source().equals("RULES") && c.category() == Category.LIBRARY);

        System.out.println(failures == 0 ? "\nALL CHECKS PASSED" : "\n" + failures + " CHECK(S) FAILED");
        if (failures > 0) {
            System.exit(1);
        }
    }

    private static void check(String name, boolean ok) {
        System.out.println((ok ? "PASS  " : "FAIL  ") + name);
        if (!ok) {
            failures++;
        }
    }
}
