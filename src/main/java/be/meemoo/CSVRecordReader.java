package be.meemoo;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import de.gwdg.metadataqa.api.calculator.CalculatorFacade;
import de.gwdg.metadataqa.api.configuration.MeasurementConfiguration;
import de.gwdg.metadataqa.api.interfaces.MetricResult;
import de.gwdg.metadataqa.api.schema.Schema;
import de.gwdg.metadataqa.api.util.CsvReader;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class CSVRecordReader extends RecordReader {

    private final CSVReader csvReader;
    private final Iterator<String[]> csvIterator;

    public CSVRecordReader(String inputFile, CalculatorFacade calculator) throws IOException, CsvValidationException {
        super(inputFile, calculator);

        this.csvReader = new CSVReader(inputReader);
        this.csvIterator = csvReader.iterator();

        // read header
        final List<String> header = Arrays.asList(csvReader.readNext());

        // right now it is a CSV source, so we set how to parse it
        this.calculator.setCsvReader(
                new CsvReader().setHeader(header));
    }

    @Override
    public boolean hasNext() {
        return this.csvIterator.hasNext();
    }

    @Override
    public Map<String, List<MetricResult>> next() {
        String[] record = csvIterator.next();
        final List<String> strings = Arrays.asList(record);
        return calculator.measureAsMetricResult(strings);
    }

}
