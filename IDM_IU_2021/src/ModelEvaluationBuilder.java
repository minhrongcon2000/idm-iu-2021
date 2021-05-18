public class ModelEvaluationBuilder {
    private ModelEvaluation evaluator;

    public ModelEvaluationBuilder() {
        evaluator = new ModelEvaluation();
    }

    public void setMaxIter(int maxIter) {
        evaluator.setMaxIter(maxIter);
    }

    public void setPositiveLabel(String positiveLabel) {
        evaluator.setPositiveLabel(positiveLabel);
    }

    public void setMinConf(double minConf) {
        evaluator.setMinConf(minConf);
    }

    public void setMinItemSetLen(int minItemSetLen) {
        evaluator.setMinItemSetLen(minItemSetLen);
    }

    public ModelEvaluation getEvaluator() {
        return this.evaluator;
    }
}
