package summary.custom;

import org.apache.datasketches.frequencies.LongsSketch;
import org.apache.datasketches.kll.KllFloatsSketch;
import org.apache.datasketches.memory.Memory;
import summary.Sketch;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class YahooKLLSketch implements Sketch<Double>, Externalizable {
    int size;
    KllFloatsSketch sketch;

    public YahooKLLSketch() {
        size = -1;
        sketch = null;
    }

    public YahooKLLSketch(KllFloatsSketch sketch, int size) {
        this.sketch = sketch;
        this.size = size;
    }

    @Override
    public String name() {
        return "kll";
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public Sketch<Double> merge(Sketch<Double> other) {
        sketch.merge(((YahooKLLSketch)other).sketch);
        return this;
    }

    @Override
    public double estimate(Double x) {
        return sketch.getRank(x.floatValue())*sketch.getN();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(size);
        out.write(sketch.toByteArray());
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        size = in.readInt();
        byte[] bytes = new byte[in.available()];
        in.read(bytes);
        sketch = KllFloatsSketch.heapify(Memory.wrap(bytes));
    }
}
