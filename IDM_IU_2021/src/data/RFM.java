package data;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import org.javatuples.*;
import java.util.concurrent.TimeUnit;

public class RFM {
    // Data Source
    private final DataSource source = new DataSource("D:\\data.arff");
    private final Instances rawData = source.getDataSet();
    private final DataCleaner cleaner = new DataCleaner(rawData);
    private final Instances cleanedData = cleaner.defaultClean().getData();
    private final int numberOfAttributes = cleanedData.numAttributes();
    private final int numberOfInstances = cleanedData.numInstances();
    // Format Settings
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yy HH:mm");
    Date latestDate = dateFormat.parse("09-12-2011 12:50");
    // Tables to find Recency Score - Map <InvoiceNo, daysFromLatest> to customerId
    private final HashMap<Integer, Pair<String, Integer>> minDateTable = new HashMap<>();
    private final ArrayList<Integer> minDateList = new ArrayList<>();
    // Tables to find Frequency Score - Map InvoiceCount to customerId
    private final HashMap<Integer, Integer> frequencyTable = new HashMap<>();
    private final ArrayList<Integer> frequencyList = new ArrayList<>();
    // Tables to find Recency Score - Map <InvoiceNo, daysFromLatest> to customerId
    private final HashMap<Integer, Pair<String, Integer>> maxPriceTable = new HashMap<>();
    private final HashMap<Integer, Triplet<Integer, Integer, Integer>> RFMTriplet = new HashMap<>();
    // Final RFMTable
    private final HashMap<Integer, ArrayList<Integer>> RFMTable = new HashMap<>();


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
    public int daysFromLatestDate(Date invoiceDate){
        long daysFromLatest = Math.abs(latestDate.getTime() - invoiceDate.getTime());
        return (int) TimeUnit.MILLISECONDS.toDays(daysFromLatest);
    }
    public int minMaxScaling(int curValue, int maxValue, int minValue) {
        int a = 1, b = 10, score;
        score = a + ((curValue - minValue)*(b-a))/(maxValue-minValue);
        return score;
    }

    // Main functions
    public HashMap<String, Quintet<Date, Integer, Integer, Double, Integer>> createInvoiceMap() throws Exception {
        Attribute[] attributeList = this.getAttributesList();
        Attribute invoiceNumberAttr = attributeList[0];
        Attribute prodQuantityAttr = attributeList[3];
        Attribute invoiceDateAttr = attributeList[4];
        Attribute pricePerProdAttr = attributeList[5];
        Attribute customerIdAttr = attributeList[6];
        HashMap<String, Quintet<Date, Integer, Integer, Double, Integer>> invoiceMap = new HashMap<>();

        for (int i = 0; i < numberOfInstances; i++) {
            String invoiceNo = cleanedData.instance(i).stringValue(invoiceNumberAttr); // att 0
            Date invoiceDate = dateFormat.parse(cleanedData.instance(i).stringValue(invoiceDateAttr)); // att 4
            int customerId = Integer.parseInt(cleanedData.instance(i).stringValue(customerIdAttr)); // att 6
            int quantity = (int) cleanedData.instance(i).value(prodQuantityAttr); // att 3
            double price =  cleanedData.instance(i).value(pricePerProdAttr); // att 5
            double curPrice = price * quantity;
            int daysFromLatest = this.daysFromLatestDate(invoiceDate);

            if(invoiceMap.containsKey(invoiceNo)) {
                Quintet<Date, Integer, Integer, Double, Integer> curValues = invoiceMap.get(invoiceNo);
                double totalPrice = curValues.getValue3();
                totalPrice += curPrice;
                curValues = invoiceMap.get(invoiceNo).setAt3(totalPrice);
                invoiceMap.replace(invoiceNo, curValues);
            } else {
                invoiceMap.put(invoiceNo, new Quintet<>(invoiceDate, customerId, quantity, curPrice, daysFromLatest));
            }
        }
        return invoiceMap;
    }
    public void createSubTables(HashMap<String, Quintet<Date, Integer, Integer, Double, Integer>> invoiceMap) {
        for (String invoiceNo : invoiceMap.keySet()) {
            // Map the most recent date along with the invoice to the customerId
            this.fillMinDateTable(invoiceMap, invoiceNo);
            this.fillFrequencyTable(invoiceMap, invoiceNo);
        }
    }

