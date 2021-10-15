package be.meemoo;

import de.gwdg.metadataqa.api.calculator.CalculatorFacade;
import de.gwdg.metadataqa.api.interfaces.MetricResult;
import org.apache.commons.io.LineIterator;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class JSONRecordReader extends RecordReader {

    private final LineIterator jsonIterator;

    public JSONRecordReader(String inputFile, CalculatorFacade calculator) throws IOException {
        super(inputFile, calculator);
        jsonIterator = new LineIterator(inputReader);
    }

    @Override
    public boolean hasNext() {
        return jsonIterator.hasNext();
    }

    @Override
    public Map<String, List<MetricResult>> next() {
        String record = jsonIterator.next();
        return this.calculator.measureAsMetricResult(record);
    }
}
