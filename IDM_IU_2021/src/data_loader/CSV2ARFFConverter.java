package Converter;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.ListIterator;
import org.json.simple.*;
import org.json.simple.parser.*;
import com.opencsv.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class CSV2ARFFConverter {
	private static  String sourcePath;
	private static String desPath;
	private static String attrConf;
	private static int numOfAttr;

	public CSV2ARFFConverter(String sourcePath, String desPath, String attrConf) throws ParseException, ParseException, org.json.simple.parser.ParseException {
		CSV2ARFFConverter.sourcePath = sourcePath;
		CSV2ARFFConverter.desPath = desPath;
		CSV2ARFFConverter.attrConf = attrConf;		
		getAttrNum();			
	}
	public static int getNumLines() throws IOException {
		try {
			BufferedReader br = new BufferedReader(new FileReader(sourcePath));
			int count = 0;
			while(br.readLine() != null) {
				count++;
			}
			br.close();
			return count;
		}catch(IOException e){
			e.printStackTrace();
		}
		return -1;
	}

	public static void getAttrNum() throws org.json.simple.parser.ParseException, ParseException {
		try {
			JSONArray attr = (JSONArray) new JSONParser().parse(new FileReader(attrConf));
			CSV2ARFFConverter.numOfAttr = attr.size();			
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}

	public static boolean isDataAttributeConsistent() {
		boolean isConsistent = true;
		try {
			CSVReader csvReader = new CSVReader(new FileReader(sourcePath));
			String[] nextRecord;
			//Skip column names
			nextRecord = csvReader.readNext();
			int count = 1;
			while ((nextRecord = csvReader.readNext()) != null) {
				count++;
				if (nextRecord.length != numOfAttr)
				{					
					System.out.println("Inconsistent data at line " + count +"\n");
					isConsistent = false;
					break;
				}	
			}
			csvReader.close();
		}catch(IOException e){
			e.printStackTrace();

		}
		return isConsistent;
	}

	public static ArrayList<AttrObj> getAttrList() throws FileNotFoundException, IOException, ParseException {
		try {
			JSONArray attrJson = (JSONArray) new JSONParser().parse(new FileReader(attrConf));
			ArrayList<AttrObj> attrList=new ArrayList<AttrObj>();  
			for (int i = 0; i < attrJson.size(); i++) {
				JSONObject o = (JSONObject) attrJson.get(i);				
				JSONArray labels = (JSONArray) o.get("labels");	
				if (labels != null) {
					ArrayList<String> temp = new ArrayList<String>();
					Iterator<?> l = labels.iterator();					
					while (l.hasNext()) {
						temp.add((String) l.next());
					}
					AttrObj attrObj = new AttrObj((String)o.get("name"), (String)o.get("type"), temp);
					attrList.add(attrObj);
				}
				else {
					AttrObj attrObj = new AttrObj((String)o.get("name"), (String)o.get("type"));
					attrList.add(attrObj);	
				}			
			}
			return(attrList);
		}catch(Exception e){
			e.fillInStackTrace();
		}
		return null;
	}

	public static void convert(String relationName, HashMap<String, String> timeInputFormat, String timeOutputFormat) {
		try {
			CSVReader reader = new CSVReader(new FileReader(sourcePath));
			BufferedWriter bw = new BufferedWriter(new FileWriter(desPath));
			//Write Header
			bw.write("@relation " + relationName + "\n\n");
			ArrayList<AttrObj> attrList = getAttrList();
			for (int i = 0; i < attrList.size(); i++) {				
				String s = formatHeader(attrList.get(i));
				bw.write(s);
				bw.newLine();
			}
			bw.newLine();
			//Write Data
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
			System.out.println("done");
		}catch(Exception e) {
			e.fillInStackTrace();
		}
	}

	public static String formatHeader(AttrObj attr) {
		StringBuilder formattedHeader = new StringBuilder();
		if (!attr.getType().contentEquals("nominal")) {
			formattedHeader.append("@attribute " + attr.getName() + " " + attr.getType());
		}else {
			formattedHeader.append("@attribute " + attr.getName() + " "); 
			formattedHeader.append("{");
			ListIterator<String> label = attr.getLabels().listIterator();			
			while (label.hasNext()) {
				if (label.hasPrevious()) {
					formattedHeader.append(",");
				}
				formattedHeader.append("\""+label.next()+"\"");

			}
			formattedHeader.append("}");
		}
		return formattedHeader.toString();
	}

	public static String formatData(String[] dataLine, ArrayList<AttrObj> attrList,HashMap<String, String> timeInputFormat, String timeOutputFormat) throws ParseException {
		StringBuilder formattedData = new StringBuilder();
		if (dataLine.length==numOfAttr) {	
			for (int i = 0; i < dataLine.length; i++) {
				if (i!=0) {
					formattedData.append(",");
				}// empty attribute
				if(dataLine[i].isBlank()) {
					formattedData.append("?");					
				}
				else {
					if(attrList.get(i).getType().contentEquals("numeric")) {
						formattedData.append(dataLine[i]);			
					}
					else {
						if (attrList.get(i).getType().contains("date")){
							//"date "+ timeInputFormat					
							Pattern p = Pattern.compile("^\\d{1,4}([-/])\\d{1,4}([-/])\\d{1,4}\\s+\\d{1,2}([:-])\\d{1,2}|\\d{1,2}([:-])\\d{1,2}([:-])\\d{1,2}");
							Matcher m = p.matcher(dataLine[i]);
							m.find();
							formattedData.append("\"");						
							formattedData.append(convertTimeFormat(dataLine[i], timeInputFormat.get(m.group(1)), timeOutputFormat));
							formattedData.append("\"");
						}
						else {	//type String or Nominal		
							//remove quote in string
							dataLine[i] = dataLine[i].replace("\""," ");						
							formattedData.append("\"");
							formattedData.append(dataLine[i]);
							formattedData.append("\"");
						}		
					}						
				}
			}
		}
		return formattedData.toString();
	}

	public static String convertTimeFormat(String dt, String inputFormat, String outputFormat) throws ParseException{
		String r = null;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(inputFormat);
			Date dateObj = sdf.parse(dt);
			r = new SimpleDateFormat(outputFormat).format(dateObj);
		}catch(final ParseException e) {
			e.printStackTrace();
		}		
		return r;
	}

	public static class AttrObj {
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
		public  ArrayList<String> getLabels() {
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
	/* Run converter example
	 * 
	 public static void main(String[] args) throws IOException, ParseException, Exception {
		// TODO Auto-generated method stub
		CSV2ARFFConverter c = new CSV2ARFFConverter("..\\new\\data.csv", "data_1.arff", "..\\new\\datatype.json");
		HashMap<String,String> timeInputFormat = new HashMap<String, String>();
		timeInputFormat.put("/", "dd/MM/yy HH:mm");
		timeInputFormat.put("-", "dd-MM-yy HH:mm");
		convert("wekadata", timeInputFormat, "dd-MM-yy KK:mma");
	}	 
	 * 
	 */
}

