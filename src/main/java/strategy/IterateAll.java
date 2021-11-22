package strategy;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.function.Function;

public class IterateAll {

  public static <T> List<T> execute(List<T> fractionList, Function<T, T> op,
      ForkJoinPool pool) {

    return pool.invoke(new RecursiveTask<List<T>>() {
      /**
       * Entry point into the new task.
       */
      protected List<T> compute() {
        // Create a list to hold the forked tasks.
        List<ForkJoinTask<T>> forks =
            new LinkedList<>();

        // Create a list to hold the joined results.
        List<T> results =
            new LinkedList<>();

        // Iterate through list, fork all the tasks,
        // and add them to the forks list.
        for (T t : fractionList)
          // Add each new task to the forks list.
          forks.add(new RecursiveTask<T>() {
            /**
             * Apply the operation.
             */
            protected T compute() {
              return op.apply(t);
            }
          }
              // Fork a new task.
              .fork());

        // Join all the results of the forked tasks.
        for (ForkJoinTask<T> task : forks)
          // Add the joined results.
          results.add(task.join());

        // Return the results.
        return results;
      }
    });
  }

}
