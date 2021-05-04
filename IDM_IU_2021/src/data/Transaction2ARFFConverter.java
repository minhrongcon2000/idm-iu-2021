package data;

import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.*;
import weka.filters.unsupervised.instance.*;
import weka.core.converters.ConverterUtils.DataSink;
// test
import weka.core.Instance;
import weka.core.converters.ArffLoader;
import java.io.File;
import java.io.IOException;

public class Transaction2ARFFConverter implements iConverter {

    private final String sourcePath;
    private final String desPath;

    public Transaction2ARFFConverter(String sourcePath, String desPath) {
        this.sourcePath = sourcePath;
        this.desPath = desPath;
    }

    private static Instances removeRedundantAttributes(Instances data) throws Exception {
        Remove remove = new Remove();
        String[] options = new String[2];
        options[0] = "-R";
        options[1] = "3-8";
        remove.setOptions(options);
        remove.setInputFormat(data);
        Instances newData = Filter.useFilter(data, remove);
        return newData;
    }

    private static Instances stockCodeStringToNominal(Instances data) throws Exception {
        StringToNominal stn = new StringToNominal();
        String[] options = new String[2];
        options[0] = "-R";
        options[1] = "1-2";
        stn.setOptions(options);
        stn.setInputFormat(data);
        Instances newData = Filter.useFilter(data, stn);
        return newData;
    }

    public static Instances denormalize(Instances inputData) throws Exception {
        Instances data = stockCodeStringToNominal(removeRedundantAttributes(inputData));
        Denormalize denormalize = new Denormalize();
        String[] options = new String[2];
        options[0] = "-G";
        options[1] = "1";
        denormalize.setOptions(options);
        denormalize.setInputFormat(data);
        Instances newData = Filter.useFilter(data, denormalize);
        return newData;
    }

    @Override
    public void convert() throws Exception {
        ArffLoader loader = new ArffLoader();
        loader.setSource(new File(this.sourcePath));
        Instances data = loader.getDataSet();
        DataCleaner cleaner = new DataCleaner(data)
                .removeCTransaction()
                .removeInvalidPrice()
                .removeMissingID()
                .removeNegativeQuantity();
        Instances newData = denormalize(cleaner.getData());
        DataSink.write(this.desPath, newData);
    }

    // test
    public static void main(String[] args) throws Exception {
        String src = "T:\\1\\2-3\\Intro to Data Mining\\Local Project\\idm-iu-2021\\IDM_IU_2021\\data\\data.arff";
        String dest = "T:\\1\\2-3\\Intro to Data Mining\\Local Project\\idm-iu-2021\\IDM_IU_2021\\data\\transaction.arff";

        Transaction2ARFFConverter converter = new Transaction2ARFFConverter(src, dest);
        converter.convert();
    }


}

