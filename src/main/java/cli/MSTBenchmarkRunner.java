package cli;

import algorithms.*;
import metrics.PerformanceTracker;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class MSTBenchmarkRunner {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) {
        try {
            System.out.println("Running MST Algorithm Benchmarks (Prim's vs Kruskal's)...");

            // Load input data
            JsonNode inputData = objectMapper.readTree(new File("performance-data/assign_3_input.json"));
            JsonNode graphs = inputData.get("graphs");

            List<Map<String, Object>> results = new ArrayList<>();

            System.out.println("Graph ID,Algorithm,Vertices,Edges,MST Weight,Execution Time(ms),Comparisons,Unions");

            for (JsonNode graphNode : graphs) {
                int graphId = graphNode.get("id").asInt();
                String graphName = "Graph " + graphId;
                int vertices = graphNode.get("vertices").asInt();
                JsonNode edgesNode = graphNode.get("edges");

                // Create graph from JSON data
                Graph graph = createGraphFromJson(vertices, edgesNode);

                // Test Prim's algorithm
                testPrimsAlgorithm(graphId, graphName, graph, results);

                // Test Kruskal's algorithm
                testKruskalsAlgorithm(graphId, graphName, graph, results);
            }

            // Save results to output JSON
            saveResultsToJson(results);

            System.out.println("\nBenchmark completed! Results saved to assign_3_output.json");

        } catch (IOException e) {
            System.err.println("Error reading input file: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error during benchmark: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void testPrimsAlgorithm(int graphId, String graphName, Graph graph, List<Map<String, Object>> results) {
        try {
            PrimsAlgorithm prims = new PrimsAlgorithm();
            PerformanceTracker tracker = new PerformanceTracker();

            long startTime = System.nanoTime();
            List<Edge> mst = prims.findMSTWithTracking(graph, tracker);
            long endTime = System.nanoTime();

            long executionTime = (endTime - startTime) / 1_000_000; // Convert to milliseconds
            int mstWeight = prims.calculateMSTWeight(mst);

            System.out.printf("%d,Prim,%d,%d,%d,%d,%d,%d%n",
                    graphId, graph.getVertices(), graph.getEdgesCount(),
                    mstWeight, executionTime, tracker.getComparisons(), 0);

            // Store result
            Map<String, Object> result = new HashMap<>();
            result.put("graphId", graphId);
            result.put("graphName", graphName);
            result.put("algorithm", "Prim");
            result.put("vertices", graph.getVertices());
            result.put("edges", graph.getEdgesCount());
            result.put("mstWeight", mstWeight);
            result.put("executionTimeMs", executionTime);
            result.put("comparisons", tracker.getComparisons());
            result.put("unions", 0);
            result.put("mstEdges", convertEdgesToJson(mst));
            result.put("isValidMST", prims.isValidMST(graph, mst));

            results.add(result);

        } catch (Exception e) {
            System.err.println("Error testing Prim's algorithm on graph " + graphId + ": " + e.getMessage());
        }
    }

    private static void testKruskalsAlgorithm(int graphId, String graphName, Graph graph, List<Map<String, Object>> results) {
        try {
            KruskalsAlgorithm kruskals = new KruskalsAlgorithm();
            PerformanceTracker tracker = new PerformanceTracker();

            long startTime = System.nanoTime();
            List<Edge> mst = kruskals.findMSTWithTracking(graph, tracker);
            long endTime = System.nanoTime();

            long executionTime = (endTime - startTime) / 1_000_000; // Convert to milliseconds
            int mstWeight = kruskals.calculateMSTWeight(mst);

            System.out.printf("%d,Kruskal,%d,%d,%d,%d,%d,%d%n",
                    graphId, graph.getVertices(), graph.getEdgesCount(),
                    mstWeight, executionTime, tracker.getComparisons(), tracker.getSwaps());

            // Store result
            Map<String, Object> result = new HashMap<>();
            result.put("graphId", graphId);
            result.put("graphName", graphName);
            result.put("algorithm", "Kruskal");
            result.put("vertices", graph.getVertices());
            result.put("edges", graph.getEdgesCount());
            result.put("mstWeight", mstWeight);
            result.put("executionTimeMs", executionTime);
            result.put("comparisons", tracker.getComparisons());
            result.put("unions", tracker.getSwaps());
            result.put("mstEdges", convertEdgesToJson(mst));
            result.put("isValidMST", kruskals.isValidMST(graph, mst));

            results.add(result);

        } catch (Exception e) {
            System.err.println("Error testing Kruskal's algorithm on graph " + graphId + ": " + e.getMessage());
        }
    }

    private static Graph createGraphFromJson(int vertices, JsonNode edgesNode) {
        Graph graph = new Graph(vertices);

        for (JsonNode edgeNode : edgesNode) {
            int from = edgeNode.get("from").asInt();
            int to = edgeNode.get("to").asInt();
            int weight = edgeNode.get("weight").asInt();
            graph.addEdge(from, to, weight);
        }

        return graph;
    }

    private static List<Map<String, Object>> convertEdgesToJson(List<Edge> edges) {
        List<Map<String, Object>> edgeList = new ArrayList<>();

        for (Edge edge : edges) {
            Map<String, Object> edgeMap = new HashMap<>();
            edgeMap.put("from", edge.getFrom());
            edgeMap.put("to", edge.getTo());
            edgeMap.put("weight", edge.getWeight());
            edgeList.add(edgeMap);
        }

        return edgeList;
    }

    private static void saveResultsToJson(List<Map<String, Object>> results) throws IOException {
        Map<String, Object> outputData = new HashMap<>();
        outputData.put("results", results);
        outputData.put("timestamp", new Date().toString());
        outputData.put("totalTests", results.size());

        objectMapper.writerWithDefaultPrettyPrinter()
                .writeValue(new File("performance-data/assign_3_output.json"), outputData);
    }
}
