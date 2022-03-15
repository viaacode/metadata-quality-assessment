package be.meemoo;

import com.opencsv.exceptions.CsvValidationException;
import de.gwdg.metadataqa.api.calculator.CalculatorFacade;
import de.gwdg.metadataqa.api.schema.Schema;
import org.apache.commons.io.FilenameUtils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

public class RecordFactory {

    private static Logger logger;

    public static RecordReader getRecordReader(String inputFile, CalculatorFacade calculator, boolean gzip) throws CsvValidationException, IOException {
        final Schema schema = calculator.getSchema();

        BufferedReader inputReader;
        if (gzip) {
            FileInputStream fis = new FileInputStream(inputFile);
            GZIPInputStream gis = new GZIPInputStream(fis);
            InputStreamReader inputStreamReader = new InputStreamReader(gis, "UTF-8");
            inputReader = new BufferedReader(inputStreamReader);
        } else {
            Path inputPath = Paths.get(inputFile);
            inputReader = Files.newBufferedReader(inputPath);
        }

        switch (schema.getFormat()) {
            case CSV:
                return new CSVRecordReader(inputReader, calculator);
            case JSON:
                return new JSONRecordReader(inputReader, calculator);
        }
        return new CSVRecordReader(inputReader, calculator);
    }

    public static ResultWriter getResultWriter(String outputFormat, String outputFile) throws IOException {

        if (outputFormat == null) {
            outputFormat = FilenameUtils.getExtension(outputFile);
        }

        switch (outputFormat) {
            case App.CSV:
                return new CSVResultWriter(outputFile);
            case App.NDJSON:
                return new JSONResultWriter(outputFile);
            case App.CSVJSON:
                return new CSVJSONResultWriter(outputFile);
        }

        return new CSVResultWriter(outputFile);
    }

    public static ResultWriter getResultWriter(String outputFormat) throws IOException {
        switch (outputFormat) {
            case App.CSV:
                return new CSVResultWriter();
            case App.NDJSON:
                return new JSONResultWriter();
            case App.CSVJSON:
                return new CSVJSONResultWriter();
        }
        return new CSVResultWriter();
    }

}
