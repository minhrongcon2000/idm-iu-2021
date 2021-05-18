package arm;

import weka.core.Instances;
import weka.core.converters.ArffLoader;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class FPDemo {
    public static void main(String[] args) throws IOException {
        // load data
        ArffLoader dataLoader = new ArffLoader();
        dataLoader.setSource(new File("data/vote.arff"));
        Instances data = dataLoader.getDataSet();

        // set options
        FPOptions options = new FPOptions();
        options.setMinSup(100);
        options.setPositiveLabel("y");
        options.setMinItemSetLen(2);
        options.setMinConfidence(0.5);

        // build model
        FPTree fpg = new FPTree(options);
        fpg.buildAssociations(data);


        // Rules
        List<IRule> rules = fpg.getAssociationRules();
        System.out.printf("FP Growth found %d rules:%n", rules.size());
        for (int i = 0; i < rules.size(); i++) {
            System.out.printf("%d. %s%n", i + 1, rules.get(i));
        }
    }
}
