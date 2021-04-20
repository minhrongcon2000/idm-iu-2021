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
	private static  String source_path;
	private static String des_path;
	private static String attr_conf;
	private static String delimiter = ",";
	private static int attr_num;

	public CSV2ARFFConverter(String source_path, String des_path, String attr_conf) throws org.json.simple.parser.ParseException, ParseException {
		CSV2ARFFConverter.source_path = source_path;
		CSV2ARFFConverter.des_path = des_path;
		CSV2ARFFConverter.attr_conf = attr_conf;		
		getAttrNum();			
	}



	public static int getNumLines() throws IOException {
		try {
			BufferedReader br = new BufferedReader(new FileReader(source_path));
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
			JSONArray attr = (JSONArray) new JSONParser().parse(new FileReader(attr_conf));
			CSV2ARFFConverter.attr_num = attr.size();			
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}

	public static boolean isDataAttributeConsistent() {
		boolean isConsistent = true;
		try {
			CSVReader csvReader = new CSVReader(new FileReader(source_path));
			String[] nextRecord;
			//Skip column names
			nextRecord = csvReader.readNext();
			int count = 1;
			while ((nextRecord = csvReader.readNext()) != null) {
				count++;
				if (nextRecord.length != attr_num)
				{					
					System.out.println("Inconsistent data at line " + count +"\n");
					isConsistent = false;
					break;
				}	
			}
		}catch(IOException e){
			e.printStackTrace();

		}
		return isConsistent;
	}


	public static ArrayList<AttrObj> getAttr() throws FileNotFoundException, IOException, ParseException {
		try {
			JSONArray attr_json = (JSONArray) new JSONParser().parse(new FileReader(attr_conf));
			ArrayList<AttrObj> attr_list=new ArrayList<AttrObj>();  
			for (int i = 0; i < attr_json.size(); i++) {
				JSONObject o = (JSONObject) attr_json.get(i);				
				JSONArray labels = (JSONArray) o.get("labels");				
				
				if (labels != null) {
					ArrayList<String> temp = new ArrayList<String>();
					Iterator l = labels.iterator();					
					while (l.hasNext()) {
						temp.add((String) l.next());
					}
					
					AttrObj attr_obj = new AttrObj((String)o.get("name"), (String)o.get("type"), temp);
					attr_list.add(attr_obj);
										
				}
				else {
					AttrObj attr_obj = new AttrObj((String)o.get("name"), (String)o.get("type"));
					attr_list.add(attr_obj);	
				}			
			}
			return(attr_list);
		}catch(Exception e){
			e.fillInStackTrace();
		}
		return null;
	}
	
	public void convert(String relationName, HashMap<String, String> timeInputFormat, String timeOutputFormat) {
		try {
			CSVReader reader = new CSVReader(new FileReader(source_path));
			BufferedWriter bw = new BufferedWriter(new FileWriter(des_path));
			//Write Header
			bw.write("@relation " + relationName + "\n\n");
			ArrayList<AttrObj> attr_list = getAttr();
			//System.out.println(attr_list);
			for (int i = 0; i < attr_list.size(); i++) {				
				if (!attr_list.get(i).getType().contentEquals("nominal")) {
					bw.write("@attribute " + attr_list.get(i).getName() + " " + attr_list.get(i).getType());
				}else {
					bw.write("@attribute " + attr_list.get(i).getName() + " "); 
					bw.write("{");
					//+ attr_list.get(i).getLabels());
					//ArrayList<String> labels = attr_list.get(i).getLabels();
					ListIterator<String> label = attr_list.get(i).getLabels().listIterator();
					while (label.hasNext()) {
						if (label.hasPrevious()) {
							bw.write(",");
						}
						bw.write("\""+label.next()+"\"");
						
					}
					bw.write("}");
				}				
				bw.newLine();
			}
			//Write Data
			bw.write("@data\n");			
			String[] nextLine;
			//skip Header
			reader.readNext();
;			while ((nextLine = reader.readNext()) != null) {
				String s = formatData(nextLine, attr_list, timeInputFormat, timeOutputFormat);
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
	
	public static String  formatData(String[] dataLine, ArrayList<AttrObj> attrList,HashMap<String, String> timeInputFormat, String timeOutputFormat) throws ParseException {
		StringBuilder formattedData = new StringBuilder();
		if (dataLine.length==attr_num) {			
			for (int i = 0; i < dataLine.length; i++) {				
				if(dataLine[i].isBlank()) {
					 formattedData.append("?");
				}else {					
					if(attrList.get(i).getType().contentEquals("numeric")) {
						formattedData.append(dataLine[i]);				
				}else {
					//"date "+ timeInputFormat
					if (attrList.get(i).getType().contentEquals(new StringBuilder("date \"dd-MM-yy HH:mm\""))){
						Pattern p = Pattern.compile("^\\d{1,4}([-/])\\d{1,4}([-/])\\d{1,4}\\s+\\d{1,2}([:-])\\d{1,2}|\\d{1,2}([:-])\\d{1,2}([:-])\\d{1,2}");
						Matcher m = p.matcher(dataLine[i]);
						m.find();
						formattedData.append("\"");						
						formattedData.append(convertTimeFormat(dataLine[i], timeInputFormat.get(m.group(1)), timeOutputFormat));
						formattedData.append("\"");
				}else {	//type String or Nominal						
						dataLine[i] = dataLine[i].replace("\"","");						
						formattedData.append("\"");
						formattedData.append(dataLine[i]);
						formattedData.append("\"");
					}					
				}
				
			}
				if (i!=(dataLine.length-1)) {
					formattedData.append(",");
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
}

