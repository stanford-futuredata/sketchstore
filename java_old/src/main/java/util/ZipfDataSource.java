package util;

import org.apache.commons.math3.distribution.ZipfDistribution;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;

import java.io.IOException;
import java.util.ArrayList;

public class ZipfDataSource implements DataSource<Integer>
{
  private int UNIVERSE_SIZE = 10_000_000;
  private RandomGenerator rng;
  private ZipfDistribution zipf;
  private int length;

  public ZipfDataSource(double exponent, int seed, int length) {
    rng = new JDKRandomGenerator(seed);
    zipf = new ZipfDistribution(rng, UNIVERSE_SIZE, exponent);
    this.length = length;
  }

  @Override
  public ArrayList<Integer> get()
  {
    ArrayList<Integer> output = new ArrayList<>(length);
    for (int i = 0; i < length; i++) {
      output.add(zipf.sample());
    }
    return output;
  }
}
