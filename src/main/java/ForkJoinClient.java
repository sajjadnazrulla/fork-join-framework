import static java.util.stream.Collectors.toList;

import java.math.BigInteger;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;
import java.util.stream.Stream;
import strategy.IterateAll;
import strategy.SplitAll;
import strategy.SplitIndex;
import utils.BigFraction;

public class ForkJoinClient {

  private static final int sMAX_FRACTIONS = 50;

  List<BigFraction> fractionList;

  private BigFraction sBigReducedFraction;

  private Function<BigFraction, BigFraction> op;

  private ForkJoinClient(){
    this.fractionList = Stream
        .generate(() -> makeBigFraction(new Random(), false))
        .limit(sMAX_FRACTIONS)
        .collect(toList());
    this.sBigReducedFraction =
        BigFraction.valueOf(new BigInteger("846122553600669882"),
            new BigInteger("188027234133482196"),
            true);
    op = bigFraction -> BigFraction
        // Reduce the big fraction.
        .reduce(bigFraction)

        // Multiply it by the constant.
        .multiply(sBigReducedFraction);
  }



  public static void main(String[] args) {
    ForkJoinClient client = new ForkJoinClient();
    System.gc();

//    client.runTaskInPoolWithIterateAllStrategy(new ForkJoinPool());
    client.runTaskInPoolWithSplitAllStrategy(new ForkJoinPool());
//    client.runTaskInPoolWithSplitIndexStrategy(new ForkJoinPool());

  }

  private static void printMetrics(String strategy, ForkJoinPool pool, long startTime) {
    System.out.println(strategy);
    System.out.println("Time Taken = " + getTimeTaken(startTime));
    System.out.println("Steal count = "
        + pool.getStealCount());
    System.out.println("---------------------");
  }

  private static long getTimeTaken(long startTime) {
    return System.currentTimeMillis() - startTime;
  }


  public void runTaskInPoolWithSplitAllStrategy(ForkJoinPool pool) {
    long startTime = System.currentTimeMillis();
    SplitAll.execute(fractionList,
        op,
        pool);
    printMetrics("SplitAll", pool, startTime);
  }

  public void runTaskInPoolWithSplitIndexStrategy(ForkJoinPool pool) {
    long startTime = System.currentTimeMillis();
    SplitIndex.execute(fractionList,
        op,
        pool);
    printMetrics("SplitIndex", pool, startTime);
  }

  public void runTaskInPoolWithIterateAllStrategy(ForkJoinPool pool) {
    long startTime = System.currentTimeMillis();
    IterateAll.execute(fractionList,
        op,
        pool);
    printMetrics("IterateAll", pool, startTime);
  }


  private BigFraction makeBigFraction(Random random,
      boolean reduced) {
    // Create a large random big integer.
    BigInteger numerator =
        new BigInteger(150000, random);

    // Create a denominator that's between 1 to 10 times smaller
    // than the numerator.
    BigInteger denominator =
        numerator.divide(BigInteger.valueOf(random.nextInt(10) + 1));

    // Return a big fraction.
    return BigFraction.valueOf(numerator,
        denominator,
        reduced);
  }
}
