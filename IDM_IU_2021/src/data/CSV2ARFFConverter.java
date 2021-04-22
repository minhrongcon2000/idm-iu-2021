package data;

import com.opencsv.CSVReader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CSV2ARFFConverter implements iConverter {
    private final String sourcePath;
    private final String desPath;
    private final String attrConf;
    private final String relationName;
    private int numOfAttr;
    private HashMap<String, String> timeInputFormat;
    private final String timeOutputFormat;

    public CSV2ARFFConverter(String sourcePath, String desPath,
                             String attrConf,
                             String relationName) throws IOException, org.json.simple.parser.ParseException {
        this.sourcePath = sourcePath;
        this.desPath = desPath;
        this.attrConf = attrConf;
        this.relationName = relationName;
        this.timeOutputFormat = "dd-MM-yy KK:mma";
        this.buildInputFormat();
        this.getAttrNum();
    }

    public CSV2ARFFConverter(String sourcePath, String desPath,
                             String attrConf) throws IOException, org.json.simple.parser.ParseException {
        this(sourcePath, desPath, attrConf, "wekadata");
    }

    public int getNumLines() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(this.sourcePath));
        int count = 0;
        while (br.readLine() != null) {
            count++;
        }
        br.close();
        return count;
    }

    public void buildInputFormat() {
        this.timeInputFormat = new HashMap<>();
        timeInputFormat = new HashMap<>();
        timeInputFormat.put("/", "dd/MM/yy HH:mm");
        timeInputFormat.put("-", "dd-MM-yy HH:mm");
    }

    public void getAttrNum() throws org.json.simple.parser.ParseException, IOException {
        JSONArray attr = (JSONArray) new JSONParser().parse(new FileReader(attrConf));
        this.numOfAttr = attr.size();
    }

    public void isDataAttributeConsistent() throws IOException, ConfigDataInconsistentException {
        CSVReader csvReader = new CSVReader(new FileReader(sourcePath));
        String[] nextRecord;
        //Skip column names
        csvReader.readNext();
        int count = 1;
        while ((nextRecord = csvReader.readNext()) != null) {
            count++;
            if (nextRecord.length != this.numOfAttr) {
                throw new ConfigDataInconsistentException("Inconsistent data at line " + count + "\n");
            }
        }
        csvReader.close();
    }

    public ArrayList<AttrObj> getAttrList() throws IOException, org.json.simple.parser.ParseException {
        JSONArray attrJson = (JSONArray) new JSONParser().parse(new FileReader(attrConf));
        ArrayList<AttrObj> attrList = new ArrayList<>();
        for (Object value : attrJson) {
            JSONObject o = (JSONObject) value;
            JSONArray labels = (JSONArray) o.get("labels");
            if (labels != null) {
                ArrayList<String> temp = new ArrayList<>();
                for (Object label : labels) {
                    temp.add((String) label);
                }
                AttrObj attrObj = new AttrObj((String) o.get("name"), (String) o.get("type"), temp);
                attrList.add(attrObj);
            } else {
                AttrObj attrObj = new AttrObj((String) o.get("name"), (String) o.get("type"));
                attrList.add(attrObj);
            }
        }
        return (attrList);
    }

    public void convert() throws ConfigDataInconsistentException,
            IOException, org.json.simple.parser.ParseException, ParseException {
        isDataAttributeConsistent();
        CSVReader reader = new CSVReader(new FileReader(sourcePath));
        BufferedWriter bw = new BufferedWriter(new FileWriter(desPath));

        //Write Header
        System.out.println("Writing header...");
        bw.write("@relation " + this.relationName + "\n\n");
        ArrayList<AttrObj> attrList = getAttrList();
        for (AttrObj attrObj : attrList) {
            String s = formatHeader(attrObj);
            bw.write(s);
            bw.newLine();
        }
        bw.newLine();

        //Write Data
        System.out.println("Writing data...");
        bw.write("@data\n");
        String[] nextLine;
        //skip Header
        reader.readNext();
        while ((nextLine = reader.readNext()) != null) {
            String s = formatData(nextLine, attrList, timeInputFormat, timeOutputFormat);
            bw.write(s);
            bw.newLine();
        }
        reader.close();
        bw.close();
        System.out.println("Completed!");
    }

    public String formatHeader(AttrObj attr) {
        StringBuilder formattedHeader = new StringBuilder();
        if (!attr.getType().contentEquals("nominal")) {
            formattedHeader.append("@attribute ")
                    .append(attr.getName())
                    .append(" ")
                    .append(attr.getType());
            if (attr.getType().contentEquals("date")) {
                formattedHeader.append(" ")
                        .append(this.timeOutputFormat);
            }
        } else {
            formattedHeader.append("@attribute ")
                    .append(attr.getName())
                    .append(" ")
                    .append("{");

            ListIterator<String> label = attr.getLabels().listIterator();
            while (label.hasNext()) {
                if (label.hasPrevious()) {
                    formattedHeader.append(",");
                }
                formattedHeader.append("\"").append(label.next()).append("\"");

            }
            formattedHeader.append("}");
        }
        return formattedHeader.toString();
    }

    public String formatData(String[] dataLine,
                             ArrayList<AttrObj> attrList,
                             HashMap<String, String> timeInputFormat,
                             String timeOutputFormat) throws ParseException {
        StringBuilder formattedData = new StringBuilder();
        if (dataLine.length == numOfAttr) {
            for (int i = 0; i < dataLine.length; i++) {
                if (i != 0) {
                    formattedData.append(",");
                }// empty attribute
                if (dataLine[i].isEmpty()) {
                    formattedData.append("?");
                } else {
                    if (attrList.get(i).getType().contentEquals("numeric")) {
                        formattedData.append(dataLine[i]);
                    } else {
                        if (attrList.get(i).getType().contains("date")) {
                            //"date "+ timeInputFormat
                            Pattern p = Pattern.compile("^\\d{1,4}([-/])\\d{1,4}([-/])\\d{1,4}\\s+\\d{1,2}([:-])\\d{1,2}|\\d{1,2}([:-])\\d{1,2}([:-])\\d{1,2}");
                            Matcher m = p.matcher(dataLine[i]);
                            m.find();
                            formattedData.append("\"");
                            formattedData.append(convertTimeFormat(dataLine[i], timeInputFormat.get(m.group(1)), timeOutputFormat));
                        } else {    //type String or Nominal
                            //remove quote in string
                            dataLine[i] = dataLine[i].replace("\"", " ");
                            formattedData.append("\"");
                            formattedData.append(dataLine[i]);
                        }
                        formattedData.append("\"");
                    }
                }
            }
        }
        return formattedData.toString();
    }

    public static String convertTimeFormat(String dt,
                                           String inputFormat,
                                           String outputFormat) throws ParseException {
        String r;
        SimpleDateFormat sdf = new SimpleDateFormat(inputFormat);
        Date dateObj = sdf.parse(dt);
        r = new SimpleDateFormat(outputFormat).format(dateObj);
        return r;
    }

    private static class AttrObj {
        private String name;
        private String type;
        private ArrayList<String> labels;

        public AttrObj(String name, String type, ArrayList<String> labels) {
            this.name = name;
            this.type = type;
            this.labels = labels;
        }

        public AttrObj(String name, String type) {
            this.name = name;
            this.type = type;
            this.labels = null;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public ArrayList<String> getLabels() {
            return labels;
        }

        public void setLabels(ArrayList<String> labels) {
            this.labels = labels;
        }

        @Override
        public String toString() {
            return "AttrObj [name=" + name + ", type=" + type + ", labels=" + labels + "]\n";
        }
    }
}

