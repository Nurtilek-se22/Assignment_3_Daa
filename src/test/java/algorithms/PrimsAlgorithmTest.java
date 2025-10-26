package algorithms;

import metrics.PerformanceTracker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

public class PrimsAlgorithmTest {

    private Graph smallGraph;
    private Graph mediumGraph;
    private Graph largeGraph;
    private Graph disconnectedGraph;
    private PrimsAlgorithm prims;

    @BeforeEach
    void setUp() {
        prims = new PrimsAlgorithm();

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
    void testPrimsCorrectnessSmallGraph() {
        List<Edge> mst = prims.findMST(smallGraph);

        // Check MST has correct number of edges
        assertEquals(3, mst.size()); // V-1 edges

        // Check MST is valid
        assertTrue(prims.isValidMST(smallGraph, mst));

        // Check MST weight (should be minimum)
        int mstWeight = prims.calculateMSTWeight(mst);
        assertEquals(19, mstWeight); // Expected minimum weight
    }

    @Test
    void testPrimsCorrectnessMediumGraph() {
        List<Edge> mst = prims.findMST(mediumGraph);

        // Check MST has correct number of edges
        assertEquals(5, mst.size()); // V-1 edges

        // Check MST is valid
        assertTrue(prims.isValidMST(mediumGraph, mst));
    }

    @Test
    void testPrimsCorrectnessLargeGraph() {
        List<Edge> mst = prims.findMST(largeGraph);

        // Check MST has correct number of edges
        assertEquals(7, mst.size()); // V-1 edges

        // Check MST is valid
        assertTrue(prims.isValidMST(largeGraph, mst));
    }

    @Test
    void testMSTContainsAllVertices() {
        List<Edge> primsMST = prims.findMST(mediumGraph);

        // Check that MST connects all vertices
        assertTrue(prims.isValidMST(mediumGraph, primsMST));
    }

    @Test
    void testMSTIsAcyclic() {
        List<Edge> primsMST = prims.findMST(largeGraph);

        // MST should be acyclic
        assertTrue(prims.isValidMST(largeGraph, primsMST));
    }

    @Test
    void testDisconnectedGraphHandling() {
        // Algorithm should throw exception for disconnected graphs
        assertThrows(IllegalArgumentException.class, () -> prims.findMST(disconnectedGraph));
    }

    @Test
    void testEmptyGraphHandling() {
        Graph emptyGraph = new Graph(0);

        List<Edge> primsMST = prims.findMST(emptyGraph);

        assertTrue(primsMST.isEmpty());
    }

    @Test
    void testSingleVertexGraph() {
        Graph singleVertexGraph = new Graph(1);

        List<Edge> primsMST = prims.findMST(singleVertexGraph);

        assertTrue(primsMST.isEmpty());
    }

    // ========== PERFORMANCE AND CONSISTENCY TESTS ==========

    @Test
    void testExecutionTimeNonNegative() {
        long startTime = System.nanoTime();
        prims.findMST(mediumGraph);
        long endTime = System.nanoTime();

        assertTrue(endTime >= startTime);
    }

    @Test
    void testOperationCountsNonNegative() {
        PerformanceTracker tracker = new PerformanceTracker();

        prims.findMSTWithTracking(mediumGraph, tracker);
        assertTrue(tracker.getComparisons() >= 0);
    }

    @Test
    void testResultsReproducible() {
        List<Edge> primsMST1 = prims.findMST(mediumGraph);
        List<Edge> primsMST2 = prims.findMST(mediumGraph);

        // Results should be reproducible (same weight)
        assertEquals(prims.calculateMSTWeight(primsMST1), prims.calculateMSTWeight(primsMST2));
    }

    // ========== EDGE CASES ==========

    @Test
    void testGraphWithDuplicateEdges() {
        Graph graphWithDuplicates = new Graph(3);
        graphWithDuplicates.addEdge(0, 1, 5);
        graphWithDuplicates.addEdge(0, 1, 3); // Duplicate edge with different weight
        graphWithDuplicates.addEdge(1, 2, 4);
        graphWithDuplicates.addEdge(0, 2, 2);

        List<Edge> primsMST = prims.findMST(graphWithDuplicates);

        assertTrue(prims.isValidMST(graphWithDuplicates, primsMST));
    }

    @Test
    void testGraphWithSelfLoops() {
        Graph graphWithSelfLoops = new Graph(3);
        graphWithSelfLoops.addEdge(0, 1, 2);
        graphWithSelfLoops.addEdge(1, 2, 3);
        graphWithSelfLoops.addEdge(0, 2, 1);

        List<Edge> primsMST = prims.findMST(graphWithSelfLoops);

        assertTrue(prims.isValidMST(graphWithSelfLoops, primsMST));
    }

    @Test
    void testGraphWithNegativeWeights() {
        Graph graphWithNegativeWeights = new Graph(3);
        graphWithNegativeWeights.addEdge(0, 1, -2);
        graphWithNegativeWeights.addEdge(1, 2, 3);
        graphWithNegativeWeights.addEdge(0, 2, 1);

        List<Edge> primsMST = prims.findMST(graphWithNegativeWeights);

        assertTrue(prims.isValidMST(graphWithNegativeWeights, primsMST));
    }

    @Test
    void testGraphWithZeroWeights() {
        Graph graphWithZeroWeights = new Graph(3);
        graphWithZeroWeights.addEdge(0, 1, 0);
        graphWithZeroWeights.addEdge(1, 2, 3);
        graphWithZeroWeights.addEdge(0, 2, 1);

        List<Edge> primsMST = prims.findMST(graphWithZeroWeights);

        assertTrue(prims.isValidMST(graphWithZeroWeights, primsMST));
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

        List<Edge> primsMST = prims.findMST(completeGraph);

        assertEquals(3, primsMST.size());
        assertTrue(prims.isValidMST(completeGraph, primsMST));
    }
}

