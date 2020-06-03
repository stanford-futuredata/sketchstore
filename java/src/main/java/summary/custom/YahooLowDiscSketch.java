package summary.custom;

import org.apache.datasketches.memory.Memory;
import org.apache.datasketches.quantiles.DoublesSketch;
import org.apache.datasketches.quantiles.DoublesUnion;
import org.apache.datasketches.quantiles.UpdateDoublesSketch;
import summary.Sketch;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class YahooLowDiscSketch implements Sketch<Double>, Externalizable {
    int size;
    DoublesSketch sketch;

    public YahooLowDiscSketch() {
        size = -1;
        sketch = null;
    }

    public YahooLowDiscSketch(DoublesSketch sketch, int size) {
        this.sketch = sketch;
        this.size = size;
    }

    @Override
    public String name() {
        return "low_discrepancy";
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public Sketch<Double> merge(Sketch<Double> other) {
        assert(other instanceof YahooLowDiscSketch);
        YahooLowDiscSketch otherSketch = (YahooLowDiscSketch)other;
        DoublesUnion union = DoublesUnion.heapify(sketch);
        union.update(otherSketch.sketch);
        sketch = union.getResult();
        return this;
    }

    @Override
    public double estimate(Double x) {
        return sketch.getRank(x)*sketch.getN();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(size);
        out.write(sketch.toByteArray(true));
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        size = in.readInt();
        byte[] bytes = new byte[in.available()];
        in.read(bytes);
        sketch = UpdateDoublesSketch.heapify(Memory.wrap(bytes));
    }
}
