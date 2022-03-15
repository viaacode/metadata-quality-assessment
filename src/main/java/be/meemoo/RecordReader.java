package be.meemoo;

import de.gwdg.metadataqa.api.calculator.CalculatorFacade;
import de.gwdg.metadataqa.api.configuration.MeasurementConfiguration;
import de.gwdg.metadataqa.api.interfaces.MetricResult;
import de.gwdg.metadataqa.api.schema.Schema;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

abstract class RecordReader implements Iterator<Map<String, List<MetricResult>>> {

    protected final BufferedReader inputReader;
    protected final CalculatorFacade calculator;

    public RecordReader(BufferedReader inputReader, CalculatorFacade calculator) {
        this.inputReader = inputReader;
        this.calculator = calculator;
    }

}
