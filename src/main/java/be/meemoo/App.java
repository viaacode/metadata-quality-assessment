package be.meemoo;

import com.jayway.jsonpath.InvalidJsonException;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import de.gwdg.metadataqa.api.calculator.CalculatorFacade;
import de.gwdg.metadataqa.api.configuration.ConfigurationReader;
import de.gwdg.metadataqa.api.configuration.MeasurementConfiguration;
import de.gwdg.metadataqa.api.schema.Schema;
import de.gwdg.metadataqa.api.util.CsvReader;
import org.apache.commons.cli.*;
import org.apache.commons.io.FilenameUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
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
    // Arguments
    private static final String INPUT_FILE = "input";
    private static final String OUTPUT_FILE = "output";
    private static final String OUTPUT_FORMAT = "outputFormat";
    private static final String SCHEMA_CONFIG = "schema";
    private static final String SCHEMA_FORMAT = "schemaFormat";
    private static final String MEASUREMENTS_CONFIG = "measurements";
    private static final String HEADERS_CONFIG = "headers";
    private static final String MEASUREMENTS_FORMAT = "measurementsFormat";
    private static final String CSV = "csv";
    private static final String JSON = "json";
    private static final String YAML = "yaml";
    private final CalculatorFacade calculator;
    private final BufferedReader inputReader;
    private final Schema schema;
    private String outputFormat;
    private BufferedWriter outputWriter;

    public App(CommandLine cmd) throws IOException {
        // initialize input
        String inputFile = cmd.getOptionValue(INPUT_FILE);
        Path inputPath = Paths.get(inputFile);
        inputReader = Files.newBufferedReader(inputPath);

        // initialize output
        if (cmd.hasOption(OUTPUT_FILE)) {
            String outputFile = cmd.getOptionValue(OUTPUT_FILE);
            Path outputPath = Paths.get(outputFile);
            try {
                this.outputWriter = Files.newBufferedWriter(outputPath);
                this.outputFormat = cmd.hasOption(OUTPUT_FORMAT) ? cmd.getOptionValue(OUTPUT_FORMAT) : FilenameUtils.getExtension(outputFile);
            } catch (IOException e) {
                logger.warning(String.format("File %s not found. Printing output to stdout.",
                        outputPath));
                this.outputWriter = new BufferedWriter(new OutputStreamWriter(System.out));
                this.outputFormat = cmd.getOptionValue(OUTPUT_FORMAT, JSON);
            }
        } else {
            // write to std out if no file was given
            this.outputWriter = new BufferedWriter(new OutputStreamWriter(System.out));
            this.outputFormat = cmd.getOptionValue(OUTPUT_FORMAT, JSON);
        }

        // initialize config
        String schemaFile = cmd.getOptionValue(SCHEMA_CONFIG);

        String schemaFormat = cmd.getOptionValue(SCHEMA_FORMAT, FilenameUtils.getExtension(schemaFile));
        switch (schemaFormat) {
            case YAML:
                this.schema = ConfigurationReader.readSchemaYaml(schemaFile).asSchema();
                break;
            case JSON:
            default:
                this.schema = ConfigurationReader.readSchemaJson(schemaFile).asSchema();
        }

        MeasurementConfiguration measurementConfig = new MeasurementConfiguration();
        if (cmd.hasOption(MEASUREMENTS_CONFIG)) {
            String measurementFile = cmd.getOptionValue(MEASUREMENTS_CONFIG);
            String measurementFormat = cmd.getOptionValue(MEASUREMENTS_FORMAT, FilenameUtils.getExtension(measurementFile));
            switch (measurementFormat) {
                case YAML:
                    measurementConfig = ConfigurationReader.readMeasurementYaml(measurementFile);
                    break;
                case JSON:
                default:
                    measurementConfig = ConfigurationReader.readMeasurementJson(measurementFile);
            }
        }

        this.calculator = new CalculatorFacade(measurementConfig)
                // set the schema which describes the source
                .setSchema(this.schema);


        // Set the fields supplied by the command line to extractable fields
        if (cmd.hasOption(HEADERS_CONFIG)) {
            String[] headers = cmd.getOptionValues(HEADERS_CONFIG);
            for (String h : headers) {
                this.schema.addExtractableField(h, this.schema.getPathByLabel(h).getJsonPath());
            }
        }
    }

    public static void main(String[] args) {

        // Take input file

        final Options options = new Options();
        options.addOption(new Option("i", INPUT_FILE, true, "Input file."));

        options.addOption(new Option("s", SCHEMA_CONFIG, true, "Schema file to run assessment against."));
        options.addOption(new Option("v", SCHEMA_FORMAT, true, "Format of schema file."));

        options.addOption(new Option("m", MEASUREMENTS_CONFIG, true, "Config file for measurements."));
        options.addOption(new Option("w", MEASUREMENTS_FORMAT, true, "Format of measurements file."));

        options.addOption(new Option("o", OUTPUT_FILE, true, "Output file."));
        options.addOption(new Option("f", OUTPUT_FORMAT, true, "Output format."));

        options.addOption(new Option("h", HEADERS_CONFIG, true, "Headers to copy from source"));

        // create the parser
        CommandLineParser parser = new DefaultParser();

        // Check how many arguments were passed in
        if (args.length == 0 || args.length < 3) {
            printHelp(options);
        }

        try {
            // parse the command line arguments
            CommandLine cmd = parser.parse(options, args);
            new App(cmd).run();
        } catch (Exception ex) {
            System.err.println("Error: " + ex.getMessage());
            printHelp(options);
        }
    }

    private static void printHelp(Options options) {
        // create help formatter
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("java -jar target/metadata-qa-api-cmd-1.0-SNAPSHOT-shaded.jar", options);
        System.exit(0);
    }

    public void run() throws IOException {
        switch (outputFormat) {
            case CSV:
                switch (this.schema.getFormat()) {
                    case CSV:
                        processCSVasCSV();
                        break;
                    case JSON:
                        processJSONasCSV();
                        break;
                }
                break;
            case JSON:
            default:
                switch (this.schema.getFormat()) {
                    case CSV:
                        processCSVasJSON();
                        break;
                    case JSON:
                        processJSONasJSON();
                        break;
                }
                break;
        }
    }

    private void printCsvHeader(CSVWriter csvWriter) {
        // print header
        List<String> header = new ArrayList<>();
        header.addAll(calculator.getHeader());
        // Switch headers
        List<String> outputHeader = header.stream()
                .map(s -> s.replaceAll("(:|/|\\.)", "_")
                        .toLowerCase()).collect(Collectors.toList());

        csvWriter.writeNext(outputHeader.toArray(new String[0]));
    }

    public void processJSONasCSV() throws IOException {
        final CSVWriter csvWriter = new CSVWriter(outputWriter);
        printCsvHeader(csvWriter);

        long counter = 0;
        String record = null;
        while ((record = inputReader.readLine()) != null) {
            try {
                // Add QA results
                List<String> results = calculator.measureAsList(record);

                // Write results to CSV
                csvWriter.writeNext(results.toArray(new String[0]));

                // update process
                counter++;
                if (counter % 50 == 0) {
                    logger.info(String.format("Processed %s records. ", counter));
                }
            } catch (InvalidJsonException e) {
                // handle exception
                logger.severe(String.format("Invalid JSON: %s. Error message: %s.", record, e.getLocalizedMessage()));
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        }

        csvWriter.close();
    }

    public void processJSONasJSON() throws IOException {
        long counter = 0;

        String record = null;
        while ((record = inputReader.readLine()) != null) {
            try {
                // Add QA results
                String s = calculator.measureAsJson(record);

                outputWriter.write(s);
                outputWriter.newLine();

                // update process
                counter++;
                if (counter % 50 == 0) {
                    logger.info(String.format("Processed %s records. ", counter));
                }
            } catch (InvalidJsonException e) {
                // handle exception
                logger.severe(String.format("Invalid JSON: %s. Error message: %s.", record, e.getLocalizedMessage()));
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        }

        logger.info(String.format("Assessment completed successfully with %s records. ", counter));
        outputWriter.close();
    }

    public void processCSVasCSV() throws IOException {

        final CSVWriter csvWriter = new CSVWriter(outputWriter);
        printCsvHeader(csvWriter);

        // Configure input
        final CSVReader csvReader = new CSVReader(inputReader);

        long counter = 0;
        try {
            // read header
            final List<String> header = Arrays.asList(csvReader.readNext());

            // right now it is a CSV source, so we set how to parse it
            this.calculator.setCsvReader(
                    new CsvReader().setHeader(header));

            String[] record = null;
            while ((record = csvReader.readNext()) != null) {
                try {
                    final List<String> strings = Arrays.asList(record);
                    final List<String> results = calculator.measureAsList(strings);

                    // Write results to CSV
                    csvWriter.writeNext(results.toArray(new String[0]));

                    // update process
                    counter++;
                    if (counter % 50 == 0) {
                        logger.info(String.format("Processed %s records. ", counter));
                    }
                } catch (InvalidJsonException e) {
                    // handle exception
                    logger.severe(String.format("Invalid JSON: %s. Error message: %s.", record, e.getLocalizedMessage()));
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

    public void processCSVasJSON() throws IOException {

        // Configure input
        final CSVReader csvReader = new CSVReader(inputReader);

        long counter = 0;
        try {
            // read header
            final List<String> header = Arrays.asList(csvReader.readNext());

            // right now it is a CSV source, so we set how to parse it
            this.calculator.setCsvReader(
                    new CsvReader().setHeader(header));

            String[] record = null;
            while ((record = csvReader.readNext()) != null) {
                try {
                    final List<String> strings = Arrays.asList(record);
                    final String s = calculator.measureAsJson(strings);

                    outputWriter.write(s);
                    outputWriter.newLine();

                    // update process
                    counter++;
                    if (counter % 50 == 0) {
                        logger.info(String.format("Processed %s records. ", counter));
                    }
                } catch (InvalidJsonException e) {
                    // handle exception
                    logger.severe(String.format("Invalid JSON: %s. Error message: %s.", record, e.getLocalizedMessage()));
                } catch (Exception e) {
                    logger.severe(String.format("Measurement failed at record %s with %s columns (expected %s)", counter + 1, record.length, this.calculator.getHeader().size()));
                    logger.severe(Arrays.toString(record));
                    logger.severe(e.getMessage());
                    throw e;
                }
            }

            logger.info(String.format("Assessment completed successfully with %s records. ", counter));
        } catch (CsvValidationException e) {
            logger.severe(String.format("Assessment failed with %s records. ", counter));
            logger.severe(e.getMessage());
        } finally {
            outputWriter.close();
        }
    }
}

