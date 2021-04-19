import weka.core.Instance;
import weka.core.Instances;

public class Cleaner {
    public Instances cleanData(Instances data) {
        int indexInvoiceNo = data.attribute("InvoiceNo").index();
        int indexQuantity = data.attribute("Quantity").index();
        int indexUnitPrice = data.attribute("UnitPrice").index();
        int indexCustomerID = data.attribute("CustomerID").index();

        for (int i = data.numInstances() - 1; i >= 0; i--) {
            Instance inst = data.get(i);
            if (inst.toString(indexInvoiceNo) == "C"
                    || Integer.valueOf(inst.toString(indexQuantity)) < 0
                    || Double.valueOf(inst.toString(indexUnitPrice)) < 0
                    || inst.toString(indexCustomerID).equals("NaN")) {
                data.delete(i);
            }
        }
        return data;
    }
}
