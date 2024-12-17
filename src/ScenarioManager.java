import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

class Scenario {
    private String title;
    private List<Branch> branches;

    public Scenario(String title, List<Branch> branches) {
        this.title = title;
        this.branches = branches;
    }

    public String getTitle() {
        return title;
    }

    public List<Branch> getBranches() {
        return branches;
    }
}

class Branch {
    private String description;
    private Map<String, Integer> options;

    public Branch(String description, Map<String, Integer> options) {
        this.description = description;
        this.options = options;
    }

    public String getDescription() {
        return description;
    }

    public Map<String, Integer> getOptions() {
        return options;
    }
}

public class ScenarioManager {
    private static final String SCENARIO_DIR = "scenarios";

    public static void main(String[] args) {
        try {
            List<Scenario> scenarios = loadScenarios(SCENARIO_DIR);
            if (scenarios.isEmpty()) {
                System.out.println("No scenarios found.");
                return;
            }

            displayScenarios(scenarios);

            Scanner scanner = new Scanner(System.in);
            int choice = getUserChoice(scanner, scenarios.size());
            if (choice >= 0) {
                playScenario(scenarios.get(choice), scanner);
            } else {
                System.out.println("Invalid selection.");
            }
        } catch (Exception e) {
            System.out.println("An unexpected error occurred: " + e.getMessage());
        }
    }

    private static List<Scenario> loadScenarios(String directory) {
        List<Scenario> scenarios = new ArrayList<>();
        File dir = new File(directory);

        if (dir.exists() && dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                if (file.isFile() && file.getName().endsWith(".txt")) {
                    loadScenarioFromFile(scenarios, file);
                }
            }
        } else {
            System.out.println("Scenarios directory not found.");
        }
        return scenarios;
    }

    private static void loadScenarioFromFile(List<Scenario> scenarios, File file) {
        try {
            List<String> lines = Files.readAllLines(file.toPath());
            if (lines.isEmpty()) {
                System.out.println("Scenario file " + file.getName() + " is empty.");
                return;
            }
            String title = lines.get(0);
            List<Branch> branches = parseBranches(lines.subList(1, lines.size()));
            scenarios.add(new Scenario(title, branches));
        } catch (IOException e) {
            System.out.println("Failed to load scenario: " + file.getName());
        } catch (Exception e) {
            System.out.println("Error processing scenario file: " + file.getName() + " - " + e.getMessage());
        }
    }

    private static List<Branch> parseBranches(List<String> lines) {
        List<Branch> branches = new ArrayList<>();
        for (String line : lines) {
            String[] parts = line.split("->");
            if (parts.length < 1) {
                System.out.println("Invalid line format: " + line);
                continue;
            }
            String description = parts[0];
            Map<String, Integer> options = new HashMap<>();
            if (parts.length > 1) {
                parseOptions(options, parts[1]);
            }
            branches.add(new Branch(description, options));
        }
        return branches;
    }

    private static void parseOptions(Map<String, Integer> options, String optionsPart) {
        String[] optionParts = optionsPart.split(",");
        for (String option : optionParts) {
            String[] optionDetail = option.split(":");
            if (optionDetail.length == 2) {
                try {
                    options.put(optionDetail[0], Integer.parseInt(optionDetail[1]));
                } catch (NumberFormatException e) {
                    System.out.println("Invalid number format in options: " + option);
                }
            } else {
                System.out.println("Invalid option format: " + option);
            }
        }
    }

    private static void displayScenarios(List<Scenario> scenarios) {
        System.out.println("Available scenarios:");
        for (int i = 0; i < scenarios.size(); i++) {
            System.out.println((i + 1) + ": " + scenarios.get(i).getTitle());
        }
    }

    private static int getUserChoice(Scanner scanner, int maxChoice) {
        int choice = -1;
        while (choice < 0 || choice >= maxChoice) {
            System.out.print("Select a scenario: ");
            if (scanner.hasNextInt()) {
                choice = scanner.nextInt() - 1;
                if (choice < 0 || choice >= maxChoice) {
                    System.out.println("Invalid selection. Try again.");
                }
            } else {
                System.out.println("Invalid input. Please enter a number.");
                scanner.next(); // Consume the invalid input
            }
        }
        return choice;
    }

    private static void playScenario(Scenario scenario, Scanner scanner) {
        List<Branch> branches = scenario.getBranches();
        int index = 0;

        while (index >= 0 && index < branches.size()) {
            try {
                Branch branch = branches.get(index);
                System.out.println(branch.getDescription());

                Map<String, Integer> options = branch.getOptions();
                if (!options.isEmpty()) {
                    index = getBranchIndex(scanner, options);
                } else {
                    System.out.println("End of scenario. Exiting application.");
                    break; // Завершение работы приложения при достижении последней ветки
                }
            } catch (Exception e) {
                System.out.println("An error occurred while processing the scenario: " + e.getMessage());
                break; // Завершение работы приложения в случае ошибки
            }
        }
    }

    private static int getBranchIndex(Scanner scanner, Map<String, Integer> options) {
        int index = -1;
        List<String> optionKeys = new ArrayList<>(options.keySet());
        while (index < 0) {
            for (int i = 0; i < optionKeys.size(); i++) {
                System.out.println((i + 1) + ": " + optionKeys.get(i));
            }

            System.out.print("Your choice: ");
            String userInput = scanner.next();

            try {
                int choice = Integer.parseInt(userInput) - 1;
                if (choice >= 0 && choice < optionKeys.size()) {
                    index = options.get(optionKeys.get(choice));
                } else {
                    System.out.println("Invalid choice. Try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
        return index;
    }
}