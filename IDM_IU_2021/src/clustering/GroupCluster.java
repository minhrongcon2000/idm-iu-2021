package clustering;

import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

import java.io.File;

public class GroupCluster {
    public static Instances groupByCluster(Instances instances, int cluster) {
        Instance temp = new DenseInstance(2);
        for (Instance inst : instances) {
            if (inst.value(1) != cluster) {
                instances.set(instances.indexOf(inst), temp);
            }
        }
        instances.deleteWithMissing(1);
        return instances;
    }
    public static void main(String[] args) throws Exception {
       ArffLoader loader = new ArffLoader();
        loader.setSource(new File("T:\\1\\2-3\\Intro to Data Mining\\Local Project\\idm-iu-2021\\IDM_IU_2021\\data\\Clustered.arff"));
        Instances data = loader.getDataSet();
        Instances newData = groupByCluster(data, 0);
        System.out.println(newData);
    }
}
