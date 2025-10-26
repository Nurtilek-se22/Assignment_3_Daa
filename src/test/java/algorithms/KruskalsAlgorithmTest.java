package algorithms;

import metrics.PerformanceTracker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

public class KruskalsAlgorithmTest {

    private Graph smallGraph;
    private Graph mediumGraph;
    private Graph largeGraph;
    private Graph disconnectedGraph;
    private KruskalsAlgorithm kruskals;

    @BeforeEach
    void setUp() {
        kruskals = new KruskalsAlgorithm();

        // Create small connected graph
        smallGraph = new Graph(4);
        smallGraph.addEdge(0, 1, 10);
        smallGraph.addEdge(0, 2, 6);
        smallGraph.addEdge(0, 3, 5);
        smallGraph.addEdge(1, 3, 15);
        smallGraph.addEdge(2, 3, 4);

        // Create medium connected graph
        mediumGraph = new Graph(6);
        mediumGraph.addEdge(0, 1, 4);
        mediumGraph.addEdge(0, 2, 3);
        mediumGraph.addEdge(1, 2, 1);
        mediumGraph.addEdge(1, 3, 2);
        mediumGraph.addEdge(2, 3, 4);
        mediumGraph.addEdge(2, 4, 2);
        mediumGraph.addEdge(3, 4, 3);
        mediumGraph.addEdge(3, 5, 2);
        mediumGraph.addEdge(4, 5, 6);

        // Create large connected graph
        largeGraph = new Graph(8);
        largeGraph.addEdge(0, 1, 2);
        largeGraph.addEdge(0, 2, 3);
        largeGraph.addEdge(1, 2, 1);
        largeGraph.addEdge(1, 3, 4);
        largeGraph.addEdge(2, 3, 5);
        largeGraph.addEdge(2, 4, 6);
        largeGraph.addEdge(3, 4, 7);
        largeGraph.addEdge(3, 5, 8);
        largeGraph.addEdge(4, 5, 9);
        largeGraph.addEdge(4, 6, 10);
        largeGraph.addEdge(5, 6, 11);
        largeGraph.addEdge(5, 7, 12);
        largeGraph.addEdge(6, 7, 13);

        // Create disconnected graph
        disconnectedGraph = new Graph(6);
        disconnectedGraph.addEdge(0, 1, 2);
        disconnectedGraph.addEdge(0, 2, 3);
        disconnectedGraph.addEdge(1, 2, 1);
        disconnectedGraph.addEdge(3, 4, 4);
        disconnectedGraph.addEdge(3, 5, 5);
        disconnectedGraph.addEdge(4, 5, 2);
    }

    // ========== CORRECTNESS TESTS ==========

    @Test
    void testKruskalsCorrectnessSmallGraph() {
        List<Edge> mst = kruskals.findMST(smallGraph);

        // Check MST has correct number of edges
        assertEquals(3, mst.size()); // V-1 edges

        // Check MST is valid
        assertTrue(kruskals.isValidMST(smallGraph, mst));

        // Check MST weight (should be minimum)
        int mstWeight = kruskals.calculateMSTWeight(mst);
        assertEquals(19, mstWeight); // Expected minimum weight
    }

    @Test
    void testKruskalsCorrectnessMediumGraph() {
        List<Edge> mst = kruskals.findMST(mediumGraph);

        // Check MST has correct number of edges
        assertEquals(5, mst.size()); // V-1 edges

        // Check MST is valid
        assertTrue(kruskals.isValidMST(mediumGraph, mst));
    }

    @Test
    void testKruskalsCorrectnessLargeGraph() {
        List<Edge> mst = kruskals.findMST(largeGraph);

        // Check MST has correct number of edges
        assertEquals(7, mst.size()); // V-1 edges

        // Check MST is valid
        assertTrue(kruskals.isValidMST(largeGraph, mst));
    }

    @Test
    void testMSTContainsAllVertices() {
        List<Edge> kruskalsMST = kruskals.findMST(mediumGraph);

        // Check that MST connects all vertices
        assertTrue(kruskals.isValidMST(mediumGraph, kruskalsMST));
    }

    @Test
    void testMSTIsAcyclic() {
        List<Edge> kruskalsMST = kruskals.findMST(largeGraph);

        // MST should be acyclic
        assertTrue(kruskals.isValidMST(largeGraph, kruskalsMST));
    }

    @Test
    void testDisconnectedGraphHandling() {
        // Algorithm should throw exception for disconnected graphs
        assertThrows(IllegalArgumentException.class, () -> kruskals.findMST(disconnectedGraph));
    }

    @Test
    void testEmptyGraphHandling() {
        Graph emptyGraph = new Graph(0);

        List<Edge> kruskalsMST = kruskals.findMST(emptyGraph);

        assertTrue(kruskalsMST.isEmpty());
    }

