package be.meemoo;

import de.gwdg.metadataqa.api.interfaces.MetricResult;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

abstract class ResultWriter implements Closeable {

    protected final BufferedWriter outputWriter;

    public ResultWriter(String outputFile) throws IOException {
        Path outputPath = Paths.get(outputFile);
        this.outputWriter = Files.newBufferedWriter(outputPath);
    }

    public ResultWriter() {
        this.outputWriter = new BufferedWriter(new OutputStreamWriter(System.out));
    }

    @Override
    public void close() throws IOException {
        this.outputWriter.close();
    }

    abstract void writeResult(Map<String, List<MetricResult>> result) throws IOException;
    abstract void writeHeader(List<String> header) throws IOException;
}
