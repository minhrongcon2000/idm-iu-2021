import classifier.PredictiveFPTree;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

import java.io.File;
import java.util.Arrays;
import java.util.Random;

public class ClusterEvaluation {

    public static void main(String[] args) throws Exception {
        System.out.println("Load data...");


        int max_iter = 10;
        ArffLoader dataLoader;
        Instances data;
        Evaluation evaluation;
        PredictiveFPTree tree;

        dataLoader = new ArffLoader();
        dataLoader.setSource(new File("data/cluster1_trans.arff"));
        data = dataLoader.getDataSet();
        int group1Instances = data.numInstances();
        double avg_score_group1 = 0.0;
        System.out.println("Evaluate on cluster 1...");
        System.out.println("===========================");
        for (int i = 0; i < max_iter; ++i) {
            System.out.println((i+1) + "Class: " + data.attribute(i).name());
            data.setClassIndex(i);
            tree = new PredictiveFPTree();
            tree.setMinMetrics(0.0001);


            evaluation = new Evaluation(data);
            evaluation.crossValidateModel(tree, data, 10, new Random(43));
            avg_score_group1 += (1.0 / max_iter) * evaluation.pctCorrect();

            System.out.println(evaluation.toSummaryString());
            System.out.println(evaluation.toClassDetailsString());
            System.out.println(Arrays.deepToString(evaluation.confusionMatrix()));
        }
        System.out.println(group1Instances);
        System.out.println(avg_score_group1);

        dataLoader = new ArffLoader();
        dataLoader.setSource(new File("data/cluster2_trans.arff"));
        data = dataLoader.getDataSet();
        int group2Instances = data.numInstances();
        double avg_score_group2 = 0.0;
        System.out.println("Evaluate on cluster 2...");
        System.out.println("===========================");
        for (int i = 0; i < max_iter; ++i) {
            System.out.println((i+1) + "Class: " + data.attribute(i).name());
            data.setClassIndex(i);
            tree = new PredictiveFPTree();
            tree.setMinMetrics(0.005);


            evaluation = new Evaluation(data);
            evaluation.crossValidateModel(tree, data, 10, new Random(43));
            avg_score_group2 += (1.0 / max_iter) * evaluation.pctCorrect();

            System.out.println(evaluation.toSummaryString());
            System.out.println(evaluation.toClassDetailsString());
            System.out.println(Arrays.deepToString(evaluation.confusionMatrix()));
        }
        System.out.println(group2Instances);
        System.out.println(avg_score_group2);

        dataLoader = new ArffLoader();
        dataLoader.setSource(new File("data/cluster3_trans.arff"));
        data = dataLoader.getDataSet();
        double avg_score_group3 = 0.0;
        int group3Instances = data.numInstances();
        System.out.println("Evaluate on cluster 3...");
        System.out.println("===========================");
        for (int i = 0; i < max_iter; ++i) {
            System.out.println((i+1) + "Class: " + data.attribute(i).name());
            data.setClassIndex(i);
            tree = new PredictiveFPTree();
            tree.setMinMetrics(0.005);


            evaluation = new Evaluation(data);
            evaluation.crossValidateModel(tree, data, 10, new Random(43));
            avg_score_group3 += (1.0 / max_iter) * evaluation.pctCorrect();

            System.out.println(evaluation.toSummaryString());
            System.out.println(evaluation.toClassDetailsString());
            System.out.println(Arrays.deepToString(evaluation.confusionMatrix()));
        }
        System.out.println(group3Instances);
        System.out.println(avg_score_group3);

        int totalData = group1Instances + group2Instances + group3Instances;

        double avg_acc = (group1Instances * 1.0 / totalData) * avg_score_group1
                + (group2Instances * 1.0 / totalData) * avg_score_group2
                + (group3Instances * 1.0 / totalData) * avg_score_group3;
        System.out.println("===============================");
        System.out.println("Average acc: " + avg_acc);
    }
}
