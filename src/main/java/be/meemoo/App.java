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
import java.util.*;
import java.util.logging.Logger;


/**
 * Hello world!
 */
public class App {

    private static final Logger logger = Logger.getLogger(App.class.getCanonicalName());

    private static final String appName = "mqa";
    private static final String appHeader = "Command-line application for the Metadata Quality API (https://github.com/pkiraly/metadata-qa-api). Read line-based metadata records and output quality assessment results using various metrics.";

    // constants
    public static final String NDJSON = "ndjson";
    public static final String CSVJSON = "csvjson";
    public static final String CSV = "csv";
    public static final String JSON = "json";
    public static final String YAML = "yaml";

    // Arguments
    private static final String INPUT_FILE = "input";
    private static final String OUTPUT_FILE = "output";
    private static final String OUTPUT_FORMAT = "outputFormat";
    private static final String SCHEMA_CONFIG = "schema";
    private static final String SCHEMA_FORMAT = "schemaFormat";
    private static final String MEASUREMENTS_CONFIG = "measurements";
    private static final String HEADERS_CONFIG = "headers";
    private static final String MEASUREMENTS_FORMAT = "measurementsFormat";
    private static final String GZIP_FLAG = "gzip";

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
        this.inputReader = RecordFactory.getRecordReader(inputFile, calculator, cmd.hasOption(GZIP_FLAG));

        // initialize output
        String outFormat = cmd.getOptionValue(OUTPUT_FORMAT, NDJSON);
        // write to std out if no file was given
        this.outputWriter = cmd.hasOption(OUTPUT_FILE) ? RecordFactory.getResultWriter(outFormat, cmd.getOptionValue(OUTPUT_FILE)) : RecordFactory.getResultWriter(outFormat);
    }

    public static void main(String[] args) {

        // Take input file
        final Options options = new Options();

        Option inputOption = Option.builder("i")
                .numberOfArgs(1)
                .required(true)
                .longOpt(INPUT_FILE)
                .desc("Input file.")
                .build();

        Option outputOption = Option.builder("o")
                .numberOfArgs(1)
                .required(false)
                .longOpt(OUTPUT_FILE)
                .desc("Output file.")
                .build();

        Option outputFormatOption = Option.builder("f")
                .numberOfArgs(1)
                .required(false)
                .longOpt(OUTPUT_FORMAT)
                .desc("Format of the output: ndjson, csv, csvjson (json encoded in csv; useful for RDB bulk loading). Default: ndjson.")
                .build();

        Option schemaConfigOption = Option.builder("s")
                .numberOfArgs(1)
                .required(true)
                .longOpt(SCHEMA_CONFIG)
                .desc("Schema file to run assessment against.")
                .build();

        Option schemaFormatOption = Option.builder("v")
                .numberOfArgs(1)
                .required(false)
                .longOpt(SCHEMA_FORMAT)
                .desc("Format of schema file: json, yaml. Default: based on file extension, else json.")
                .build();

        Option measurementsConfigOption = Option.builder("m")
                .numberOfArgs(1)
                .required(true)
                .longOpt(MEASUREMENTS_CONFIG)
                .desc("Config file for measurements.")
                .build();

        Option measurementsFormatOption = Option.builder("w")
                .numberOfArgs(1)
                .required(false)
                .longOpt(MEASUREMENTS_FORMAT)
                .desc("Format of measurements config file: json, yaml. Default: based on file extension, else json.")
                .build();

        Option headersOption = Option.builder("h")
                .hasArgs()
                .required(false)
                .longOpt(HEADERS_CONFIG)
                .desc("Headers to copy from source")
                .build();

        Option gzipOption = Option.builder("z")
                .numberOfArgs(0)
                .required(false)
                .longOpt(GZIP_FLAG)
                .desc("Flag to indicate that input is gzipped.")
                .build();

        options.addOption(inputOption);
        options.addOption(outputOption);
        options.addOption(outputFormatOption);
        options.addOption(schemaConfigOption);
        options.addOption(schemaFormatOption);
        options.addOption(measurementsConfigOption);
        options.addOption(measurementsFormatOption);
        options.addOption(headersOption);
        options.addOption(gzipOption);

        // create the parser
        CommandLineParser parser = new DefaultParser();

        // create the formatter
        HelpFormatter formatter = new HelpFormatter();

        try {
            // parse the command line arguments
            CommandLine cmd = parser.parse(options, args);
            new App(cmd).run();
        } catch (MissingOptionException ex) {
            formatter.printHelp(appName, appHeader, options, "Options missing: " + ex.getMissingOptions().toString(), true);
            System.exit(1);
        } catch (MissingArgumentException ex) {
            formatter.printHelp(appName, appHeader, options, "Arguments missing: " + ex.getOption().getArgName(), true);
            System.exit(1);
        } catch (Exception ex) {
            formatter.printHelp(appName, appHeader, options, "Error: " + ex.getMessage(), true);
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private void run() {
        long counter = 0;
        try {
            // print header
            List<String> header = calculator.getHeader();
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

