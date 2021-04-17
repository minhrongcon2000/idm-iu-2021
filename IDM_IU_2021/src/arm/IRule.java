package arm;


import java.util.List;

public interface IRule {
    List<IConditionalItem> getPremise();
    List<IConditionalItem> getConsequences();
}
