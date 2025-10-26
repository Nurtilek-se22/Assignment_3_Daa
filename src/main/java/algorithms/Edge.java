package algorithms;

import java.util.Objects;


public class Edge implements Comparable<Edge> {
    private int from;
    private int to;
    private int weight;

    public Edge(int from, int to, int weight) {
        this.from = Math.min(from, to); // Ensure consistent ordering
        this.to = Math.max(from, to);
        this.weight = weight;
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }

    public int getWeight() {
        return weight;
    }


    public boolean connects(int vertex1, int vertex2) {
        return (from == vertex1 && to == vertex2) || (from == vertex2 && to == vertex1);
    }

    public int getOtherVertex(int vertex) {
        if (vertex == from) {
            return to;
        } else if (vertex == to) {
            return from;
        } else {
            throw new IllegalArgumentException("Vertex " + vertex + " is not connected by this edge");
        }
    }

    public boolean contains(int vertex) {
        return from == vertex || to == vertex;
    }

    @Override
    public int compareTo(Edge other) {
        return Integer.compare(this.weight, other.weight);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Edge edge = (Edge) obj;
        return from == edge.from && to == edge.to && weight == edge.weight;
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to, weight);
    }

    @Override
    public String toString() {
        return String.format("Edge(%d-%d, weight=%d)", from, to, weight);
    }

    public Edge copy() {
        return new Edge(from, to, weight);
    }

    public boolean isSameEdge(Edge other) {
        return (from == other.from && to == other.to) ||
                (from == other.to && to == other.from);
    }
}
