import arm.FPTree;
import classifier.PredictiveFPTree;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.evaluation.Evaluation;
import weka.core.Instances;

import java.util.Arrays;
import java.util.Random;

public class ModelEvaluation {
    private int maxIter;
    private String positiveLabel;
    private final int minSup;
    private double minConf;
    private int minItemSetLen;
    private double avg_score;

    public ModelEvaluation(int maxIter, String positiveLabel, int minSup, double minConf, int minItemSetLen) {
        this.maxIter = maxIter;
        this.positiveLabel = positiveLabel;
        this.minSup = minSup;
        this.minConf = minConf;
        this.minItemSetLen = minItemSetLen;
        this.avg_score = 0.0d;
    }

    public ModelEvaluation() {
        this(10, "TRUE", 1000, 0.1, 2);
    }

    public void evaluate(Instances data) throws Exception {
        for (int i=0; i<this.maxIter; ++i) {
            System.out.println((i+1) + ": Evaluation on item " + data.attribute(i).name());
            data.setClassIndex(i);

            PredictiveFPTree tree = new PredictiveFPTree();
            tree.setPositiveLabel(this.positiveLabel);
            tree.setMinSup(this.minSup);
            tree.setMinConfidence(this.minConf);
            tree.setMinItemSetLen(this.minItemSetLen);

            Evaluation evaluation = new Evaluation(data);
            evaluation.crossValidateModel(tree, data, 10, new Random(43));

            this.avg_score += (1.0 / this.maxIter) * evaluation.pctCorrect();

            System.out.println(evaluation.toSummaryString());
            System.out.println(evaluation.toClassDetailsString());
            System.out.println(Arrays.deepToString(evaluation.confusionMatrix()));
        }
    }

    public double getAccuracy() {
        return this.avg_score;
    }

    public void setMaxIter(int maxIter) {
        this.maxIter = maxIter;
    }

    public void setPositiveLabel(String positiveLabel) {
        this.positiveLabel = positiveLabel;
    }

    public void setMinConf(double minConf) {
        this.minConf = minConf;
    }

    public void setMinItemSetLen(int minItemSetLen) {
        this.minItemSetLen = minItemSetLen;
    }
}
