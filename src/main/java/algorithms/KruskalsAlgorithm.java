package algorithms;

import metrics.PerformanceTracker;
import java.util.*;

public class KruskalsAlgorithm {

    public List<Edge> findMST(Graph graph) {
        if (graph == null || graph.getVertices() == 0) {
            return new ArrayList<>();
        }

        if (!graph.isConnected()) {
            throw new IllegalArgumentException("Graph is not connected. MST does not exist.");
        }

        List<Edge> mst = new ArrayList<>();
        List<Edge> allEdges = new ArrayList<>(graph.getEdges());

        // Sort edges by weight
        Collections.sort(allEdges);

        // Use Union-Find to detect cycles
        UnionFind uf = new UnionFind(graph.getVertices());

        // Process edges in order of increasing weight
        for (Edge edge : allEdges) {
            int root1 = uf.find(edge.getFrom());
            int root2 = uf.find(edge.getTo());

            // If adding this edge doesn't create a cycle
            if (root1 != root2) {
                mst.add(edge);
                uf.union(edge.getFrom(), edge.getTo());

                // Stop when we have V-1 edges
                if (mst.size() == graph.getVertices() - 1) {
                    break;
                }
            }
        }

        return mst;
    }

    public List<Edge> findMSTWithTracking(Graph graph, PerformanceTracker tracker) {
        if (graph == null || graph.getVertices() == 0) {
            return new ArrayList<>();
        }

        if (!graph.isConnected()) {
            throw new IllegalArgumentException("Graph is not connected. MST does not exist.");
        }

        tracker.incrementComparisons(); // Check for connectivity

        List<Edge> mst = new ArrayList<>();
        List<Edge> allEdges = new ArrayList<>(graph.getEdges());

        // Sort edges by weight
        Collections.sort(allEdges);
        tracker.incrementComparisons(); // Sort operation

        // Use Union-Find to detect cycles
        UnionFind uf = new UnionFind(graph.getVertices());

        // Process edges in order of increasing weight
        for (Edge edge : allEdges) {
            tracker.incrementComparisons(); // Loop iteration

            int root1 = uf.find(edge.getFrom());
            int root2 = uf.find(edge.getTo());

            tracker.incrementComparisons(); // Find operations
            tracker.incrementComparisons(); // Comparison root1 != root2

            // If adding this edge doesn't create a cycle
            if (root1 != root2) {
                mst.add(edge);
                uf.union(edge.getFrom(), edge.getTo());
                tracker.incrementComparisons(); // Union operation

                // Stop when we have V-1 edges
                tracker.incrementComparisons();
                if (mst.size() == graph.getVertices() - 1) {
                    break;
                }
            }
        }

        return mst;
    }

    public int calculateMSTWeight(List<Edge> mstEdges) {
        return mstEdges.stream().mapToInt(Edge::getWeight).sum();
    }

    public boolean isValidMST(Graph graph, List<Edge> mstEdges) {
        if (mstEdges.size() != graph.getVertices() - 1) {
            return false; // MST should have V-1 edges
        }

        // Check if MST is connected
        Graph mstGraph = graph.createSubgraph(mstEdges);
        if (!mstGraph.isConnected()) {
            return false;
        }

        // Check for cycles using Union-Find
        return !hasCycle(mstEdges, graph.getVertices());
    }

    private boolean hasCycle(List<Edge> edges, int vertices) {
        UnionFind uf = new UnionFind(vertices);

        for (Edge edge : edges) {
            int root1 = uf.find(edge.getFrom());
            int root2 = uf.find(edge.getTo());

            if (root1 == root2) {
                return true; // Cycle detected
            }

            uf.union(edge.getFrom(), edge.getTo());
        }

        return false;
    }

    /**
     * Optimized Union-Find implementation with path compression and union by rank
     */
    private static class UnionFind {
        private int[] parent;
        private int[] rank;

        public UnionFind(int n) {
            parent = new int[n];
            rank = new int[n];
            for (int i = 0; i < n; i++) {
                parent[i] = i;
                rank[i] = 0;
            }
        }

        public int find(int x) {
            if (parent[x] != x) {
                parent[x] = find(parent[x]); // Path compression
            }
            return parent[x];
        }

        public void union(int x, int y) {
            int rootX = find(x);
            int rootY = find(y);

            if (rootX == rootY) return;

            // Union by rank
            if (rank[rootX] < rank[rootY]) {
                parent[rootX] = rootY;
            } else if (rank[rootX] > rank[rootY]) {
                parent[rootY] = rootX;
            } else {
                parent[rootY] = rootX;
                rank[rootX]++;
            }
        }

    }
}
