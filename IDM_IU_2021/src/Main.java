import classifier.PredictiveFPTree;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

import java.io.File;
import java.util.Arrays;
import java.util.Random;

public class Main {
    public static void main(String[] args) throws Exception {
        ArffLoader dataLoader = new ArffLoader();
        dataLoader.setSource(new File("/Users/HoangMinh/Desktop/demo/demo.arff"));
        Instances data = dataLoader.getDataSet();
        data.setClassIndex(0);

        PredictiveFPTree tree = new PredictiveFPTree();

        Evaluation evaluation = new Evaluation(data);
        evaluation.crossValidateModel(tree, data, 10, new Random(43));

        System.out.println(evaluation.toSummaryString());
        System.out.println(evaluation.toClassDetailsString());
        System.out.println(Arrays.deepToString(evaluation.confusionMatrix()));
    }
}
