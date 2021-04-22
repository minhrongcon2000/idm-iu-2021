package data;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import java.text.SimpleDateFormat;
import java.util.*;
import org.javatuples.*;
import java.util.concurrent.TimeUnit;

public class RFM {
    private final DataSource source = new DataSource("D:\\data.arff");
    private final Instances rawData = source.getDataSet();
    private final DataCleaner cleaner = new DataCleaner(rawData);
    private final Instances cleanedData = cleaner.defaultClean().getData();
    private final int numberOfAttributes = cleanedData.numAttributes();
    private final int numberOfInstances = cleanedData.numInstances();

    // Setting Time & DateOnly Format
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yy HH:mm");
    Date mostRecentDate = dateFormat.parse("09-12-2011 12:50");

    // Getters & Setters
    public Attribute[] getAttributesList() {
        Attribute[] attributeList = new Attribute[numberOfAttributes];
        Instance exampleInst = cleanedData.firstInstance();
        for (int i = 0; i < numberOfAttributes; i++) {
            Attribute attribute = exampleInst.attributeSparse(i);
            attributeList[i] = attribute;
        }
        return attributeList;
    }

    // Calculate Stuff
    public long daysFromLatestDate(Date invoiceDate){
        long daysFromLatest = Math.abs(mostRecentDate.getTime() - invoiceDate.getTime());
        return TimeUnit.MILLISECONDS.toDays(daysFromLatest);
    }

    public void calculateFrequency() throws Exception {
        Attribute[] attributeList = this.getAttributesList();
        Attribute invoiceNumberAttr = attributeList[0];
        Attribute prodQuantityAttr = attributeList[3];
        Attribute invoiceDateAttr = attributeList[4];
        Attribute pricePerProdAttr = attributeList[5];
        Attribute customerIdAttr = attributeList[6];
        HashMap<String, Quintet<Date, Integer, Integer, Integer, Long>> invoiceMap = new HashMap<>();

        for (int i = 0; i < numberOfInstances; i++) {

            String invoiceNo = cleanedData.instance(i).stringValue(invoiceNumberAttr);
            Date invoiceDate = dateFormat.parse(cleanedData.instance(i).stringValue(invoiceDateAttr));
            int customerId = Integer.parseInt(cleanedData.instance(i).stringValue(customerIdAttr));
            int quantity = Integer.parseInt(cleanedData.attribute(3).value(i));
            System.out.println(quantity);
            int price = Integer.parseInt(cleanedData.instance(i).stringValue(pricePerProdAttr));
            //long curPrice = (long) price * quantity;
            //long curPrice = (long) price * Integer.parseInt(quantity);
            long daysFromLatest = this.daysFromLatestDate(invoiceDate);

            /*
            System.out.println(invoiceNo + '\n'
                    + invoiceDate + '\n'
                    + customerId + '\n'
                    + quantity + '\n'
                    + daysFromLatest);
             */
            if(invoiceMap.containsKey(invoiceNo)) {
                if(invoiceMap.get(invoiceNo).getValue1().equals(customerId)) {
                    long totalPrice = invoiceMap.get(invoiceNo).getValue3();
                    //totalPrice += curPrice;
                    System.out.println(invoiceMap.get(invoiceNo).setAt3(totalPrice));
                }
            } else {
                invoiceMap.put(invoiceNo, new Quintet<>(invoiceDate, customerId, price, price, daysFromLatest));
            }
        } // size = 4339 = number of customers
    }

    // Constructor
    public RFM() throws Exception {
        Instances rawData = DataSource.read("D:\\data.arff");
        DataCleaner cleaner = new DataCleaner(rawData);
        Instances cleanedData = cleaner.defaultClean().getData();
        Instance first1 = cleanedData.firstInstance();
        System.out.println(first1);
        for (int i = 0; i < first1.numAttributes(); i++) {
            System.out.println(first1.attribute(i).value(0));
        }

        calculateFrequency();
    }

    // Main to test all previous sections
    public static void main(String[] args) throws Exception {
        RFM test = new RFM();
    }
}