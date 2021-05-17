package classifier;

import weka.associations.AssociationRule;
import weka.associations.AssociationRules;
import weka.associations.FPGrowth;
import weka.associations.Item;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.rules.ZeroR;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

public class PredictiveFPTree extends AbstractClassifier {
    private final FPGrowth tree;
    private final ZeroR zeroRule;
    private Attribute classAttribute;

    public PredictiveFPTree() {
        tree = new FPGrowth();
        zeroRule = new ZeroR();
    }

    public static int getAttributeIndex(Instance data, String attributeName) {
        for (int i=0; i<data.numAttributes(); ++i) {
            if (data.attribute(i).name().equals(attributeName))
                return i;
        }
        return -1;
    }

    private double[] getProb(FPGrowth tree, Instance data) {
        AssociationRules rules = tree.getAssociationRules();
        double[] c = {0, 0};
        for (AssociationRule rule: rules.getRules()) {
            Item[] consequence = rule.getConsequence().toArray(new Item[0]);
            if ((consequence.length == 1) && (consequence[0].getAttribute().name().equals(classAttribute.name()))) {
                Item[] antecedent = rule.getPremise().toArray(new Item[0]);
                int s = 0;
                for (Item item: antecedent) {
                    if (data.stringValue(getAttributeIndex(data, item.getAttribute().name())).equals(item.getItemValueAsString()))
                        s++;
                }
                if (s == antecedent.length) {
                    if (consequence[0].getItemValueAsString().equals(classAttribute.value(0))) c[0]++;
                    else c[1]++;
                }
            }
        }

        if (c[0] != c[1]) {
            c[0] /= (c[0] + c[1]);
            c[1] /= (c[0] + c[1]);
        }
        return c;
    }

    @Override
    public void buildClassifier(Instances instances) throws Exception {
        int classIdx = instances.classIndex();
        classAttribute = instances.classAttribute();

        // remove class index in order to build FP tree
        instances.setClassIndex(-1);
        tree.buildAssociations(instances);

        // reset class index to build zeroR
        instances.setClassIndex(classIdx);
        zeroRule.buildClassifier(instances);
    }

    @Override
    public double classifyInstance(Instance instance) {
        double[] distribution = this.getProb(tree, instance);
        if (distribution[0] == distribution[1]) {
            return zeroRule.classifyInstance(instance);
        }
        if (distribution[0] > distribution[1]) return 0.0d;
        return 1.0d;
    }

    @Override
    public double[] distributionForInstance(Instance instance) throws Exception {
        double[] distribution = this.getProb(tree, instance);
        if (distribution[0] == distribution[1]) {
            return zeroRule.distributionForInstance(instance);
        }
        return distribution;
    }

    public void setMinMetrics(double minMetrics) {
        tree.setMinMetric(minMetrics);
    }
}
