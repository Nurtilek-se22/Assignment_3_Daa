package metrics;

public class PerformanceTracker {
    private long comparisons = 0;
    private long swaps = 0;
    private long arrayAccesses = 0;

    public void incrementComparisons() {
        this.comparisons++;
    }

    public void incrementSwaps() {
        this.swaps++;
    }

    public void incrementArrayAccesses(int count) {
        this.arrayAccesses += count;
    }

    public long getComparisons() {
        return comparisons;
    }

    public long getSwaps() {
        return swaps;
    }

    public void reset() {
        this.comparisons = 0;
        this.swaps = 0;
        this.arrayAccesses = 0;
    }

    @Override
    public String toString() {
        return String.format("Comparisons: %d, Swaps: %d, Array Accesses: %d",
                comparisons, swaps, arrayAccesses);
    }

    public String toCsv() {
        return String.format("%d,%d,%d", comparisons, swaps, arrayAccesses);
    }
}