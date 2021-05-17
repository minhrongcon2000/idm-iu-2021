package arm.fpgrowth;

import arm.IRule;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

import java.io.File;
import java.io.IOException;

public class FPDemo {
    public FPDemo() throws IOException {
        // load data
        ArffLoader dataLoader = new ArffLoader();
        dataLoader.setSource(new File("IDM_IU_2021/data/vote.arff"));
        Instances data = dataLoader.getDataSet();

        // set options
        var options = new FPOptions();
        options.setMinSup(100);
        options.setPositiveLabel("y");
        options.setMinItemSetLen(2);
        options.setMinConfidence(0.5);

        // build model
        var fpg = new FPTree(options);
        fpg.buildAssociations(data);

        // Item sets
//        fpg.printFrequentItemSets(2);

        // Rules
        var rules = fpg.getAssociationRules();
        System.out.printf("FP Growth found %d rules:%n", rules.size());
        for (int i = 0; i < rules.size(); i++) {
            System.out.printf("%d. %s%n", i + 1, rules.get(i));
        }
    }
}
