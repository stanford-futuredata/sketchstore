package summary.custom;

import summary.Sketch;

public class CMSSketch implements Sketch<Long> {
    PatchedCountMinSketch sketch;
    int width;

    public CMSSketch(PatchedCountMinSketch sketch, int width) {
        this.sketch = sketch;
        this.width = width;
    }

    @Override
    public String name() {
        return "cms_min";
    }

    @Override
    public int size() {
        return width;
    }

    @Override
    public Sketch<Long> merge(Sketch<Long> other) {
        CMSSketch oSketch = (CMSSketch) other;
        sketch.merge(oSketch.sketch);
        return this;
    }

    @Override
    public double estimate(Long x) {
        return sketch.estimateCount(x);
    }
}
