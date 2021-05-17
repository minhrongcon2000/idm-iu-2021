package arm.fpgrowth;

public class FPOptions {
    // default options
    int minSup = 0;
    int minItemSetLen = 2;
    int minItemSetSupport = 1;
    double minConfidence = 0.5;
    String positiveLabel = "y";

    public int getMinItemSetSupport() {
        return minItemSetSupport;
    }

    public void setMinItemSetSupport(int minItemSetSupport) {
        this.minItemSetSupport = minItemSetSupport;
    }

    public int getMinSup() {
        return minSup;
    }

    public void setMinSup(int minSup) {
        this.minSup = minSup;
    }

    public int getMinItemSetLen() {
        return minItemSetLen;
    }

    public void setMinItemSetLen(int minItemSetLen) {
        this.minItemSetLen = minItemSetLen;
    }

    public String getPositiveLabel() {
        return positiveLabel;
    }

    public void setPositiveLabel(String positiveLabel) {
        this.positiveLabel = positiveLabel;
    }

    public double getMinConfidence() {
        return minConfidence;
    }

    public void setMinConfidence(double minConfidence) {
        this.minConfidence = minConfidence;
    }

    public FPOptions() {

    }
}
