import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.core.converters.Loader;

import java.io.FileReader;
import java.io.IOException;

public class Cleaner {
    public static Instances cleanData(Instances data) {
        int indexInvoiceNo = data.attribute("InvoiceNo").index();
        int indexQuantity = data.attribute("Quantity").index();
        int indexUnitPrice = data.attribute("UnitPrice").index();
        int indexCustomerID = data.attribute("CustomerID").index();

        for (int i = data.numInstances() - 1; i >= 0; i--) {
            Instance inst = data.get(i);
            if (inst.toString(indexInvoiceNo) == "C"
                    || Integer.valueOf(inst.toString(indexQuantity)) < 0
                    || Double.valueOf(inst.toString(indexUnitPrice)) < 0
                    || inst.toString(indexCustomerID).equals("?")) {
                data.delete(i);
            }
        }
        return data;
    }
//    public static void main(String[] args) {
//        try {
//            DataSource dts = new DataSource("C:\\Users\\DELL\\Desktop\\data.arff");
//            Instances data = dts.getDataSet();
//            System.out.println(cleanData(data));
//        } catch(Exception e)
//        { e.printStackTrace();}
//        }
}
