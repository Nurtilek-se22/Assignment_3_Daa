package algorithms;

import java.util.*;

public class Graph {
    private int vertices;
    private List<Edge> edges;
    private List<List<Edge>> adjacencyList;

    public Graph(int vertices) {
        this.vertices = vertices;
        this.edges = new ArrayList<>();
        this.adjacencyList = new ArrayList<>();

        // Initialize adjacency list
        for (int i = 0; i < vertices; i++) {
            adjacencyList.add(new ArrayList<>());
        }
    }
    public void addEdge(int from, int to, int weight) {
        Edge edge = new Edge(from, to, weight);
        edges.add(edge);

        // Add to adjacency list (undirected graph)
        adjacencyList.get(from).add(edge);
        adjacencyList.get(to).add(edge);
    }

    public List<Edge> getEdges() {
        return new ArrayList<>(edges);
    }

    public List<Edge> getAdjacentEdges(int vertex) {
        return new ArrayList<>(adjacencyList.get(vertex));
    }

    public int getVertices() {
        return vertices;
    }

    public int getEdgesCount() {
        return edges.size();
    }

    public boolean isConnected() {
        if (vertices == 0) return true;

        boolean[] visited = new boolean[vertices];
        dfs(0, visited);

        for (boolean v : visited) {
            if (!v) return false;
        }
        return true;
    }

    private void dfs(int vertex, boolean[] visited) {
        visited[vertex] = true;

        for (Edge edge : adjacencyList.get(vertex)) {
            int nextVertex = (edge.getFrom() == vertex) ? edge.getTo() : edge.getFrom();
            if (!visited[nextVertex]) {
                dfs(nextVertex, visited);
            }
        }
    }

    public Set<Integer> getAllVertices() {
        Set<Integer> vertexSet = new HashSet<>();
        for (Edge edge : edges) {
            vertexSet.add(edge.getFrom());
            vertexSet.add(edge.getTo());
        }
        return vertexSet;
    }

    public Graph createSubgraph(List<Edge> selectedEdges) {
        Set<Integer> verticesInSubgraph = new HashSet<>();
        for (Edge edge : selectedEdges) {
            verticesInSubgraph.add(edge.getFrom());
            verticesInSubgraph.add(edge.getTo());
        }

        Graph subgraph = new Graph(verticesInSubgraph.size());
        Map<Integer, Integer> vertexMapping = new HashMap<>();

        int newVertexId = 0;
        for (int vertex : verticesInSubgraph) {
            vertexMapping.put(vertex, newVertexId++);
        }

        for (Edge edge : selectedEdges) {
            int newFrom = vertexMapping.get(edge.getFrom());
            int newTo = vertexMapping.get(edge.getTo());
            subgraph.addEdge(newFrom, newTo, edge.getWeight());
        }

        return subgraph;
    }

    public int calculateTotalWeight(List<Edge> edges) {
        return edges.stream().mapToInt(Edge::getWeight).sum();
    }

    public boolean isValidMST(List<Edge> mstEdges) {
        if (mstEdges.size() != vertices - 1) {
            return false; // MST should have V-1 edges
        }

        // Check if MST is connected
        Graph mstGraph = createSubgraph(mstEdges);
        if (!mstGraph.isConnected()) {
            return false;
        }

        // Check for cycles using Union-Find
        return !hasCycle(mstEdges);
    }

    private boolean hasCycle(List<Edge> edges) {
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Graph with ").append(vertices).append(" vertices and ").append(edges.size()).append(" edges:\n");
        for (Edge edge : edges) {
            sb.append(edge.toString()).append("\n");
        }
        return sb.toString();
    }
}
