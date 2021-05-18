import weka.core.Instances;
import weka.core.converters.ArffLoader;

import java.io.File;

public class ClusterEvaluation {
    public static void main(String[] args) throws Exception {
        // cluster 1
        ModelEvaluation evaluator = new ModelEvaluation();
        ArffLoader dataLoader = new ArffLoader();
        dataLoader.setSource(new File("data/cluster1_trans.arff"));
        Instances data = dataLoader.getDataSet();
        evaluator.evaluate(data);
        double acc_c1 = evaluator.getAccuracy();
        int num_c1 = data.numInstances();

        // cluster 2
        evaluator = new ModelEvaluation();
        dataLoader = new ArffLoader();
        dataLoader.setSource(new File("data/cluster2_trans.arff"));
        data = dataLoader.getDataSet();
        evaluator.evaluate(data);
        double acc_c2 = evaluator.getAccuracy();
        int num_c2 = data.numInstances();

        // cluster 3
        evaluator = new ModelEvaluation();
        dataLoader = new ArffLoader();
        dataLoader.setSource(new File("data/cluster3_trans.arff"));
        data = dataLoader.getDataSet();
        evaluator.evaluate(data);
        double acc_c3 = evaluator.getAccuracy();
        int num_c3 = data.numInstances();

        int total_instances = num_c1 + num_c2 + num_c3;
        double avg_acc = 1.0 * num_c1 / total_instances * acc_c1 + 1.0 * num_c2 / total_instances * acc_c2
                + 1.0 * num_c3 / total_instances * acc_c3;
        System.out.println("Overall accuracy: " + avg_acc);
    }
}
