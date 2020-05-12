package runner;

public class Timer {
    public long startTime;
    long totalTime;
    public Timer() {
        totalTime = 0;
        startTime = 0;
    }
    public void reset() {
        totalTime = 0;
        startTime = 0;
    }
    public void start() {
        startTime = System.nanoTime();
    }
    public void end() {
        long curTime = System.nanoTime();
        totalTime += curTime - startTime;
        startTime = curTime;
    }
    public double getTotalMs() {
        return totalTime * 1e-6;
    }
}
