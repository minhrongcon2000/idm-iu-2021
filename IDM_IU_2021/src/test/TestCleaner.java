package test;

import data.DataCleaner;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

import java.io.File;
import java.io.IOException;

public class TestCleaner {
    public static void main(String[] args) throws IOException {
        ArffLoader loader = new ArffLoader();
        loader.setSource(new File("IDM_IU_2021/data/data.arff"));
        Instances data = loader.getDataSet();
        DataCleaner cleaner = new DataCleaner(data)
                .removeCTransaction()
                .removeInvalidPrice()
                .removeMissingID()
                .removeNegativeQuantity();
        // System.out.println(cleaner.getData());
        System.out.println(cleaner.defaultClean().getData().attribute(3).numValues());
    }
}
