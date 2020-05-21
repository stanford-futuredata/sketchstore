package summary.custom;

import org.apache.datasketches.frequencies.LongsSketch;
import org.apache.datasketches.memory.Memory;
import summary.Sketch;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class YahooMGSketch implements Sketch<Long>, Externalizable {
    LongsSketch sketch;
    int size;

    // Needed for external serialization
    public YahooMGSketch() {
    }

    public YahooMGSketch(LongsSketch sketch, int size) {
        this.sketch = sketch;
        this.size = size;
    }

    @Override
    public String name() {
        return "yahoo_mg";
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public Sketch<Long> merge(Sketch<Long> other) {
        YahooMGSketch otherYahoo = (YahooMGSketch) other;
        sketch.merge(otherYahoo.sketch);
        return this;
    }

    @Override
    public double estimate(Long x) {
        return sketch.getEstimate(x);
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(size);
        out.write(sketch.toByteArray());
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException {
        size = in.readInt();
        byte[] bytes = new byte[in.available()];
        in.read(bytes);
        sketch = LongsSketch.getInstance(Memory.wrap(bytes));
    }
}
