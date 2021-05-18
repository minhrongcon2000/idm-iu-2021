import weka.core.Instances;
import weka.core.converters.ArffLoader;

import java.io.File;

public class NoClusterEvaluation {
    public static void main(String[] args) throws Exception {
        ArffLoader dataLoader = new ArffLoader();
        dataLoader.setSource(new File("data/transaction.arff"));
        Instances data = dataLoader.getDataSet();
        ModelEvaluation evaluator = new ModelEvaluation();
        evaluator.evaluate(data);
        System.out.println("Overall accuracy: " + evaluator.getAccuracy());
    }
}
