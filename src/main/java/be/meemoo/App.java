package be.meemoo;

import com.jayway.jsonpath.InvalidJsonException;
import de.gwdg.metadataqa.api.calculator.CalculatorFacade;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Hello world!
 *
 */
public class App {
    private static final Logger logger = Logger.getLogger(App.class.getCanonicalName());

    public static void main(String[] args ) {

        CalculatorFacade calculator = new CalculatorFacade();
        MeemooSchema schema = new MeemooSchema();
        calculator.setSchema(schema);

        // do some configuration with the accessor of calculator Facade
        calculator.enableCompletenessMeasurement(true);
        calculator.configure();

        try {
            // initialize lines stream
            final Path path = Paths.get("./head-json.txt");

            Stream<String> stream = Files.lines(path);

            // read lines
            stream.forEach((jsonRecord) -> {
                try {
                    String csv = calculator.measure(jsonRecord);

                    System.out.println(csv);
                    // save csv
                } catch (InvalidJsonException e) {
                    // handle exception
                    logger.severe(String.format("Invalid JSON in %s: %s. Error message: %s.",
                            path.toString(), jsonRecord, e.getLocalizedMessage()));
                }
            });

            // close the stream
            stream.close();

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

}


