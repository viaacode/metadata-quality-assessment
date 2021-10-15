package be.meemoo;

import de.gwdg.metadataqa.api.calculator.CalculatorFacade;
import de.gwdg.metadataqa.api.configuration.MeasurementConfiguration;
import de.gwdg.metadataqa.api.interfaces.MetricResult;
import de.gwdg.metadataqa.api.schema.Schema;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

abstract class RecordReader implements Iterator<Map<String, List<MetricResult>>> {

    protected final BufferedReader inputReader;
    protected final CalculatorFacade calculator;

    public RecordReader(String inputFile, CalculatorFacade calculator) throws IOException {
        Path inputPath = Paths.get(inputFile);
        this.inputReader = Files.newBufferedReader(inputPath);

        this.calculator = calculator;
    }

}
