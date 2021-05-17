package arm;

import weka.associations.AssociationRules;
import weka.associations.FPGrowth;
import weka.core.Instances;

import java.util.*;

public class FPGrowthAssociation {
    AssociationRules rules = null;
    Instances data, clusters;
    Map<Integer, Set<String>> cusByClusters = new HashMap<Integer, Set<String>>();

    public FPGrowthAssociation(Instances data, Instances clusters) throws Exception {
        this.data = data;
        this.clusters = clusters;
        var fpg = new FPGrowth();
//        fpg.buildAssociations(preProcessDataForFPGrowth(data));
//        rules = fpg.getAssociationRules();
        var rows = clusters.enumerateInstances();
        var clusterAttr = clusters.attribute("Cluster");
        var cusAttr = clusters.attribute("CustId");
        while (rows.hasMoreElements()) {
            var row = rows.nextElement();
            int cluster_index = (int) row.value(clusterAttr);
            if (!cusByClusters.containsKey(cluster_index))
                cusByClusters.put(cluster_index, new HashSet<String>());
            else
                cusByClusters.get(cluster_index).add(String.valueOf((int) row.value(cusAttr)));
        }
        System.out.println("Done");
    }

    public AssociationRules rulesForCluster(int cluster_index) throws Exception {
        var fpg = new FPGrowth();
        fpg.buildAssociations(dataByCluster((cluster_index)));
        return fpg.getAssociationRules();
    }

    public Instances dataByCluster(int cluster_index) {
        var clusterInstances = new Instances(data);
        clusterInstances.delete();
        var cusAttr = data.attribute("CustomerID");
        var cusInCluster = cusByClusters.get(cluster_index);
        var rows = data.enumerateInstances();
        while (rows.hasMoreElements()) {
            var row = rows.nextElement();
            var cusId = row.stringValue(cusAttr);
            if (cusInCluster.contains(cusId)) clusterInstances.add(row);
        }
        return clusterInstances;
    }

    public void displayAssociationRules() throws Exception {
        System.out.println(String.format("FP Growth %d rules for whole data:", rules.getNumRules()));
        for (var rule : rules.getRules()) {
            System.out.println(rule.toString());
        }
        System.out.println();
        for (int c = 0; c < cusByClusters.keySet().size(); c++) {
            var cluster_rules = rulesForCluster(c);
            System.out.println(String.format("FP Growth %d rules for cluster %d:", cluster_rules.getNumRules(), c));
            for (var rule : cluster_rules.getRules()) {
                System.out.println(rule.toString());
            }
            System.out.println();
        }
    }
}
