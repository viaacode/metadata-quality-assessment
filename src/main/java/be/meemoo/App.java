package be.meemoo;

import com.jayway.jsonpath.InvalidJsonException;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import de.gwdg.metadataqa.api.calculator.CalculatorFacade;
import de.gwdg.metadataqa.api.configuration.ConfigurationReader;
import de.gwdg.metadataqa.api.configuration.MeasurementConfiguration;
import de.gwdg.metadataqa.api.schema.BaseSchema;
import de.gwdg.metadataqa.api.schema.Schema;
import de.gwdg.metadataqa.api.util.CsvReader;
import org.apache.commons.cli.*;
import org.apache.commons.io.FilenameUtils;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;


/**
 * Hello world!
 */
public class App {

    private static final Logger logger = Logger.getLogger(App.class.getCanonicalName());

    private final CalculatorFacade calculator;
    private final BufferedReader inputReader;
    private final String inputFormat;
    private String outputFormat;
    private BufferedWriter outputWriter;

    private Schema schema;


    // Arguments
    private static final String INPUT_FILE = "input";
    private static final String INPUT_FORMAT = "inputFormat";
    private static final String OUTPUT_FILE = "output";
    private static final String OUTPUT_FORMAT = "inputFormat";
    private static final String SCHEMA_CONFIG = "schema";
    private static final String SCHEMA_FORMAT = "schemaFormat";
    private static final String MEASUREMENTS_CONFIG = "measurements";
    private static final String HEADERS_CONFIG = "headers";
    private static final String MEASUREMENTS_FORMAT = "measurementsFormat";
    private static final String CSV = "csv";
    private static final String JSON = "json";
    private static final String YAML = "yaml";



    public App(CommandLine cmd) throws IOException {
        // initialize input
        String inputFile = cmd.getOptionValue(INPUT_FILE);
        Path inputPath = Paths.get(inputFile);
        inputReader = Files.newBufferedReader(inputPath);

        this.inputFormat = cmd.hasOption(INPUT_FORMAT) ? cmd.getOptionValue(INPUT_FORMAT) : FilenameUtils.getExtension(inputFile);

        // initialize output
        if (cmd.hasOption(OUTPUT_FILE)) {
            String outputFile = cmd.getOptionValue(OUTPUT_FILE);
            Path outputPath = Paths.get(outputFile);
            try {
                this.outputWriter = Files.newBufferedWriter(outputPath);
                this.outputFormat = cmd.hasOption(OUTPUT_FORMAT) ? cmd.getOptionValue(OUTPUT_FORMAT) : FilenameUtils.getExtension(outputFile);
            } catch (IOException e) {
                logger.warning(String.format("File %s not found. Printing output to stdout.",
                        outputPath.toString()));
                this.outputWriter = new BufferedWriter(new OutputStreamWriter(System.out));
                this.outputFormat = cmd.hasOption(OUTPUT_FORMAT) ? cmd.getOptionValue(OUTPUT_FORMAT) : JSON;
            }
        } else {
            // write to std out if no file was given
            this.outputWriter = new BufferedWriter(new OutputStreamWriter(System.out));
            this.outputFormat = cmd.hasOption(OUTPUT_FORMAT) ? cmd.getOptionValue(OUTPUT_FORMAT) : JSON;
        }

        // initialize config
        String schemaFile = cmd.getOptionValue(SCHEMA_CONFIG);
        String measurementFile = cmd.getOptionValue(MEASUREMENTS_CONFIG);

        String schemaFormat = cmd.hasOption(SCHEMA_FORMAT) ? cmd.getOptionValue(SCHEMA_FORMAT) : FilenameUtils.getExtension(schemaFile);
        switch (schemaFormat) {
            case YAML:
                this.schema = ConfigurationReader.readSchemaYaml(schemaFile).asSchema();
                break;
            case JSON:
            default:
                this.schema = ConfigurationReader.readSchemaJson(schemaFile).asSchema();
        }

        MeasurementConfiguration measurementConfig;
        String measurementFormat = cmd.hasOption(MEASUREMENTS_FORMAT) ? cmd.getOptionValue(MEASUREMENTS_FORMAT) : FilenameUtils.getExtension(measurementFile);
        switch (measurementFormat) {
            case YAML:
                measurementConfig = ConfigurationReader.readMeasurementYaml(measurementFile);
                break;
            case JSON:
            default:
                measurementConfig = ConfigurationReader.readMeasurementJson(measurementFile);
        }


        this.calculator = new CalculatorFacade(measurementConfig)
                // set the schema which describes the source
                .setSchema(this.schema);
    }