    // Used in finding Recency
    public void fillMinDateTable(HashMap<String, Quintet<Date, Integer, Integer, Double, Integer>> invoiceMap, String invoiceNo) {
        int customerId = invoiceMap.get(invoiceNo).getValue1();
        int curDate = invoiceMap.get(invoiceNo).getValue4();
        if (minDateTable.containsKey(customerId)) {
            int oldDate = minDateTable.get(customerId).getValue1();
            if (curDate < oldDate)
                minDateTable.replace(customerId, minDateTable.get(customerId), new Pair<>(invoiceNo, curDate));
        } else {
            minDateTable.put(customerId, new Pair<>(invoiceNo, curDate));
        }
    }
    public void getMinDateList() {
        for (int customerId : minDateTable.keySet()) {
            minDateList.add(minDateTable.get(customerId).getValue1());
        }
    }
    public void findRecency() {
        /*
        int farthestDate = 0, mostNoOfInvoices = 0, maxPurchase = 0;
        for (Integer customerId : RFMTriplet.keySet()) {
            int curDate = RFMTriplet.get(customerId).getValue0();
            int curNoOfInvoices = RFMTriplet.get(customerId).getValue1();
            int curPurchase = RFMTriplet.get(customerId).getValue2();
            if (curDate >= farthestDate)
                farthestDate = curDate;
            if (curNoOfInvoices >= mostNoOfInvoices)
                mostNoOfInvoices = curNoOfInvoices;
            if (curPurchase >= maxPurchase)
                maxPurchase = curPurchase;
        }
        for (Integer customerId : RFMTriplet.keySet()) {
            int RScore = minMaxScaling(RFMTriplet.get(customerId).getValue0(), farthestDate, 0);
            int FScore = minMaxScaling(RFMTriplet.get(customerId).getValue1(), mostNoOfInvoices, 0);
            int MScore = minMaxScaling(RFMTriplet.get(customerId).getValue2(), maxPurchase, 0);
        }

         */

        int farthestDay = minDateList.get(0);
        for (int curDate : minDateList) {
            if (curDate >= farthestDay)
                farthestDay = curDate;
        }
        for (Integer customerId : minDateTable.keySet()) {
            int rScore = minMaxScaling(minDateTable.get(customerId).getValue1(), farthestDay, 0);
            RFMTable.put(customerId, new ArrayList<>());
            RFMTable.get(customerId).add(rScore);
            System.out.println(customerId + "\t" + RFMTable.get(customerId));
        }
        // Retrieve the min and maximum date
    }

    // Used in finding Frequency
    public void fillFrequencyTable(HashMap<String, Quintet<Date, Integer, Integer, Double, Integer>> invoiceMap, String invoiceNo) {
        int customerId = invoiceMap.get(invoiceNo).getValue1();
        if (frequencyTable.containsKey(customerId)) {
            int currentNoOfInvoice = frequencyTable.get(customerId);
            currentNoOfInvoice += 1;
            frequencyTable.replace(customerId, frequencyTable.get(customerId), currentNoOfInvoice);
        } else {
            frequencyTable.put(customerId, 1);
        }
    }
    public ArrayList<Integer> getFrequencyList() {
        for (int customerId : frequencyTable.keySet()) {
            frequencyList.add(frequencyTable.get(customerId));
        }
        return frequencyList;
    }
    public void findFrequency() {
        int mostNoOfInvoices = frequencyList.get(0);
        for (int curNoOfInvoices : frequencyList) {
            if (curNoOfInvoices >= mostNoOfInvoices)
                mostNoOfInvoices = curNoOfInvoices;
        }
        for (Integer customerId : minDateTable.keySet()) {
            int rScore = minMaxScaling(minDateTable.get(customerId).getValue1(), mostNoOfInvoices, 0);
            RFMTable.put(customerId, new ArrayList<>());
            RFMTable.get(customerId).add(rScore);
            System.out.println(customerId + "\t" + RFMTable.get(customerId));
        }
    }
    // Constructor
    public RFM() throws Exception {
        /*
        Instances rawData = DataSource.read("D:\\data.arff");
        DataCleaner cleaner = new DataCleaner(rawData);
        Instances cleanedData = cleaner.defaultClean().getData();
        Instance first1 = cleanedData.firstInstance();
        System.out.println(first1);
        for (int i = 0; i < first1.numAttributes(); i++) {
            System.out.println(first1.attribute(i).value(0));
        }
         */
        createSubTables(createInvoiceMap());
        getMinDateList();
        findRecency();

        // Frequency
        /*
        for (Integer customerId : frequencyTable.keySet()) {
            System.out.println(customerId + " has " + frequencyTable.get(customerId) + " invoices.");
        }
         */
    }

    // Main to test all previous sections
    public static void main(String[] args) throws Exception {
        RFM test = new RFM();
    }
}