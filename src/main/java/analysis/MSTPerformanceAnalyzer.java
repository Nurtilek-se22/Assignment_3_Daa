package analysis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.File;
import java.io.IOException;
import java.util.*;


public class MSTPerformanceAnalyzer {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) {
        try {
            System.out.println("=== MST Algorithm Performance Analysis ===\n");

            // Load results from output JSON
            JsonNode outputData = objectMapper.readTree(new File("performance-data/assign_3_output.json"));
            JsonNode results = outputData.get("results");

            // Analyze results
            analyzeResults(results);

            // Generate performance comparison
            generatePerformanceComparison(results);

            // Generate theoretical analysis
            generateTheoreticalAnalysis();

        } catch (IOException e) {
            System.err.println("Error reading output file: " + e.getMessage());
        }
    }

    private static void analyzeResults(JsonNode results) {
        System.out.println("1. SUMMARY OF RESULTS");
        System.out.println("====================");

        Map<String, List<JsonNode>> algorithmResults = new HashMap<>();
        algorithmResults.put("Prim", new ArrayList<>());
        algorithmResults.put("Kruskal", new ArrayList<>());

        // Group results by algorithm
        for (JsonNode result : results) {
            String algorithm = result.get("algorithm").asText();
            algorithmResults.get(algorithm).add(result);
        }

        // Analyze each algorithm
        for (Map.Entry<String, List<JsonNode>> entry : algorithmResults.entrySet()) {
            String algorithm = entry.getKey();
            List<JsonNode> algorithmResultsList = entry.getValue();

            System.out.println("\n" + algorithm + "'s Algorithm Results:");
            System.out.println("-".repeat(30));

            double totalTime = 0;
            long totalComparisons = 0;
            long totalUnions = 0;

            for (JsonNode result : algorithmResultsList) {
                int graphId = result.get("graphId").asInt();
                int vertices = result.get("vertices").asInt();
                int edges = result.get("edges").asInt();
                int mstWeight = result.get("mstWeight").asInt();
                long executionTime = result.get("executionTimeMs").asLong();
                long comparisons = result.get("comparisons").asLong();
                long unions = result.get("unions").asLong();

                System.out.printf("Graph %d: V=%d, E=%d, Weight=%d, Time=%dms, Comparisons=%d, Unions=%d%n",
                        graphId, vertices, edges, mstWeight, executionTime, comparisons, unions);

                totalTime += executionTime;
                totalComparisons += comparisons;
                totalUnions += unions;
            }

            System.out.printf("\n%s Totals: Avg Time=%.2fms, Total Comparisons=%d, Total Unions=%d%n",
                    algorithm, totalTime / algorithmResultsList.size(), totalComparisons, totalUnions);
        }
    }

    private static void generatePerformanceComparison(JsonNode results) {
        System.out.println("\n\n2. PERFORMANCE COMPARISON");
        System.out.println("==========================");

        Map<Integer, Map<String, JsonNode>> graphResults = new HashMap<>();

        // Group results by graph ID
        for (JsonNode result : results) {
            int graphId = result.get("graphId").asInt();
            String algorithm = result.get("algorithm").asText();

            graphResults.computeIfAbsent(graphId, k -> new HashMap<>()).put(algorithm, result);
        }

        System.out.println("\nDetailed Comparison by Graph:");
        System.out.println("Graph ID | Algorithm | Vertices | Edges | MST Weight | Time(ms) | Comparisons | Unions");
        System.out.println("-".repeat(90));

        for (Map.Entry<Integer, Map<String, JsonNode>> entry : graphResults.entrySet()) {
            int graphId = entry.getKey();
            Map<String, JsonNode> algorithms = entry.getValue();

            JsonNode primResult = algorithms.get("Prim");
            JsonNode kruskalResult = algorithms.get("Kruskal");

            if (primResult != null && kruskalResult != null) {
                System.out.printf("%8d | %-8s | %8d | %5d | %10d | %7d | %11d | %6d%n",
                        graphId, "Prim", primResult.get("vertices").asInt(), primResult.get("edges").asInt(),
                        primResult.get("mstWeight").asInt(), primResult.get("executionTimeMs").asLong(),
                        primResult.get("comparisons").asLong(), primResult.get("unions").asLong());

                System.out.printf("%8d | %-8s | %8d | %5d | %10d | %7d | %11d | %6d%n",
                        graphId, "Kruskal", kruskalResult.get("vertices").asInt(), kruskalResult.get("edges").asInt(),
                        kruskalResult.get("mstWeight").asInt(), kruskalResult.get("executionTimeMs").asLong(),
                        kruskalResult.get("comparisons").asLong(), kruskalResult.get("unions").asLong());

                // Verify MST weights are identical
                int primWeight = primResult.get("mstWeight").asInt();
                int kruskalWeight = kruskalResult.get("mstWeight").asInt();

                if (primWeight == kruskalWeight) {
                    System.out.println("         ✓ MST weights match: " + primWeight);
                } else {
                    System.out.println("         ✗ MST weights differ: Prim=" + primWeight + ", Kruskal=" + kruskalWeight);
                }

                System.out.println();
            }
        }

        // Calculate average performance metrics
        calculateAverageMetrics(graphResults);
    }

    private static void calculateAverageMetrics(Map<Integer, Map<String, JsonNode>> graphResults) {
        System.out.println("\nAverage Performance Metrics:");
        System.out.println("-".repeat(40));

        double primAvgTime = 0;
        double kruskalAvgTime = 0;
        long primAvgComparisons = 0;
        long kruskalAvgComparisons = 0;
        long primAvgUnions = 0;
        long kruskalAvgUnions = 0;
        int validGraphs = 0;

        for (Map<String, JsonNode> algorithms : graphResults.values()) {
            JsonNode primResult = algorithms.get("Prim");
            JsonNode kruskalResult = algorithms.get("Kruskal");

            if (primResult != null && kruskalResult != null) {
                primAvgTime += primResult.get("executionTimeMs").asLong();
                kruskalAvgTime += kruskalResult.get("executionTimeMs").asLong();
                primAvgComparisons += primResult.get("comparisons").asLong();
                kruskalAvgComparisons += kruskalResult.get("comparisons").asLong();
                primAvgUnions += primResult.get("unions").asLong();
                kruskalAvgUnions += kruskalResult.get("unions").asLong();
                validGraphs++;
            }
        }

        if (validGraphs > 0) {
            primAvgTime /= validGraphs;
            kruskalAvgTime /= validGraphs;
            primAvgComparisons /= validGraphs;
            kruskalAvgComparisons /= validGraphs;
            primAvgUnions /= validGraphs;
            kruskalAvgUnions /= validGraphs;

            System.out.printf("Prim's Algorithm:     Avg Time=%.2fms, Avg Comparisons=%d, Avg Unions=%d%n",
                    primAvgTime, primAvgComparisons, primAvgUnions);
            System.out.printf("Kruskal's Algorithm:  Avg Time=%.2fms, Avg Comparisons=%d, Avg Unions=%d%n",
                    kruskalAvgTime, kruskalAvgComparisons, kruskalAvgUnions);

            // Performance comparison
            if (primAvgTime < kruskalAvgTime) {
                System.out.printf("Prim's is %.2fx faster on average%n", kruskalAvgTime / primAvgTime);
            } else {
                System.out.printf("Kruskal's is %.2fx faster on average%n", primAvgTime / kruskalAvgTime);
            }
        }
    }

    private static void generateTheoreticalAnalysis() {
        System.out.println("\n\n3. THEORETICAL ANALYSIS");
        System.out.println("=======================");

        System.out.println("\nPrim's Algorithm:");
        System.out.println("- Time Complexity: O(E log V) with binary heap");
        System.out.println("- Space Complexity: O(V + E)");
        System.out.println("- Best for: Dense graphs (E ≈ V²)");
        System.out.println("- Advantages:");
        System.out.println("  * Simple implementation");
        System.out.println("  * Good for dense graphs");
        System.out.println("  * Always processes V-1 edges");
        System.out.println("- Disadvantages:");
        System.out.println("  * Requires adjacency list representation");
        System.out.println("  * May be slower for sparse graphs");

        System.out.println("\nKruskal's Algorithm:");
        System.out.println("- Time Complexity: O(E log E) = O(E log V)");
        System.out.println("- Space Complexity: O(V + E)");
        System.out.println("- Best for: Sparse graphs (E << V²)");
        System.out.println("- Advantages:");
        System.out.println("  * Works with edge list representation");
        System.out.println("  * Good for sparse graphs");
        System.out.println("  * Can be parallelized");
        System.out.println("- Disadvantages:");
        System.out.println("  * Requires sorting all edges");
        System.out.println("  * Union-Find operations add overhead");

        System.out.println("\n\n4. CONCLUSIONS");
        System.out.println("===============");
        System.out.println("1. Both algorithms produce identical MST weights (verified in tests)");
        System.out.println("2. Choice depends on graph characteristics:");
        System.out.println("   - Use Prim's for dense graphs");
        System.out.println("   - Use Kruskal's for sparse graphs");
        System.out.println("3. Implementation complexity:");
        System.out.println("   - Prim's: Simpler, requires adjacency list");
        System.out.println("   - Kruskal's: More complex due to Union-Find");
        System.out.println("4. Performance considerations:");
        System.out.println("   - Both have similar theoretical complexity");
        System.out.println("   - Actual performance depends on graph density");
        System.out.println("   - Kruskal's has more overhead due to sorting and Union-Find");
    }
}
