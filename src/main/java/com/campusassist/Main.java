package com.campusassist;

import com.campusassist.ai.KeywordClassifier;
import com.campusassist.ai.LlmClassifier;
import com.campusassist.dao.ComplaintDAO;
import com.campusassist.exception.InvalidInputException;
import com.campusassist.model.Complaint;
import com.campusassist.model.Status;
import com.campusassist.service.ComplaintService;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/** The console menu. It only reads input and prints output; all real work is in the service. */
public class Main {

    public static void main(String[] args) {
        // Wire the layers together once, here.
        ComplaintService service = new ComplaintService(
                new ComplaintDAO(),
                LlmClassifier.fromEnvironment(new KeywordClassifier()));

        Scanner in = new Scanner(System.in);
        System.out.println("=== CampusAssist: Student Query and Complaint Manager ===");

        boolean running = true;
        while (running) {
            printMenu();
            String choice = in.nextLine().trim();
            try {
                switch (choice) {
                    case "1" -> submit(service, in);
                    case "2" -> print(service.all());
                    case "3" -> print(service.byStatus(readStatus(in)));
                    case "4" -> viewOne(service, in);
                    case "5" -> updateStatus(service, in);
                    case "6" -> report(service);
                    case "0" -> running = false;
                    default -> System.out.println("Please choose a number from the menu.");
                }
            } catch (InvalidInputException e) {
                System.out.println("Input problem: " + e.getMessage());   // our own, friendly errors
            } catch (SQLException e) {
                System.out.println("Database problem: " + e.getMessage());
            }
        }
        System.out.println("Goodbye!");
    }

    private static void printMenu() {
        System.out.println("\n1. Submit a complaint");
        System.out.println("2. View all complaints (urgent first)");
        System.out.println("3. View complaints by status");
        System.out.println("4. View one complaint");
        System.out.println("5. Update complaint status (admin)");
        System.out.println("6. Summary report");
        System.out.println("0. Exit");
        System.out.print("Choose: ");
    }

    private static void submit(ComplaintService service, Scanner in) throws SQLException {
        System.out.print("Your name: ");
        String name = in.nextLine();
        System.out.print("Complaint title: ");
        String title = in.nextLine();
        System.out.print("Describe the problem: ");
        String description = in.nextLine();

        Complaint c = service.submit(name, title, description);
        System.out.println("Saved as complaint #" + c.getId());
        System.out.println("Category: " + c.getCategory() + " | Priority: " + c.getPriority()
                + " (decided by " + c.getClassifiedBy() + ")");
    }

    private static void viewOne(ComplaintService service, Scanner in) throws SQLException {
        Complaint c = service.get(readInt(in, "Complaint id: "));
        System.out.println(c);
        System.out.println("Details: " + c.getDescription());
        System.out.println("Created: " + c.getCreatedAt());
    }

    private static void updateStatus(ComplaintService service, Scanner in) throws SQLException {
        int id = readInt(in, "Complaint id: ");
        Status status = readStatus(in);
        service.changeStatus(id, status);
        System.out.println("Complaint #" + id + " is now " + status);
    }

    private static void report(ComplaintService service) throws SQLException {
        System.out.println("-- By category --");
        for (Map.Entry<String, Integer> e : service.categoryReport().entrySet()) {
            System.out.println(String.format("%-15s %d", e.getKey(), e.getValue()));
        }
        System.out.println("-- By status --");
        for (Map.Entry<String, Integer> e : service.statusReport().entrySet()) {
            System.out.println(String.format("%-15s %d", e.getKey(), e.getValue()));
        }
    }

    private static void print(List<Complaint> list) {
        if (list.isEmpty()) {
            System.out.println("No complaints found.");
            return;
        }
        list.forEach(System.out::println);
    }

    // ---------- safe input helpers ----------

    private static int readInt(Scanner in, String prompt) {
        System.out.print(prompt);
        try {
            return Integer.parseInt(in.nextLine().trim());
        } catch (NumberFormatException e) {
            throw new InvalidInputException("Please type a whole number");
        }
    }

    private static Status readStatus(Scanner in) {
        System.out.print("Status (OPEN / IN_PROGRESS / RESOLVED): ");
        try {
            return Status.valueOf(in.nextLine().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidInputException("Status must be OPEN, IN_PROGRESS or RESOLVED");
        }
    }
}
