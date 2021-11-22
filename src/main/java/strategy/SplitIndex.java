package strategy;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.function.Function;

public class SplitIndex {

  public static <T> List<T> execute(List<T> list,
      Function<T, T> op,
      ForkJoinPool forkJoinPool) {
    // Create a new array to hold the results.
    T[] results = (T[]) Array.newInstance(list.get(0).getClass(),
        list.size());

        /*
          This task partitions list recursively and runs each half in
          a ForkJoinTask.  It uses indices to avoid the overhead of
          copying.
         */
    class SplitterTask
        extends RecursiveAction {
      /**
       * The lo index in this partition.
       */
      private int mLo;

      /**
       * The hi index in this partition.
       */
      private final int mHi;

      /**
       * Constructor initializes the fields.
       */
      private SplitterTask(int lo, int hi) {
        mLo = lo;
        mHi = hi;
      }

      /**
       * Recursively perform the computations in parallel using
       * the fork-join pool.
       */
      protected void compute() {
        // Find the midpoint.
        int mid = mLo + mHi >>> 1;

        // If there's just a single element then apply
        // the operation.
        if (mLo == mid) {
          // Update the mLo location with the results of
          // applying the operation.
          results[mLo] = op.apply(list.get(mLo));
        } else {
          // Create a new SplitterTask to handle the
          // left-hand side of the list and fork it.
          ForkJoinTask<Void> leftTask =
              new SplitterTask(mLo, mLo = mid)
                  .fork();

          // Compute the right-hand side in parallel with
          // the left-hand side.
          compute();

          // Join with the left-hand side.  This is a
          // synchronization point.
          leftTask.join();
        }
      }
    }

    // Invoke a new SplitterTask in the fork-join pool.
    forkJoinPool.invoke(new SplitterTask(0, list.size()));

    // Create a list from the array of results and return it.
    return Arrays.asList(results);
  }

}
