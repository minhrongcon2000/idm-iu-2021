package data;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.ConverterUtils;
import weka.core.converters.LibSVMSaver;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.attribute.Reorder;
import weka.filters.unsupervised.attribute.SortLabels;
import weka.filters.unsupervised.attribute.StringToNominal;
import weka.filters.unsupervised.instance.Denormalize;

import java.io.File;
import java.util.ArrayList;

public class Cluster2Transaction {

    public static void main(String[] args) throws Exception {
        String src = "T:\\1\\2-3\\Intro to Data Mining\\Local Project\\idm-iu-2021\\IDM_IU_2021\\data\\data.arff";
        String dest = "T:\\1\\2-3\\Intro to Data Mining\\Local Project\\idm-iu-2021\\IDM_IU_2021\\data\\transaction2.arff";

        ArffLoader loader = new ArffLoader();
        loader.setSource(new File(src));
        Instances data = loader.getDataSet();
        DataCleaner cleaner = new DataCleaner(data)
                .removeCTransaction()
                .removeInvalidPrice()
                .removeMissingID()
                .removeNegativeQuantity();
        Instances newData = cleaner.getData();

        Reorder reorder = new Reorder();
        String[] roptions = new String[2];
        roptions[0] = "-R";
        roptions[1] = "7,2";
        reorder.setOptions(roptions);
        reorder.setInputFormat(newData);
        Instances reorderedData = Filter.useFilter(newData, reorder);

        String temp = "T:\\1\\2-3\\Intro to Data Mining\\Local Project\\idm-iu-2021\\IDM_IU_2021\\data\\temp.arff";
        loader.setSource(new File(temp));
        Instances tempInst = loader.getDataSet();


        StringToNominal stn = new StringToNominal();
        String[] options = new String[2];
        options[0] = "-R";
        options[1] = "2";
        stn.setOptions(options);
        stn.setInputFormat(tempInst);
        Instances nominalData = Filter.useFilter(tempInst, stn);

        nominalData.sort(0);

        Denormalize denormalize = new Denormalize();
        String[] doptions = new String[2];
        doptions[0] = "-G";
        doptions[1] = "1";
        denormalize.setOptions(doptions);
        denormalize.setInputFormat(nominalData);
        Instances denormalizedData = Filter.useFilter(nominalData, denormalize);

        String src2 = "T:\\1\\2-3\\Intro to Data Mining\\Local Project\\idm-iu-2021\\IDM_IU_2021\\data\\Clustered-John.arff";
        loader.setSource(new File(src2));
        Instances johndata = loader.getDataSet();


        Instance tempi = new DenseInstance(denormalizedData.numAttributes());
        for (Instance i : denormalizedData) {
            boolean isIn = false;
            for (Instance j : johndata) {
                if (i.value(0) == j.value(0)) {
                    isIn = true;
                }
            }
            if (!isIn) {
                denormalizedData.set(denormalizedData.indexOf(i), tempi);
            }
        }
        denormalizedData.deleteWithMissing(0);

        Instances newTransaction = Instances.mergeInstances(johndata, denormalizedData);

        Remove remove = new Remove();
        String[] reoptions = new String[2];
        reoptions[0] = "-R";
        reoptions[1] = "3";
        remove.setOptions(reoptions);
        remove.setInputFormat(newTransaction);
        Instances newTransaction2 = Filter.useFilter(newTransaction, remove);

        String dest2 = "T:\\1\\2-3\\Intro to Data Mining\\Local Project\\idm-iu-2021\\IDM_IU_2021\\data\\new-transaction.arff";
        ConverterUtils.DataSink.write(dest2, newTransaction2);
    }
}
