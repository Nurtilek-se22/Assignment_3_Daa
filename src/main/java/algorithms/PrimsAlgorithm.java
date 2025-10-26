package algorithms;

import metrics.PerformanceTracker;
import java.util.*;


public class PrimsAlgorithm {

    public List<Edge> findMST(Graph graph) {
        if (graph == null || graph.getVertices() == 0) {
            return new ArrayList<>();
        }

        if (!graph.isConnected()) {
            throw new IllegalArgumentException("Graph is not connected. MST does not exist.");
        }

        List<Edge> mst = new ArrayList<>();
        boolean[] inMST = new boolean[graph.getVertices()];
        PriorityQueue<Edge> minHeap = new PriorityQueue<>();

        // Start with vertex 0
        inMST[0] = true;

        // Add all edges from vertex 0 to the priority queue
        for (Edge edge : graph.getAdjacentEdges(0)) {
            minHeap.offer(edge);
        }

        // Process edges until we have V-1 edges in MST
        while (!minHeap.isEmpty() && mst.size() < graph.getVertices() - 1) {
            Edge currentEdge = minHeap.poll();

            int u = currentEdge.getFrom();
            int v = currentEdge.getTo();

            // Skip if both vertices are already in MST (would create cycle)
            if (inMST[u] && inMST[v]) {
                continue;
            }

            // Add edge to MST
            mst.add(currentEdge);

            // Mark the new vertex as in MST
            int newVertex = inMST[u] ? v : u;
            inMST[newVertex] = true;

            // Add all edges from the new vertex to the priority queue
            for (Edge edge : graph.getAdjacentEdges(newVertex)) {
                int otherVertex = edge.getOtherVertex(newVertex);
                if (!inMST[otherVertex]) {
                    minHeap.offer(edge);
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
        boolean[] inMST = new boolean[graph.getVertices()];
        PriorityQueue<Edge> minHeap = new PriorityQueue<>();

        // Start with vertex 0
        inMST[0] = true;
        tracker.incrementArrayAccesses(1);

        // Add all edges from vertex 0 to the priority queue
        for (Edge edge : graph.getAdjacentEdges(0)) {
            minHeap.offer(edge);
            tracker.incrementComparisons(); // Priority queue operations
        }

        // Process edges until we have V-1 edges in MST
        while (!minHeap.isEmpty() && mst.size() < graph.getVertices() - 1) {
            tracker.incrementComparisons(); // Loop condition check

            Edge currentEdge = minHeap.poll();
            tracker.incrementComparisons(); // Priority queue poll operation

            int u = currentEdge.getFrom();
            int v = currentEdge.getTo();

            // Skip if both vertices are already in MST (would create cycle)
            tracker.incrementArrayAccesses(2);
            tracker.incrementComparisons();
            if (inMST[u] && inMST[v]) {
                continue;
            }

            // Add edge to MST
            mst.add(currentEdge);

            // Mark the new vertex as in MST
            int newVertex = inMST[u] ? v : u;
            tracker.incrementArrayAccesses(1);
            inMST[newVertex] = true;

            // Add all edges from the new vertex to the priority queue
            for (Edge edge : graph.getAdjacentEdges(newVertex)) {
                int otherVertex = edge.getOtherVertex(newVertex);
                tracker.incrementArrayAccesses(1);
                tracker.incrementComparisons();
                if (!inMST[otherVertex]) {
                    minHeap.offer(edge);
                    tracker.incrementComparisons(); // Priority queue offer operation
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
