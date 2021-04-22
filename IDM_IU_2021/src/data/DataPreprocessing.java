package data;

import weka.core.Instance;
import weka.core.Instances;

public class DataPreprocessing {
    public static Instances removeCTransaction(Instances data) {
        int indexInvoiceNo = data.attribute("InvoiceNo").index();
        for (int i = data.numInstances() - 1; i >= 0; i--) {
            Instance inst = data.get(i);
            if (inst.toString(indexInvoiceNo).equals("C")) {
                data.delete(i);
            }
        }
        return data;
    }

    public static Instances removeNegativeQuantity(Instances data) {
        int indexQuantity = data.attribute("Quantity").index();
        for (int i = data.numInstances() - 1; i >= 0; i--) {
            Instance inst = data.get(i);
            if (Integer.parseInt(inst.toString(indexQuantity)) < 0) {
                data.delete(i);
            }
        }
        return data;
    }

    public static Instances removeInvalidPrice(Instances data) {
        int indexUnitPrice = data.attribute("UnitPrice").index();
        for (int i = data.numInstances() - 1; i >= 0; i--) {
            Instance inst = data.get(i);
            if (Double.parseDouble(inst.toString(indexUnitPrice)) < 0) {
                data.delete(i);
            }
        }
        return data;
    }

    public static Instances removeMissingID(Instances data) {
        int indexCustomerID = data.attribute("CustomerID").index();

        for (int i = data.numInstances() - 1; i >= 0; i--) {
            Instance inst = data.get(i);
            if (inst.toString(indexCustomerID).equals("?")) {
                data.delete(i);
            }
        }
        return data;
    }
}