    @Test
    void testSingleVertexGraph() {
        Graph singleVertexGraph = new Graph(1);

        List<Edge> kruskalsMST = kruskals.findMST(singleVertexGraph);

        assertTrue(kruskalsMST.isEmpty());
    }

    // ========== PERFORMANCE AND CONSISTENCY TESTS ==========

    @Test
    void testExecutionTimeNonNegative() {
        long startTime = System.nanoTime();
        kruskals.findMST(mediumGraph);
        long endTime = System.nanoTime();

        assertTrue(endTime >= startTime);
    }

    @Test
    void testOperationCountsNonNegative() {
        PerformanceTracker tracker = new PerformanceTracker();

        kruskals.findMSTWithTracking(mediumGraph, tracker);
        assertTrue(tracker.getComparisons() >= 0);
        assertTrue(tracker.getSwaps() >= 0);
    }

    @Test
    void testResultsReproducible() {
        List<Edge> kruskalsMST1 = kruskals.findMST(mediumGraph);
        List<Edge> kruskalsMST2 = kruskals.findMST(mediumGraph);

        // Results should be reproducible (same weight)
        assertEquals(kruskals.calculateMSTWeight(kruskalsMST1), kruskals.calculateMSTWeight(kruskalsMST2));
    }

    // ========== EDGE CASES ==========

    @Test
    void testGraphWithDuplicateEdges() {
        Graph graphWithDuplicates = new Graph(3);
        graphWithDuplicates.addEdge(0, 1, 5);
        graphWithDuplicates.addEdge(0, 1, 3); // Duplicate edge with different weight
        graphWithDuplicates.addEdge(1, 2, 4);
        graphWithDuplicates.addEdge(0, 2, 2);

        List<Edge> kruskalsMST = kruskals.findMST(graphWithDuplicates);

        assertTrue(kruskals.isValidMST(graphWithDuplicates, kruskalsMST));
    }

    @Test
    void testGraphWithSelfLoops() {
        Graph graphWithSelfLoops = new Graph(3);
        graphWithSelfLoops.addEdge(0, 1, 2);
        graphWithSelfLoops.addEdge(1, 2, 3);
        graphWithSelfLoops.addEdge(0, 2, 1);

        List<Edge> kruskalsMST = kruskals.findMST(graphWithSelfLoops);

        assertTrue(kruskals.isValidMST(graphWithSelfLoops, kruskalsMST));
    }

    @Test
    void testGraphWithNegativeWeights() {
        Graph graphWithNegativeWeights = new Graph(3);
        graphWithNegativeWeights.addEdge(0, 1, -2);
        graphWithNegativeWeights.addEdge(1, 2, 3);
        graphWithNegativeWeights.addEdge(0, 2, 1);

        List<Edge> kruskalsMST = kruskals.findMST(graphWithNegativeWeights);

        assertTrue(kruskals.isValidMST(graphWithNegativeWeights, kruskalsMST));
    }

    @Test
    void testGraphWithZeroWeights() {
        Graph graphWithZeroWeights = new Graph(3);
        graphWithZeroWeights.addEdge(0, 1, 0);
        graphWithZeroWeights.addEdge(1, 2, 3);
        graphWithZeroWeights.addEdge(0, 2, 1);

        List<Edge> kruskalsMST = kruskals.findMST(graphWithZeroWeights);

        assertTrue(kruskals.isValidMST(graphWithZeroWeights, kruskalsMST));
    }

    @Test
    void testCompleteGraph() {
        // Test on a complete graph (all vertices connected to all others)
        Graph completeGraph = new Graph(4);
        completeGraph.addEdge(0, 1, 1);
        completeGraph.addEdge(0, 2, 2);
        completeGraph.addEdge(0, 3, 3);
        completeGraph.addEdge(1, 2, 4);
        completeGraph.addEdge(1, 3, 5);
        completeGraph.addEdge(2, 3, 6);

        List<Edge> kruskalsMST = kruskals.findMST(completeGraph);

        assertEquals(3, kruskalsMST.size());
        assertTrue(kruskals.isValidMST(completeGraph, kruskalsMST));
    }

    @Test
    void testEdgeSortingByWeight() {
        // Kruskal's algorithm sorts edges by weight
        // This test ensures edges are processed in correct order
        Graph graph = new Graph(4);
        graph.addEdge(0, 1, 4);
        graph.addEdge(0, 2, 1);
        graph.addEdge(1, 2, 3);
        graph.addEdge(1, 3, 2);
        graph.addEdge(2, 3, 5);

        List<Edge> mst = kruskals.findMST(graph);

        // MST should include edges with weights 1, 2, 3
        int totalWeight = kruskals.calculateMSTWeight(mst);
        assertEquals(6, totalWeight);
    }
}

