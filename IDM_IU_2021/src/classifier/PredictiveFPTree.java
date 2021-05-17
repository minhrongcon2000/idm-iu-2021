package classifier;

import arm.fpgrowth.FPTree;
import arm.IConditionalItem;
import arm.IFPTree;
import arm.IRule;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.rules.ZeroR;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

import java.util.List;

public class PredictiveFPTree extends AbstractClassifier {
    private final FPTree tree;
    private final ZeroR zeroRule;
    private Attribute classAttribute;

    public PredictiveFPTree() {
        tree = new FPTree();
        zeroRule = new ZeroR();
    }

    private static int getAttributeIndex(Instance data, String attributeName) {
        for (int i=0; i<data.numAttributes(); ++i) {
            if (data.attribute(i).name().equals(attributeName))
                return i;
        }
        return -1;
    }

    private double[] getProb(IFPTree tree, Instance data) {
        List<IRule> rules = tree.getAssociationRules();
        double[] c = {0, 0};
        for (IRule rule: rules) {
            List<IConditionalItem> consequences = rule.getConsequences();
            if ((consequences.size() == 1) && (consequences.get(0).getItemName().equals(this.classAttribute.name()))) {
                List<IConditionalItem> premise = rule.getPremise();
                int s = 0;
                for (IConditionalItem item: premise) {
                    if (data.stringValue(getAttributeIndex(data, item.getItemName())).equals(item.getItemValue())) {
                        ++s;
                    }
                }
                if (s == premise.size()) {
                    if (consequences.get(0).getItemValue().equals(this.classAttribute.value(0))) ++c[0];
                    else ++c[1];
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
        this.classAttribute = instances.classAttribute();

        // remove class index in order to build FP tree
        instances.setClassIndex(-1);
        tree.buildAssociations(instances);

        // reset class index to build zeroR
        instances.setClassIndex(classIdx);
        zeroRule.buildClassifier(instances);
    }

    @Override
    public double classifyInstance(Instance instance) throws Exception {
        double[] distribution = this.getProb(tree, instance);
        if (distribution[0] == distribution[1]) return zeroRule.classifyInstance(instance);
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
}
