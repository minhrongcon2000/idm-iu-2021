package clustering;

import weka.clusterers.EM;
import weka.clusterers.SimpleKMeans;
import weka.core.*;
import weka.core.converters.ArffLoader;
import weka.core.converters.ArffSaver;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class KMeansClusterer {
    // specified KMeans clusterer for this project
    private Instances rfmCut, rfm, finalCentroid, writer;
    private ArrayList<Attribute> attributes= new ArrayList<>();
    private Instances cluster0,cluster1,cluster2;
    private ArrayList<Instances> kmeansClusters = new ArrayList<>();


    public void buildCluster() throws Exception {
        // call child functions
        this.getData();
        this.initAttributes();
        this.genCentroid();
        this.clusterCentroid();
        this.calculateCluster();
        this.export();
    }
    private void getData() throws IOException {
        // get data set for the centroids generation
        ArffLoader arffLoader = new ArffLoader();
        arffLoader.setFile(new File("IDM_IU_2021/data/rfmTableWithoutCustId.arff"));
        rfmCut = arffLoader.getDataSet();
        arffLoader.setFile(new File("IDM_IU_2021/data/rfmTable.arff"));
        rfm = arffLoader.getDataSet();

    }

    private void initAttributes(){
        attributes.add(rfmCut.attribute(0));
        attributes.add(rfmCut.attribute(1));
        attributes.add(rfmCut.attribute(2));

        cluster0 = new Instances("cluster 0",attributes, 5000);
        cluster1 = new Instances("cluster 1",attributes, 5000);
        cluster2 = new Instances("cluster 2",attributes, 5000);
        finalCentroid = new Instances("finalCentroid", attributes, 5000);
    }

    private void genCentroid() throws Exception {
        // Run seeds from 1 to 100, generate list of centroid --> bring to the next cluster
        String option="-init 0 -periodic-pruning 10000 -min-density 2.0 -t1 -1.25 -t2 -1.0 -N 3 -A weka.core.EuclideanDistance -I 100 -num-slots 1 ";
        SimpleKMeans kMeans = new SimpleKMeans();
        kMeans.setOptions(weka.core.Utils.splitOptions(option));
        kMeans.setPreserveInstancesOrder(true);

        for (int i = 1; i < 100; i++){
            kMeans.setSeed(i);
            kMeans.buildClusterer(rfmCut);
            cluster0.add(kMeans.getClusterCentroids().instance(0));
            cluster1.add(kMeans.getClusterCentroids().instance(1));
            cluster2.add(kMeans.getClusterCentroids().instance(2));
        }
        kmeansClusters.add(cluster0);
        kmeansClusters.add(cluster1);
        kmeansClusters.add(cluster2);
    }
    private void clusterCentroid() throws Exception {
        // Cluster the centroids, return the final centroid

        for (int counter = 0; counter<kmeansClusters.size(); counter++){
            Instances cluster = kmeansClusters.get(counter);
            EM em = new EM();
            String option = "-I 100 -N -1 -X 10 -max -1 -ll-cv 1.0E-6 -ll-iter 1.0E-6 -M 1.0E-6 -K 10 -num-slots 1 -S 100";
            em.setOptions(weka.core.Utils.splitOptions(option));
            em.buildClusterer(cluster);
            ArrayList<Double> resultByCluster= new ArrayList<>();

            int numberOfCluster= em.numberOfClusters();
            double percentageThres = (1.0/numberOfCluster)*100;
            double[][][] emCluster= em.getClusterModelsNumericAtts();
            double minSumStd=Double.MAX_VALUE, percentage=0, sumStd=0;
            int currentCluster=0;
            for (int i =0; i<numberOfCluster; i++){
                percentage=emCluster[i][0][2];
                if (percentage>=percentageThres){
                    sumStd= emCluster[i][0][1]+emCluster[i][1][1]+emCluster[i][2][1];
                    if (minSumStd>sumStd){
                        minSumStd=sumStd;
                        currentCluster=i;
                    }
                }
            }

            Instance temp = new DenseInstance(3);
            temp.setValue(0,emCluster[currentCluster][0][0]);
            temp.setValue(1,emCluster[currentCluster][1][0]);
            temp.setValue(2,emCluster[currentCluster][2][0]);
            finalCentroid.add(temp);
        }

        System.out.println("Final centroid:"+ finalCentroid);

    }
    private double calculateDistance(double x1, double y1, double z1, double x2, double y2, double z2){
        return Math.sqrt(Math.pow((x1-x2),2)+Math.pow((y1-y2),2)+Math.pow((z1-z2),2));
    }
    private void calculateCluster(){
        // from the final centroid --> calculate the cluster again --> export to the new data set of CustID and Clustered
//        EuclideanDistance EucDistance = new EuclideanDistance();
//        EucDistance.setDontNormalize(true);
//        EucDistance.setInstances(rfmCut);
        attributes=new ArrayList<>();
        attributes.add(new Attribute("CustId"));
        attributes.add(new Attribute("Cluster"));
        writer = new Instances("Clustered",attributes,rfm.size()+1);
        int clusterAssignment = 0;

        for (int i =0; i<rfmCut.size(); i++){
            double minDist = Double.MAX_VALUE;
            Instance temp2 = rfmCut.instance(i);
            for (int j =0; j<3; j++){
                Instance temp = finalCentroid.instance(j);
                double distance = calculateDistance(temp.value(0), temp.value(1),temp.value(2), temp2.value(0), temp2.value(1), temp2.value(2));
                if (minDist>distance){
                    minDist = distance;
                    clusterAssignment=j;
                }
            }
            Instance temp = new DenseInstance(2);
            temp.setValue(0,rfm.instance(i).value(0));
            temp.setValue(1,clusterAssignment);
            writer.add(temp);
        }
    }

    private void export() throws IOException {
        ArffSaver saver= new ArffSaver();
        saver.setInstances(writer);
        saver.setFile(new File("IDM_IU_2021/data/Clustered.arff"));
        saver.writeBatch();
    }
}