    public static void main(String[] args) throws FileNotFoundException {

        // Take input file

        final Options options = new Options();
        options.addOption(new Option("i", INPUT_FILE, true, "Input file."));
        options.addOption(new Option("u", INPUT_FORMAT, true, "Input file."));

        options.addOption(new Option("s", SCHEMA_CONFIG, true, "Schema file to run assessment against."));
        options.addOption(new Option("v", SCHEMA_FORMAT, true, "Format of schema file."));

        options.addOption(new Option("m", MEASUREMENTS_CONFIG, true, "Config file for measurements."));
        options.addOption(new Option("w", MEASUREMENTS_FORMAT, true, "Format of measurements file."));

        options.addOption(new Option("o", OUTPUT_FILE, false, "Output file."));
        options.addOption(new Option("f", OUTPUT_FORMAT, true, "Output file."));
        options.addOption(new Option("h", HEADERS_CONFIG, true, "Headers to copy from source"));

        // create the parser
        CommandLineParser parser = new DefaultParser();

        // create help formatter
        HelpFormatter formatter = new HelpFormatter();

        // Check how many arguments were passed in
        if (args.length == 0 || args.length < 3) {
            formatter.printHelp("java -jar target/meemoo-qa-api-1.0-SNAPSHOT-shaded.jar", options);
            System.exit(0);
        }

        try {
            // parse the command line arguments
            CommandLine cmd = parser.parse(options, args);
            new App(cmd).run();
        } catch (ParseException exp) {
            formatter.printHelp("java -jar target/meemoo-qa-api-1.0-SNAPSHOT-shaded.jar", options);
            // oops, something went wrong
            System.err.println("Parsing failed.  Reason: " + exp.getMessage());
        }
    }

    public void run() {

        try {

            final CSVWriter csvWriter = new CSVWriter(outputWriter);
            // print header
            List<String> header = new ArrayList<>();
            header.add("fragment_id_mam");

            header.addAll(calculator.getHeader());
            // Switch headers
            List<String> outputHeader = header.stream().map(s -> s.replaceAll("(:|/|\\.)","_").toLowerCase()).collect(Collectors.toList());;

            csvWriter.writeNext(outputHeader.toArray(new String[0]));


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
        long counter = 0;

        String record = null;
        while ((record = inputReader.readLine()) != null) {
            try {
                JSONObject obj = new JSONObject(record);

                List<String> results = new ArrayList<>();
                results.add(obj.getString("fragment_id_mam"));
                results.addAll(calculator.measureAsList(record)); // Add QA results

                // Write results to CSV
                csvWriter.writeNext(results.toArray(new String[0]));
                // update process
                counter++;
                if (counter % 50 == 0) {
                    logger.info(String.format("Assessed fragment %s. Processed %s records. ", obj.getString("fragment_id_mam"), counter));
                }
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
        final CSVReader csvReader = new CSVReader(inputReader);

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
                    results.add(strings.get(0)); // Add ID to results
                    results.addAll(calculator.measureAsList(strings)); // Add QA results

                    // Write results to CSV
                    csvWriter.writeNext(results.toArray(new String[0]));
                    // update process
                    counter++;
                    if (counter % 50 == 0) {
                        logger.info(String.format("Assessed fragment %s. Processed %s records. ", strings.get(0), counter));
                    }
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

    public void writeResults(List<String> results) {

    }

    public void printProgress() {

    }


}


