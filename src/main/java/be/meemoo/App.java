package be.meemoo;

import com.jayway.jsonpath.InvalidJsonException;
import de.gwdg.metadataqa.api.calculator.CalculatorFacade;
import de.gwdg.metadataqa.api.json.JsonBranch;
import de.gwdg.metadataqa.api.model.Category;
import de.gwdg.metadataqa.api.schema.BaseSchema;
import de.gwdg.metadataqa.api.schema.CsvAwareSchema;
import de.gwdg.metadataqa.api.schema.Format;
import de.gwdg.metadataqa.api.schema.Schema;
import de.gwdg.metadataqa.api.util.CsvReader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Hello world!
 */
public class App {
    private static final Logger logger = Logger.getLogger(App.class.getCanonicalName());

    public static void main(String[] args) {

        // Take input file
        // Check how many arguments were passed in
        if (args.length == 0) {
            System.out.println("[csv file]");
            System.exit(0);
        }
        String inputFile = args[0];

        // Instantiate schema
        Schema schema = new MeemooCSVSchema();

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

                //.enableFieldExistenceMeasurement();


        try {


            // initialize lines stream
            final Path path = Paths.get(inputFile);

            Stream<String> stream = Files.lines(path);

            System.out.println(calculator.getHeader());

            // read lines
            stream.forEach((record) -> {
                try {
                    String csv = calculator.measure(record);

                    System.out.println(csv);
                    // save csv
                } catch (InvalidJsonException e) {
                    // handle exception
                    logger.severe(String.format("Invalid JSON in %s: %s. Error message: %s.",
                            path.toString(), record, e.getLocalizedMessage()));
                }
            });

            // close the stream
            stream.close();

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

}


