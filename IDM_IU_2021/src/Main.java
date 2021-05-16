import arm.FPGrowthAssociation;
import weka.associations.FPGrowth;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

import java.io.File;

public class Main {
    public static void main(String[] args) throws Exception {
        ArffLoader dataLoader = new ArffLoader();
        dataLoader.setSource(new File("IDM_IU_2021/data/data.arff"));
        Instances data = dataLoader.getDataSet();
        dataLoader.setSource(new File("IDM_IU_2021/data/Clustered.arff"));
        Instances clusters = dataLoader.getDataSet();

        var fpg = new FPGrowthAssociation(data, clusters);
        fpg.displayAssociationRules();
//        data.setClassIndex(0);

//        PredictiveFPTree tree = new PredictiveFPTree();
//
//        Evaluation evaluation = new Evaluation(data);
//        evaluation.crossValidateModel(tree, data, 10, new Random(43));



//        System.out.println(evaluation.toSummaryString());
//        System.out.println(evaluation.toClassDetailsString());
//        System.out.println(Arrays.deepToString(evaluation.confusionMatrix()));
//
//        // call this to cluster
//        KMeansClusterer kMeansClusterer=new KMeansClusterer();
//        kMeansClusterer.buildCluster();
    }
}
