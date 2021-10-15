package be.meemoo;

import com.opencsv.exceptions.CsvValidationException;
import de.gwdg.metadataqa.api.calculator.CalculatorFacade;
import de.gwdg.metadataqa.api.configuration.MeasurementConfiguration;
import de.gwdg.metadataqa.api.schema.Schema;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.util.logging.Logger;

public class RecordFactory {

    private static Logger logger;

    public static RecordReader getRecordReader(String inputFile, CalculatorFacade calculator) throws CsvValidationException, IOException {
        final Schema schema = calculator.getSchema();
        switch (schema.getFormat()) {
            case CSV:
                return new CSVRecordReader(inputFile, calculator);
            case JSON:
                return new JSONRecordReader(inputFile, calculator);
        }
        return new CSVRecordReader(inputFile, calculator);
    }

    public static ResultWriter getResultWriter(String outputFormat, String outputFile) throws IOException {

        if (outputFormat == null) {
            outputFormat = FilenameUtils.getExtension(outputFile);
        }

        switch (outputFormat) {
            case "csv":
                return new CSVResultWriter(outputFile);
            case "ndjson":
            case "json":
                return new JSONResultWriter(outputFile);
        }


        return new CSVResultWriter(outputFile);
    }

    public static ResultWriter getResultWriter(String outputFormat) throws IOException {
        switch (outputFormat) {
            case "csv":
                return new CSVResultWriter();
            case "json":
                return new JSONResultWriter();
        }
        return new JSONResultWriter();
    }

}
