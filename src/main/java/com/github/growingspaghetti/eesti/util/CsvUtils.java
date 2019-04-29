package com.github.growingspaghetti.eesti.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.List;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

public class CsvUtils {
    public static List<String[]> readCsv(File file, char separator) throws Exception {
        CSVParser parser
            = new CSVParserBuilder()
                  .withSeparator(separator)
                  .withIgnoreQuotations(false)
                  .build();
        try (FileInputStream   in     = new FileInputStream(file);
             InputStreamReader isr    = new InputStreamReader(in, "UTF-8");
             CSVReader         reader = new CSVReaderBuilder(isr).withCSVParser(parser).build();) {
            return reader.readAll();
        }
    }
}
