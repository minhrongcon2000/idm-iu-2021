import classifier.PredictiveFPTree;
import weka.associations.FPGrowth;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

import java.io.File;
import java.util.Arrays;
import java.util.Random;

public class NoClusterEvaluation {
    public static void main(String[] args) throws Exception {
        System.out.println("Load data...");
        ArffLoader dataLoader = new ArffLoader();
        dataLoader.setSource(new File("data/transaction.arff"));
        Instances data = dataLoader.getDataSet();

        double avg_score = 0.0;
        int max_iter = 10;

        for (int i=0; i<max_iter; ++i) {
            System.out.println((i+1) + ": Evaluation on item " + data.attribute(i).name());
            data.setClassIndex(i);
            PredictiveFPTree tree = new PredictiveFPTree();

            Evaluation evaluation = new Evaluation(data);
            evaluation.crossValidateModel(tree, data, 10, new Random(43));

            avg_score += (1.0 / max_iter) * evaluation.pctCorrect();

            System.out.println(evaluation.toSummaryString());
            System.out.println(evaluation.toClassDetailsString());
            System.out.println(Arrays.deepToString(evaluation.confusionMatrix()));
        }
        System.out.println("Average accuracy: ");
        System.out.println(avg_score);
    }
}
