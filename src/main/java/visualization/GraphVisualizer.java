package visualization;

import algorithms.Graph;
import algorithms.Edge;
import algorithms.PrimsAlgorithm;
import algorithms.KruskalsAlgorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class GraphVisualizer {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) {
        try {
            System.out.println("Generating graphs for all input datasets...");

            // Load input data
            JsonNode inputData = objectMapper.readTree(new File("performance-data/assign_3_input.json"));
            JsonNode graphs = inputData.get("graphs");

            // Create output directory
            File outputDir = new File("performance-data/graphs");
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }

            // Generate graph for each dataset
            for (JsonNode graphNode : graphs) {
                int graphId = graphNode.get("id").asInt();
                int vertices = graphNode.get("vertices").asInt();
                JsonNode edgesNode = graphNode.get("edges");

                System.out.println("Generating graph for dataset " + graphId + " with " + vertices + " vertices...");

                // Create graph structure
                Graph graph = createGraphFromJson(vertices, edgesNode);

                // Generate visualization
                generateGraphVisualization(graph, graphId, outputDir);
            }

            System.out.println("All graphs generated successfully in performance-data/graphs/");

        } catch (IOException e) {
            System.err.println("Error generating graphs: " + e.getMessage());
            e.printStackTrace();
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

    private static void generateGraphVisualization(Graph graph, int graphId, File outputDir) {
        try {
            // Create MST using both algorithms
            PrimsAlgorithm prims = new PrimsAlgorithm();
            KruskalsAlgorithm kruskals = new KruskalsAlgorithm();

            List<Edge> primsMST = prims.findMST(graph);
            List<Edge> kruskalsMST = kruskals.findMST(graph);

            // Generate only network graph visualization
            generateNetworkGraphVisualization(graph, primsMST, graphId, "mst", outputDir);

        } catch (Exception e) {
            System.err.println("Error generating visualization for graph " + graphId + ": " + e.getMessage());
        }
    }

    private static void generateNetworkGraphVisualization(Graph inputGraph, List<Edge> mst,
                                                          int graphId, String algorithmName, File outputDir) {
        try {
            // Create set of MST edges for quick lookup
            Set<String> mstEdges = new HashSet<>();
            Set<Integer> mstNodes = new HashSet<>();
            for (Edge edge : mst) {
                String edgeId1 = edge.getFrom() + "-" + edge.getTo();
                String edgeId2 = edge.getTo() + "-" + edge.getFrom();
                mstEdges.add(edgeId1);
                mstEdges.add(edgeId2);
                mstNodes.add(edge.getFrom());
                mstNodes.add(edge.getTo());
            }

            // Create image directly with Java2D (no GUI!)
            int width = 1200;
            int height = 900;
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();

            // Enable antialiasing
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // Background color
            g2d.setColor(new java.awt.Color(107, 142, 158)); // #6B8E9E
            g2d.fillRect(0, 0, width, height);

            // Calculate node positions (circular layout)
            int centerX = width / 2;
            int centerY = height / 2;
            int radius = Math.min(width, height) / 2 - 100;

            double[][] nodePositions = new double[inputGraph.getVertices()][2];
            double angleStep = 2 * Math.PI / inputGraph.getVertices();

            for (int i = 0; i < inputGraph.getVertices(); i++) {
                double angle = i * angleStep - Math.PI / 2; // Start from top
                nodePositions[i][0] = centerX + radius * Math.cos(angle);
                nodePositions[i][1] = centerY + radius * Math.sin(angle);
            }

            // Draw edges first (so they appear behind nodes)
            g2d.setStroke(new java.awt.BasicStroke(3));
            for (Edge edge : inputGraph.getEdges()) {
                int from = edge.getFrom();
                int to = edge.getTo();
                String edgeKey = from + "-" + to;

                // Check if this edge is in MST
                boolean isMST = mstEdges.contains(edgeKey);

                // Set edge color
                if (isMST) {
                    g2d.setColor(new java.awt.Color(255, 107, 107)); // Red for MST
                    g2d.setStroke(new java.awt.BasicStroke(4));
                } else {
                    g2d.setColor(new java.awt.Color(102, 102, 102)); // Gray for non-MST
                    g2d.setStroke(new java.awt.BasicStroke(3));
                }

                // Draw line
                g2d.drawLine(
                        (int)nodePositions[from][0], (int)nodePositions[from][1],
                        (int)nodePositions[to][0], (int)nodePositions[to][1]
                );

                // Draw weight label
                int midX = (int)((nodePositions[from][0] + nodePositions[to][0]) / 2);
                int midY = (int)((nodePositions[from][1] + nodePositions[to][1]) / 2);

                g2d.setColor(java.awt.Color.WHITE);
                g2d.fillRect(midX - 20, midY - 12, 40, 24);
                g2d.setColor(java.awt.Color.BLACK);
                g2d.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 16));
                String weightStr = String.valueOf(edge.getWeight());
                java.awt.FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(weightStr);
                g2d.drawString(weightStr, midX - textWidth/2, midY + 6);
            }

            // Draw nodes
            int nodeSize = 40;
            for (int i = 0; i < inputGraph.getVertices(); i++) {
                int x = (int)nodePositions[i][0];
                int y = (int)nodePositions[i][1];

                // Check if node is in MST
                boolean isMST = mstNodes.contains(i);

                // Draw node circle
                if (isMST) {
                    g2d.setColor(new java.awt.Color(255, 215, 0)); // Gold for MST nodes
                } else {
                    g2d.setColor(java.awt.Color.WHITE);
                }
                g2d.fillOval(x - nodeSize/2, y - nodeSize/2, nodeSize, nodeSize);

                // Draw node border
                g2d.setColor(new java.awt.Color(51, 51, 51));
                g2d.setStroke(new java.awt.BasicStroke(2));
                g2d.drawOval(x - nodeSize/2, y - nodeSize/2, nodeSize, nodeSize);

                // Draw node label
                g2d.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 20));
                String label = String.valueOf(i + 1); // 1-indexed
                java.awt.FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(label);
                int textHeight = fm.getAscent();
                g2d.drawString(label, x - textWidth/2, y + textHeight/2 - 2);
            }

            g2d.dispose();

            // Save image
            File outputFile = new File(outputDir, "graph_" + graphId + "_" + algorithmName + "_network.png");
            ImageIO.write(image, "PNG", outputFile);
            System.out.println("Saved network visualization: " + outputFile.getName());

        } catch (Exception e) {
            System.err.println("Error generating network visualization: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void generatePerformanceChart(Graph graph, List<Edge> primsMST, List<Edge> kruskalsMST, int graphId, File outputDir) {
        try {
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();

            // Add data points
            dataset.addValue(graph.getVertices(), "Vertices", "Graph " + graphId);
            dataset.addValue(graph.getEdgesCount(), "Edges", "Graph " + graphId);
            PrimsAlgorithm prims = new PrimsAlgorithm();
            KruskalsAlgorithm kruskals = new KruskalsAlgorithm();

            dataset.addValue(prims.calculateMSTWeight(primsMST), "Prim's MST Weight", "Graph " + graphId);
            dataset.addValue(kruskals.calculateMSTWeight(kruskalsMST), "Kruskal's MST Weight", "Graph " + graphId);

            JFreeChart chart = ChartFactory.createBarChart(
                    "Graph " + graphId + " Analysis",
                    "Metrics",
                    "Values",
                    dataset,
                    PlotOrientation.VERTICAL,
                    true,
                    true,
                    false
            );

            // Save chart
            File chartFile = new File(outputDir, "graph_" + graphId + "_analysis.png");
            ChartUtils.saveChartAsPNG(chartFile, chart, 800, 600);

        } catch (IOException e) {
            System.err.println("Error creating performance chart for graph " + graphId + ": " + e.getMessage());
        }
    }

    private static void generateMSTStructureChart(List<Edge> primsMST, List<Edge> kruskalsMST, int graphId, File outputDir) {
        try {
            // Create dataset for MST edges comparison
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();

            // Add edge data
            for (int i = 0; i < Math.max(primsMST.size(), kruskalsMST.size()); i++) {
                if (i < primsMST.size()) {
                    Edge edge = primsMST.get(i);
                    dataset.addValue(edge.getWeight(), "Prim's MST", "Edge " + (i + 1));
                }
                if (i < kruskalsMST.size()) {
                    Edge edge = kruskalsMST.get(i);
                    dataset.addValue(edge.getWeight(), "Kruskal's MST", "Edge " + (i + 1));
                }
            }

            JFreeChart chart = ChartFactory.createBarChart(
                    "Graph " + graphId + " MST Structure Comparison",
                    "Edges",
                    "Weight",
                    dataset,
                    PlotOrientation.VERTICAL,
                    true,
                    true,
                    false
            );

            // Save chart
            File chartFile = new File(outputDir, "graph_" + graphId + "_mst_structure.png");
            ChartUtils.saveChartAsPNG(chartFile, chart, 1000, 600);

        } catch (IOException e) {
            System.err.println("Error creating MST structure chart for graph " + graphId + ": " + e.getMessage());
        }
    }
}
