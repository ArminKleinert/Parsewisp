package alphaparse.testutil;

import alphaparse.functions.Procedure;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.stream.DoubleStream;

/**
 * Utilities for measuring runtime performance.
 */
public final class TimeUtil {
    private TimeUtil() {
    }

    /**
     * Runs a procedure a few times and creates some statistics. Time is represented as milliseconds.
     *
     * @param n How often to run the procedure.
     * @param f The procedure.
     * @return The analysis result as a string.
     */
    public static @NotNull String measureTimeMillis(final int n, final @NotNull Procedure f) {
        if (n < 1)
            throw new IllegalArgumentException();

        final double[] stream = DoubleStream.generate(() -> {
            final long start = System.nanoTime();
            f.execute();
            final long end = System.nanoTime();
            return (end - start) / 1000000.0;
        }).limit(n).sorted().toArray();

        final double min = stream[0];
        final double max = stream[stream.length - 1];
        final double diff = max - min;
        final double sum = Arrays.stream(stream).sum();
        final double avg = sum / n;
        final double mid = stream[stream.length / 2];
        final double median = (stream[n / 4] + stream[(n / 4) * 3]) / 2;

        return String.format("{:lowest %.3f, :highest %.3f, :diff %.3f, :average %.3f, :mid %.3f, :median %.3f, :total %.3f}",
                min, max, diff, avg, mid, median, sum);
    }

    /**
     * Helper method for tests.
     * @param n Number of iterations.
     * @param f Code to run.
     * @return The average time in milliseconds. Outlier times are removed.
     */
    public static @NotNull double measureTimeMillisMean(final int n, final @NotNull Procedure f) {
        if (n < 1)
            throw new IllegalArgumentException();

        final double[] stream = DoubleStream.generate(() -> {
            final long start = System.nanoTime();
            f.execute();
            final long end = System.nanoTime();
            return (end - start) / 1000000.0;
        }).limit(n).sorted().toArray();

        var factor = 0.1;
        var keep = (long) Math.ceil((1 - factor) * stream.length);
        return Arrays.stream(stream).skip((long) Math.floor(stream.length * factor) / 2).limit(keep).sum() / keep;
    }

    /**
     * Another test method.
     *
     * @param milliseconds Time limit.
     * @param f            Code to execute.
     * @return Number of executions within the time limit.
     */
    public static @NotNull String measureExecutionsPer(final long milliseconds, final @NotNull Procedure f) {
        final long startTime = System.nanoTime();
        final long endTime = startTime + milliseconds * 1000000;
        long last;
        long executions = 0;

        while ((last = System.nanoTime()) < endTime) {
            f.execute();
            executions++;
        }

        final long actualDuration = last - startTime;

        return "Executions: " + executions + "; Actual duration (ms): " + (actualDuration / 1000000.0);
    }
}
