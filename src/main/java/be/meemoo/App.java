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
import org.apache.commons.cli.*;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;


/**
 * Hello world!
 */
public class App {

    private static final Logger logger = Logger.getLogger(App.class.getCanonicalName());

    private final Schema schema;
    private final CalculatorFacade calculator;
    private final Path inputPath;
    private final Path outputPath;

    public App(CommandLine cmd) throws FileNotFoundException {
        String inputFile = cmd.getOptionValue("input");
        String outputFile = cmd.getOptionValue("output");

        // initialize lines stream
        this.inputPath = Paths.get(inputFile);
        this.outputPath = Paths.get(outputFile);

        String schemaFile = cmd.getOptionValue("schema");
        this.schema = ConfigurationReader
                .readYaml(schemaFile)
                .asSchema();

        this.calculator = new CalculatorFacade()
                // set the schema which describes the source
                .setSchema(schema)
                // Define measurements
                .enableCompletenessMeasurement()
                .enableFieldCardinalityMeasurement();

    }

    public static void main(String[] args) throws FileNotFoundException {

        // Take input file
        // Check how many arguments were passed in
        if (args.length == 0 || args.length < 3) {
            System.out.println("java -jar target/meemoo-qa-api-1.0-SNAPSHOT-shaded.jar <input csv> <schema file> <output csv>");
            System.exit(0);
        }

        final Options options = new Options();
        options.addOption(new Option("i", "input", true, "Input CSV file."));
        options.addOption(new Option("s", "schema", true, "Schema file to run assessment against."));
        options.addOption(new Option("o", "output", true, "Output CSV file."));
        options.addOption(new Option("f", "format", true, "Output CSV file."));

        // create the parser
        CommandLineParser parser = new DefaultParser();
        try {
            // parse the command line arguments
            CommandLine cmd = parser.parse(options, args);
            new App(cmd).run();
        } catch (ParseException exp) {

            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("ant", options);
            // oops, something went wrong
            System.err.println("Parsing failed.  Reason: " + exp.getMessage());
        }
    }

    public void run() {

        try {
            // Configure output
            BufferedWriter csvBufferedWriter = Files.newBufferedWriter(this.outputPath);
            final CSVWriter csvWriter = new CSVWriter(csvBufferedWriter);

            // print header
            List<String> header = new ArrayList<>();
            header.add("fragment_id_mam");
            header.add("mediaobject_id_mam");
            header.add("ie_id");

            header.addAll(calculator.getHeader());

            csvWriter.writeNext(header.toArray(new String[0]));


            switch (this.schema.getFormat()) {
                case CSV:
                    runCSV(csvWriter);
                    break;
                case JSON:
                    runJSON(csvWriter);
                    break;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void runJSON(CSVWriter csvWriter) throws IOException {
        // Configure input
        BufferedReader jsonBufferedReader = Files.newBufferedReader(this.inputPath);

        long counter = 0;

        String record = null;
        while ((record = jsonBufferedReader.readLine()) != null) {
            try {
                JSONObject obj = new JSONObject(record);

                List<String> results = new ArrayList<>();
                results.add(obj.getString("fragment_id_mam"));
                results.add(obj.getString("mediaobject_id_mam"));
                results.add(obj.getString("ie_id"));
                results.addAll(calculator.measureAsList(record)); // Add QA results

                // Write results to CSV
                csvWriter.writeNext(results.toArray(new String[0]));
                // update process
                counter++;
                logger.info(String.format("Assessed fragment %s. Processed %s records. ", obj.getString("fragment_id_mam"), counter));
            } catch (InvalidJsonException e) {
                // handle exception
                logger.severe(String.format("Invalid JSON in %s: %s. Error message: %s.",
                        inputPath.toString(), record, e.getLocalizedMessage()));
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        }

        logger.info(String.format("Assessment completed successfully with %s records. ", counter));
        csvWriter.close();
    }

    public void runCSV(CSVWriter csvWriter) throws IOException {

        // Configure input
        BufferedReader csvBufferedReader = Files.newBufferedReader(this.inputPath);
        final CSVReader csvReader = new CSVReader(csvBufferedReader);

        long counter = 0;
        try {
            // read header
            String[] header = csvReader.readNext();

            // right now it is a CSV source, so we set how to parse it
            this.calculator.setCsvReader(
                    new CsvReader().setHeader(Arrays.asList(header)));

            String[] record = null;
            while ((record = csvReader.readNext()) != null) {
                try {
                    List<String> strings = Arrays.asList(record);

                    List<String> results = new ArrayList<>();
                    results.addAll(strings.subList(0, 3)); // Add IDs to results
                    results.addAll(calculator.measureAsList(strings)); // Add QA results

                    // Write results to CSV
                    csvWriter.writeNext(results.toArray(new String[0]));
                    // update process
                    counter++;
                    logger.info(String.format("Assessed fragment %s. Processed %s records. ", strings.get(0), counter));
                } catch (InvalidJsonException e) {
                    // handle exception
                    logger.severe(String.format("Invalid JSON in %s: %s. Error message: %s.",
                            inputPath.toString(), record, e.getLocalizedMessage()));
                } catch (Exception e) {
                    logger.severe(String.format("Measurement failed at record %s with %s columns (expected %s)", counter + 1, record.length, this.calculator.getHeader().size()));
                    logger.severe(Arrays.toString(record));
                    e.printStackTrace();
                    throw e;
                }
            }

            logger.info(String.format("Assessment completed successfully with %s records. ", counter));
        } catch (CsvValidationException e) {
            logger.severe(String.format("Assessment failed with %s records. ", counter));
            e.printStackTrace();
        } finally {
            csvWriter.close();
        }

    }


}


