package com.campusassist.ai;

import com.campusassist.model.Category;
import com.campusassist.model.Priority;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Plain rule-based classifier: counts keywords. No internet, no API key.
 * It is the safety net used when the AI is unreachable, so the app never stops working.
 */
public class KeywordClassifier implements ComplaintClassifier {

    private static final Map<Category, String[]> KEYWORDS = new LinkedHashMap<>();
    private static final String[] URGENT_WORDS = {
            "urgent", "emergency", "unsafe", "fire", "harass", "ragging", "medical",
            "shock", "threat", "injury", "immediately", "no water"
    };
    private static final String[] MINOR_WORDS = { "suggest", "request", "would be nice", "improve" };

    static {
        KEYWORDS.put(Category.ACADEMIC, new String[]{"exam", "marks", "result", "faculty", "class", "timetable", "attendance", "syllabus", "internal"});
        KEYWORDS.put(Category.HOSTEL, new String[]{"hostel", "room", "mess", "warden", "roommate", "laundry", "food"});
        KEYWORDS.put(Category.FEES, new String[]{"fee", "fees", "payment", "refund", "scholarship", "receipt", "challan"});
        KEYWORDS.put(Category.TRANSPORT, new String[]{"bus", "transport", "driver", "route", "van"});
        KEYWORDS.put(Category.LIBRARY, new String[]{"library", "book", "journal", "librarian", "borrow"});
        KEYWORDS.put(Category.INFRASTRUCTURE, new String[]{"wifi", "wi-fi", "internet", "lab", "projector", "ac ", "fan", "water", "toilet", "electricity", "power", "leak", "light"});
    }

    @Override
    public Classification classify(String title, String description) {
        String text = (title + " " + description).toLowerCase();

        // 1. Category = the one whose keywords appear most often
        Category best = Category.OTHER;
        int bestHits = 0;
        for (Map.Entry<Category, String[]> entry : KEYWORDS.entrySet()) {
            int hits = countHits(text, entry.getValue());
            if (hits > bestHits) {
                bestHits = hits;
                best = entry.getKey();
            }
        }

        // 2. Priority = urgent words win, then "just a suggestion" words, else MEDIUM
        Priority priority = Priority.MEDIUM;
        if (countHits(text, URGENT_WORDS) > 0) {
            priority = Priority.HIGH;
        } else if (countHits(text, MINOR_WORDS) > 0) {
            priority = Priority.LOW;
        }
        return new Classification(best, priority, "RULES");
    }

    private int countHits(String text, String[] words) {
        int hits = 0;
        for (String word : words) {
            if (text.contains(word)) {
                hits++;
            }
        }
        return hits;
    }
}
