package be.meemoo;

import com.jayway.jsonpath.InvalidJsonException;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderHeaderAware;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import de.gwdg.metadataqa.api.calculator.CalculatorFacade;
import de.gwdg.metadataqa.api.configuration.ConfigurationReader;
import de.gwdg.metadataqa.api.schema.CsvAwareSchema;
import de.gwdg.metadataqa.api.schema.Schema;
import de.gwdg.metadataqa.api.util.CsvReader;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Logger;

/**
 * Hello world!
 */
public class App {

    private static final Logger logger = Logger.getLogger(App.class.getCanonicalName());

    public static void main(String[] args) throws FileNotFoundException {

        // Take input file
        // Check how many arguments were passed in
        if (args.length == 0 || args.length < 3) {
            System.out.println("java -jar target/meemoo-qa-api-1.0-SNAPSHOT-shaded.jar <input csv> <schema file> <output csv>");
            System.exit(0);
        }
        String inputFile = args[0];
        String schemaFile = args[1];
        String outputFile = args[2];

        // Instantiate schema
        Schema schema = ConfigurationReader
                .readYaml(schemaFile)
                .asSchema();

        // Define measurements
        CalculatorFacade calculator = new CalculatorFacade()
                // set the schema which describes the source
                .setSchema(schema)
                // right now it is a CSV source, so we set how to parse it
                .setCsvReader(
                        new CsvReader()
                                .setHeader(((CsvAwareSchema) schema).getHeader()))
                .enableCompletenessMeasurement()
                .enableFieldCardinalityMeasurement();

        try {
            // initialize lines stream
            final Path inputPath = Paths.get(inputFile);
            final Path outputPath = Paths.get(outputFile);

            // Configure input
            BufferedReader csvBufferedReader = Files.newBufferedReader(inputPath);
            final CSVReaderHeaderAware csvReader = new CSVReaderHeaderAware(csvBufferedReader);

            // Configure output
            BufferedWriter csvBufferedWriter = Files.newBufferedWriter(outputPath);
            final CSVWriter csvWriter = new CSVWriter(csvBufferedWriter);

            // print header
            csvWriter.writeNext(calculator.getHeader().toArray(new String[0]));

            String[] record = null;
            while ((record = csvReader.readNext()) != null) {
                try {
                    String csvRecord = CsvReader.toCsv(record); // Serialize as CSV
                    List<String> result = calculator.measureAsList(csvRecord);
                    csvWriter.writeNext(result.toArray(new String[0]));
                    // save csv
                } catch (InvalidJsonException e) {
                    // handle exception
                    logger.severe(String.format("Invalid JSON in %s: %s. Error message: %s.",
                            inputPath.toString(), record, e.getLocalizedMessage()));
                }
            }

            csvWriter.close();
        } catch (CsvValidationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


