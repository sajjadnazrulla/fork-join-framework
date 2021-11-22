package strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.function.Function;

public class SplitAll {

  public static <T> List<T> execute(List<T> list,
      Function<T, T> op,
      ForkJoinPool forkJoinPool) {
        /*
          This task splits up the list recursively and runs
          each half in a ForkJoinTask.
         */
    class SplitterTask
        extends RecursiveTask<List<T>> {
      /**
       * A reference to a portion of the original list.
       */
      private List<T> mList;

      /**
       * Constructor initializes the field.
       */
      private SplitterTask(List<T> list) {
        mList = list;
      }

      /**
       * Recursively perform the computations in parallel using
       * the fork-join pool.
       */
      protected List<T> compute() {
        // The base case for the recursion.
        if (mList.size() <= 1) {
          // Create a new list to hold the result (if any).
          List<T> result = new ArrayList<>();

          // Iterate through the list.
          for (T t : mList)
            // Apply the operation and add the result to
            // the result list.
            result.add(op.apply(t));

          // Return the result list.
          return result;
        } else {
          // Determine the midpoint of the list.
          int mid = mList.size() / 2;

          // Create a new SplitterTask to handle the
          // left-hand side of the list and fork it.
          ForkJoinTask<List<T>> leftTask =
              new SplitterTask(mList.subList(0,
                  mid))
                  .fork();

          // Update mList to handle the right-hand side of
          // the list.
          mList = mList.subList(mid, mList.size());

          // Compute the right-hand side recursively.
          List<T> rightResult = compute();

          // Join the left-hand side results.
          List<T> leftResult = leftTask.join();

          // Combine the left-hand and the right-hand side
          // results.
          leftResult.addAll(rightResult);

          // Return the joined results.
          return leftResult;
        }
      }
    }

    // Invoke a new SpliterTask in the fork-join pool.
    return forkJoinPool.invoke(new SplitterTask(list));
  }

}
