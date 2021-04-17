package arm;

import weka.associations.AbstractAssociator;
import weka.core.Instances;

import java.util.List;

public class FPTree extends AbstractAssociator implements IFPTree {
    @Override
    public List<IRule> getAssociationRules() {
        // TODO: After constructing FPTree, association rules must be acquired using this method
        return null;
    }

    /**
     * build FP tree
     * @param instances data containing only binary attributes
     */
    @Override
    public void buildAssociations(Instances instances) {
        // TODO: code for constructing FPTree goes here
    }
}
