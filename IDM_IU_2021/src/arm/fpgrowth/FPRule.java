package arm.fpgrowth;

import arm.IConditionalItem;
import arm.IRule;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FPRule implements IRule {
    List<IConditionalItem> premises = new ArrayList<>();
    List<IConditionalItem> consequences = new ArrayList<>();
    int premiseSupp, consequenceSupp;
    double confidence;

//    public get

    public FPRule(Set<String> premiseItems, int premiseSupp, Set<String> consequenceItems, int consequenceSupp, double confidence) {
        addRulePart(premiseItems, premises);
        addRulePart(consequenceItems, consequences);
        this.premiseSupp = premiseSupp;
        this.consequenceSupp = consequenceSupp;
        this.confidence = confidence;
    }

    void addRulePart(Set<String> itemSet, List<IConditionalItem> target) {
        itemSet.forEach(item -> {
            var components = item.split("=");
            var attr = components[0];
            var val = components[1];
            target.add(new FPConditionalItem(attr, val));
        });
    }

    @Override
    public String toString() {
        return String.format("%s: %d => %s: %d <conf:(%f)>", premises, premiseSupp, consequences, consequenceSupp, confidence);
    }

    @Override
    public List<IConditionalItem> getPremise() {
        return premises;
    }

    @Override
    public List<IConditionalItem> getConsequences() {
        return consequences;
    }
}
