package analysis;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class CSVSummaryGenerator {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) {
        try {
            System.out.println("Generating CSV performance summary...");

            // Read results from JSON
            JsonNode outputData = objectMapper.readTree(
                    new File("performance-data/assign_3_output.json")
            );
            JsonNode results = outputData.get("results");

            // Generate detailed comparison CSV
            generateDetailedCSV(results);

            // Generate summary statistics CSV
            generateSummaryCSV(results);

            System.out.println("\n✅ CSV files generated successfully!");
            System.out.println("   - performance-data/detailed_comparison.csv");
            System.out.println("   - performance-data/summary_statistics.csv");

        } catch (IOException e) {
            System.err.println("Error generating CSV: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void generateDetailedCSV(JsonNode results) throws IOException {
        File csvFile = new File("performance-data/detailed_comparison.csv");

        try (PrintWriter writer = new PrintWriter(new FileWriter(csvFile))) {
            // Write header
            writer.println("Graph_ID,Graph_Name,Algorithm,Vertices,Edges,MST_Weight," +
                    "Execution_Time_ms,Comparisons,Unions,Valid_MST");

            // Write data rows
            for (JsonNode result : results) {
                writer.printf("%d,%s,%s,%d,%d,%d,%d,%d,%d,%s%n",
                        result.get("graphId").asInt(),
                        result.get("graphName").asText(),
                        result.get("algorithm").asText(),
                        result.get("vertices").asInt(),
                        result.get("edges").asInt(),
                        result.get("mstWeight").asInt(),
                        result.get("executionTimeMs").asInt(),
                        result.get("comparisons").asInt(),
                        result.get("unions").asInt(),
                        result.get("isValidMST").asBoolean()
                );
            }
        }
    }

    private static void generateSummaryCSV(JsonNode results) throws IOException {
        // Collect data by graph
        List<GraphComparison> comparisons = new ArrayList<>();

        for (int i = 0; i < results.size(); i += 2) {
            JsonNode primResult = results.get(i);
            JsonNode kruskalResult = results.get(i + 1);

            GraphComparison comp = new GraphComparison();
            comp.graphId = primResult.get("graphId").asInt();
            comp.vertices = primResult.get("vertices").asInt();
            comp.edges = primResult.get("edges").asInt();
            comp.mstWeight = primResult.get("mstWeight").asInt();

            comp.primTime = primResult.get("executionTimeMs").asInt();
            comp.primComparisons = primResult.get("comparisons").asInt();

            comp.kruskalTime = kruskalResult.get("executionTimeMs").asInt();
            comp.kruskalComparisons = kruskalResult.get("comparisons").asInt();
            comp.kruskalUnions = kruskalResult.get("unions").asInt();

            comparisons.add(comp);
        }

        // Write summary CSV
        File csvFile = new File("performance-data/summary_statistics.csv");

        try (PrintWriter writer = new PrintWriter(new FileWriter(csvFile))) {
            // Write header
            writer.println("Graph_ID,Vertices,Edges,MST_Weight," +
                    "Prim_Time_ms,Prim_Comparisons," +
                    "Kruskal_Time_ms,Kruskal_Comparisons,Kruskal_Unions," +
                    "Time_Diff_ms,Comparison_Diff,Faster_Algorithm");

            // Write data rows
            for (GraphComparison comp : comparisons) {
                int timeDiff = comp.primTime - comp.kruskalTime;
                int compDiff = comp.primComparisons - comp.kruskalComparisons;
                String faster = timeDiff > 0 ? "Kruskal" :
                        (timeDiff < 0 ? "Prim" : "Equal");

                writer.printf("%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%s%n",
                        comp.graphId,
                        comp.vertices,
                        comp.edges,
                        comp.mstWeight,
                        comp.primTime,
                        comp.primComparisons,
                        comp.kruskalTime,
                        comp.kruskalComparisons,
                        comp.kruskalUnions,
                        timeDiff,
                        compDiff,
                        faster
                );
            }
        }

        // Print summary statistics to console
        printSummaryStatistics(comparisons);
    }

    private static void printSummaryStatistics(List<GraphComparison> comparisons) {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("PERFORMANCE SUMMARY STATISTICS");
        System.out.println("=".repeat(70));

        // Calculate averages
        double avgPrimComparisons = comparisons.stream()
                .mapToInt(c -> c.primComparisons)
                .average()
                .orElse(0);

        double avgKruskalComparisons = comparisons.stream()
                .mapToInt(c -> c.kruskalComparisons)
                .average()
                .orElse(0);

        // Count wins
        long primWins = comparisons.stream()
                .filter(c -> c.primComparisons < c.kruskalComparisons)
                .count();

        long kruskalWins = comparisons.stream()
                .filter(c -> c.kruskalComparisons < c.primComparisons)
                .count();

        System.out.println("\nTotal Graphs Tested: " + comparisons.size());
        System.out.println("\nAverage Comparisons:");
        System.out.printf("  Prim's Algorithm:    %.2f\n", avgPrimComparisons);
        System.out.printf("  Kruskal's Algorithm: %.2f\n", avgKruskalComparisons);

        System.out.println("\nComparison Efficiency:");
        System.out.println("  Prim's fewer comparisons:    " + primWins + " graphs");
        System.out.println("  Kruskal's fewer comparisons: " + kruskalWins + " graphs");

        // Graph size ranges
        int minVertices = comparisons.stream().mapToInt(c -> c.vertices).min().orElse(0);
        int maxVertices = comparisons.stream().mapToInt(c -> c.vertices).max().orElse(0);
        int minEdges = comparisons.stream().mapToInt(c -> c.edges).min().orElse(0);
        int maxEdges = comparisons.stream().mapToInt(c -> c.edges).max().orElse(0);

        System.out.println("\nGraph Size Range:");
        System.out.println("  Vertices: " + minVertices + " - " + maxVertices);
        System.out.println("  Edges:    " + minEdges + " - " + maxEdges);

        System.out.println("\n" + "=".repeat(70));
    }

    private static class GraphComparison {
        int graphId;
        int vertices;
        int edges;
        int mstWeight;
        int primTime;
        int primComparisons;
        int kruskalTime;
        int kruskalComparisons;
        int kruskalUnions;
    }
}

