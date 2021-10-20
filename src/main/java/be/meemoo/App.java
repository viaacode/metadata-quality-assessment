package be.meemoo;

import com.opencsv.exceptions.CsvValidationException;
import de.gwdg.metadataqa.api.calculator.CalculatorFacade;
import de.gwdg.metadataqa.api.configuration.ConfigurationReader;
import de.gwdg.metadataqa.api.configuration.MeasurementConfiguration;
import de.gwdg.metadataqa.api.interfaces.MetricResult;
import de.gwdg.metadataqa.api.schema.Schema;
import org.apache.commons.cli.*;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;


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
    private static final String JSON = "json";
    private static final String YAML = "yaml";
    private final Schema schema;
    private final CalculatorFacade calculator;
    private final ResultWriter outputWriter;
    private final RecordReader inputReader;

    public App(CommandLine cmd) throws IOException, CsvValidationException {
        // initialize schema
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

        // Set the fields supplied by the command line to extractable fields
        if (cmd.hasOption(HEADERS_CONFIG)) {
            String[] headers = cmd.getOptionValues(HEADERS_CONFIG);
            for (String h : headers) {
                this.schema.addExtractableField(h, this.schema.getPathByLabel(h).getJsonPath());
            }
        }

        // initialize config
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

        // initialize calculator
        this.calculator = new CalculatorFacade(measurementConfig);
        // set the schema which describes the source
        calculator.setSchema(schema);

        // initialize input
        String inputFile = cmd.getOptionValue(INPUT_FILE);
        this.inputReader = RecordFactory.getRecordReader(inputFile, calculator);

        // initialize output
        String outFormat = cmd.getOptionValue(OUTPUT_FORMAT);
        // write to std out if no file was given
        this.outputWriter = cmd.hasOption(OUTPUT_FILE) ? RecordFactory.getResultWriter(outFormat, cmd.getOptionValue(OUTPUT_FILE)) : RecordFactory.getResultWriter(outFormat);
    }

    public static void main(String[] args) {

        // Take input file

        final Options options = new Options();
        options.addOption("i", INPUT_FILE, true, "Input file.");
        options.addOption("s", SCHEMA_CONFIG, true, "Schema file to run assessment against.");
        options.addOption("v", SCHEMA_FORMAT, true, "Format of schema file.");
        options.addOption("m", MEASUREMENTS_CONFIG, true, "Config file for measurements.");
        options.addOption("w", MEASUREMENTS_FORMAT, true, "Format of measurements file.");
        options.addOption("o", OUTPUT_FILE, true, "Output file.");
        options.addOption("f", OUTPUT_FORMAT, true, "Output format.");
        options.addOption("h", HEADERS_CONFIG, true, "Headers to copy from source");

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

    private void run() {
        long counter = 0;
        try {
            // print header
            List<String> header = new ArrayList<>();
            header.addAll(calculator.getHeader());
            outputWriter.writeHeader(header);

            while (inputReader.hasNext()) {


                Map<String, List<MetricResult>> measurement = inputReader.next();
                outputWriter.writeResult(measurement);

                // update process
                counter++;
                if (counter % 50 == 0) {
                    logger.info(String.format("Processed %s records. ", counter));
                }
            }
            logger.info(String.format("Assessment completed successfully with %s records. ", counter));
            outputWriter.close();

        } catch (IOException e) {
            logger.severe(String.format("Assessment failed with %s records. ", counter));
            logger.severe(e.getMessage());
        }
    }
}

