package data;

import weka.core.*;
import weka.core.converters.ConverterUtils.DataSource;
import weka.core.converters.ConverterUtils.DataSink;
import weka.core.Instances;

import java.text.DecimalFormat;
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
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yy HH:mm");
    private final DecimalFormat doubleFormat = new DecimalFormat("#.#####");
    Date latestDate = dateFormat.parse(cleanedData.lastInstance().stringValue(4));
    private final ArrayList<Integer> recencyList = new ArrayList<>();
    private final ArrayList<Integer> frequencyList = new ArrayList<>();
    private final ArrayList<Integer> monetaryList = new ArrayList<>();
    private final HashMap<String, Quintet<Date, Integer, Integer, Double, Integer>> invoiceMap = new HashMap<>();
    private final HashMap<Integer, Pair<String, ArrayList<Integer>>> customerValues = new HashMap<>();
    private final HashMap<Integer, ArrayList<Double>> RFMMap = new HashMap<>();


    // Constructor
    public RFM() throws Exception {
        generateInvoiceMap();
        generateCustomerValues();
        HashMap<String, Double> extremeValues = generateRFMLists();
        generateRFMMap(extremeValues);
        Instances rfmTable = createRFMInstance();
    }


    // Main functions
    public void generateInvoiceMap() throws Exception {
        Attribute[] attributeList = this.getAttributesList();
        Attribute invoiceNumberAttr = attributeList[0];
        Attribute prodQuantityAttr = attributeList[3];
        Attribute invoiceDateAttr = attributeList[4];
        Attribute pricePerProdAttr = attributeList[5];
        Attribute customerIdAttr = attributeList[6];

        for (int i = 0; i < numberOfInstances; i++) {
            String invoiceNo = cleanedData.instance(i).stringValue(invoiceNumberAttr); // att 0
            Date invoiceDate = dateFormat.parse(cleanedData.instance(i).stringValue(invoiceDateAttr)); // att 4
            int customerId = Integer.parseInt(cleanedData.instance(i).stringValue(customerIdAttr)); // att 6
            int quantity = (int) cleanedData.instance(i).value(prodQuantityAttr); // att 3
            double productPrice =  cleanedData.instance(i).value(pricePerProdAttr); // att 5
            double invoicePrice = productPrice * quantity;
            int daysFromLatest = this.daysFromLatestDate(invoiceDate);

            if(invoiceMap.containsKey(invoiceNo)) {
                Quintet<Date, Integer, Integer, Double, Integer> invoiceValues = invoiceMap.get(invoiceNo);
                double invoiceTotalPrice = invoiceValues.getValue3();
                invoiceTotalPrice += invoicePrice;
                invoiceValues = invoiceMap.get(invoiceNo).setAt3(invoiceTotalPrice);
                invoiceMap.replace(invoiceNo, invoiceValues);
            } else {
                invoiceMap.put(invoiceNo, new Quintet<>(invoiceDate, customerId, quantity, invoicePrice, daysFromLatest));
            }
        }
    }
    public void generateCustomerValues() {
        for (String invoiceNo : invoiceMap.keySet()) {
            int customerId = invoiceMap.get(invoiceNo).getValue1();
            double invoicePrice = invoiceMap.get(invoiceNo).getValue3();
            int invoiceDate = invoiceMap.get(invoiceNo).getValue4();

            if (customerValues.containsKey(customerId)) {
                int oldDate = customerValues.get(customerId).getValue1().get(0);
                int currentNoOfInvoice = customerValues.get(customerId).getValue1().get(1) + 1;
                int oldPrice = customerValues.get(customerId).getValue1().get(2);
                // Modify MinDate of customer
                if (invoiceDate < oldDate)
                    customerValues.get(customerId).getValue1().set(0, invoiceDate);
                // Modify noOfInvoices
                customerValues.get(customerId).getValue1().set(1, currentNoOfInvoice);
                // Modify MonetaryValues
                if (invoicePrice > oldPrice && invoicePrice != 0)
                    customerValues.get(customerId).getValue1().set(2, (int) invoicePrice);
            } else {
                customerValues.put(customerId, new Pair<>(invoiceNo, new ArrayList<>()));
                customerValues.get(customerId).getValue1().add(invoiceDate);
                customerValues.get(customerId).getValue1().add(1);
                customerValues.get(customerId).getValue1().add((int) invoicePrice);
            }
        }
    }
    public HashMap<String, Double> generateRFMLists() {
        double rSum = 0, fSum = 0, mSum = 0;
        // Create the lists
        for (int customerId : customerValues.keySet()) {
            recencyList.add(customerValues.get(customerId).getValue1().get(0));
            frequencyList.add(customerValues.get(customerId).getValue1().get(1));
            monetaryList.add(customerValues.get(customerId).getValue1().get(2));
        }
        // Find the maximum value in each list
        double maxDate = 0, maxPrice = 0, minPrice = monetaryList.get(0),
                maxInvoice = 0, minInvoice = frequencyList.get(0);
        for (int i = 0; i < customerValues.size(); i++) {
            // Recency List
            if (recencyList.get(i) >= maxDate)
                maxDate = recencyList.get(i);
            // Frequency List
            if (recencyList.get(i) >= maxInvoice)
                maxInvoice = frequencyList.get(i);
            if (recencyList.get(i) <= minInvoice)
                minInvoice = frequencyList.get(i);
            // Monetary List
            if (monetaryList.get(i) >= maxPrice)
                maxPrice = monetaryList.get(i);
            if (monetaryList.get(i) <= minPrice)
                minPrice = monetaryList.get(i);

            rSum += recencyList.get(i);
            fSum += frequencyList.get(i);
            mSum += monetaryList.get(i);
        }
        double rMean = rSum/recencyList.size();
        double fMean = fSum/frequencyList.size();
        double mMean = mSum/monetaryList.size();
        // Return the values
        HashMap<String, Double> extremeValues = new HashMap<>();
        extremeValues.put("maxDate", maxDate);
        extremeValues.put("maxPrice", maxPrice);
        extremeValues.put("minPrice", minPrice);
        extremeValues.put("maxInvoice", maxInvoice);
        extremeValues.put("minInvoice", minInvoice);
        extremeValues.put("rMean", rMean);
        extremeValues.put("fMean", fMean);
        extremeValues.put("mMean", mMean);
        return extremeValues;
    }
    public void generateRFMMap(HashMap<String, Double> extremeValues) {
        double maxDate = extremeValues.get("maxDate"),
                maxPrice = extremeValues.get("maxPrice"),
                minPrice = extremeValues.get("minPrice"),
                maxInvoice = extremeValues.get("maxInvoice"),
                minInvoice = extremeValues.get("minInvoice"),
                rMean = extremeValues.get("rMean"),
                fMean = extremeValues.get("fMean"),
                mMean = extremeValues.get("mMean");

        for (int customerId : customerValues.keySet()) {
            // int RScore = minMaxNormalization(customerValues.get(customerId).getValue1().get(0), maxDate, 0);
            // int FScore = minMaxNormalization(customerValues.get(customerId).getValue1().get(1), 10, 1);
            // int MScore = minMaxNormalization(customerValues.get(customerId).getValue1().get(2), maxPrice, minPrice);
            double RScore = Double.parseDouble(doubleFormat.format(
                    meanNormalization(customerValues.get(customerId).getValue1().get(0), rMean, maxDate, 0)));
            double FScore = Double.parseDouble(doubleFormat.format(
                    meanNormalization(customerValues.get(customerId).getValue1().get(1), fMean, maxInvoice, minInvoice)));
            double MScore = Double.parseDouble(doubleFormat.format(
                    meanNormalization(customerValues.get(customerId).getValue1().get(2), mMean, maxPrice, minPrice)));
            RFMMap.put(customerId, new ArrayList<>());
            RFMMap.get(customerId).add(RScore);
            RFMMap.get(customerId).add(FScore);
            RFMMap.get(customerId).add(MScore);
        }
    }
    public Instances createRFMInstance() {
        Instances RFMTable = new Instances("RFMTable", new ArrayList<>(), RFMMap.size());
        RFMTable.insertAttributeAt(new Attribute("CustomerID", false), 0);
        RFMTable.insertAttributeAt(new Attribute("Recency"), 1);
        RFMTable.insertAttributeAt(new Attribute("Frequency"), 2);
        RFMTable.insertAttributeAt(new Attribute("Monetary"), 3);
        for (Integer customerId : RFMMap.keySet()) {
            DenseInstance customerInstance = new DenseInstance(4);
            customerInstance.setValue(0, customerId);
            customerInstance.setValue(1, RFMMap.get(customerId).get(0));
            customerInstance.setValue(2, RFMMap.get(customerId).get(1));
            customerInstance.setValue(3, RFMMap.get(customerId).get(2));
            RFMTable.add(customerInstance);
        }
        String outputFile = "D:\\rfmTable.arff";
        try {
            DataSink.write(outputFile,RFMTable);
        } catch(Exception e) {
            System.out.println("Couldn't write data to " + outputFile);
            e.printStackTrace();
        }
        return RFMTable;
    }


    // Supporting functions
    public Attribute[] getAttributesList() {
        Attribute[] attributeList = new Attribute[numberOfAttributes];
        Instance exampleInst = cleanedData.firstInstance();
        for (int i = 0; i < numberOfAttributes; i++) {
            Attribute attribute = exampleInst.attributeSparse(i);
            attributeList[i] = attribute;
        }
        return attributeList;
    }
    public int daysFromLatestDate(Date invoiceDate) {
        long daysFromLatest = Math.abs(latestDate.getTime() - invoiceDate.getTime());
        return (int) TimeUnit.MILLISECONDS.toDays(daysFromLatest);
    }


    // Normalization Methods
    public int minMaxNormalization(int curValue, int maxValue, int minValue) {
        int a = 1, b = 10, score;
        score = a + ((curValue - minValue)*(b-a))/(maxValue-minValue);
        return score;
    }
    public double meanNormalization(double curValue, double sampleMean, double maxValue, double minValue) {
        return (curValue - sampleMean)/maxValue-minValue;
    }


    // Test function main()
    public static void main(String[] args) throws Exception {
        RFM test = new RFM();
        Instances rfm = test.createRFMInstance();
        System.out.println(rfm.toString());
    }
}