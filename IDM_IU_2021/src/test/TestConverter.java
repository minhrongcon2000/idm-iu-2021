package test;

import data.CSV2ARFFConverter;
import data.ConfigDataInconsistentException;

import java.io.IOException;
import java.text.ParseException;

public class TestConverter {
    public static void main(String[] args) throws org.json.simple.parser.ParseException,
            IOException, ConfigDataInconsistentException, ParseException {
        String src = "./data/data.csv";
        String dest = "./data/data.arff";
        String config = "./datatype.json";
        CSV2ARFFConverter converter = new CSV2ARFFConverter(src, dest, config);
        converter.convert();
    }
}
