package be.meemoo;

import de.gwdg.metadataqa.api.calculator.CalculatorFacade;
import de.gwdg.metadataqa.api.configuration.ConfigurationReader;
import de.gwdg.metadataqa.api.configuration.MeasurementConfiguration;
import de.gwdg.metadataqa.api.json.JsonBranch;
import de.gwdg.metadataqa.api.model.Category;
import de.gwdg.metadataqa.api.schema.BaseSchema;
import de.gwdg.metadataqa.api.schema.CsvAwareSchema;
import de.gwdg.metadataqa.api.schema.Format;
import de.gwdg.metadataqa.api.schema.Schema;
import de.gwdg.metadataqa.api.util.CsvReader;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

import static org.junit.Assert.assertEquals;

public class CalculatorTest {

  @Test
  public void fullMeasurementTest() throws Exception {
    String fileName = "src/test/resources/csv/head.csv";
    File inputFile = new File(fileName);

    if (!inputFile.exists())
      return;

    Schema schema = new BaseSchema()
      .setFormat(Format.CSV)
      .addField(
        new JsonBranch("fragment_id_mam")
                .setCategories(Category.MANDATORY)
          .setExtractable()
      )
      .addFields(
        "mediaobject_id_mam", "cp", "cp_id", "sp_id", "sp_name", "pid", "dc_description", "dc_format",
        "dc_publisher", "dc_source", "dc_terms", "dc_title", "dcterms_abstract", "dcterms_created",
        "dcterms_issued"
      )
      ;

    // we will measure completeness now
    MeasurementConfiguration config = new MeasurementConfiguration();
    config.enableCompletenessMeasurement().disableFieldCardinalityMeasurement();

    CalculatorFacade calculator = new CalculatorFacade(config)
      // set the schema which describes the source
      .setSchema(schema)
      // right now it is a CSV source, so we set how to parse it
      .setCsvReader(
        new CsvReader()
          .setHeader(((CsvAwareSchema) schema).getHeader()));

    assertEquals(
      Arrays.asList(
        "completeness:TOTAL", "completeness:MANDATORY",
        "existence:fragment_id_mam", "existence:mediaobject_id_mam",
        "existence:cp", "existence:cp_id", "existence:sp_id", "existence:sp_name", "existence:pid",
        "existence:dc_description", "existence:dc_format", "existence:dc_publisher", "existence:dc_source",
        "existence:dc_terms", "existence:dc_title", "existence:dcterms_abstract",
        "existence:dcterms_created", "existence:dcterms_issued"
      ),
      calculator.getHeader()
    );

    StringBuffer output = new StringBuffer();
    // output.append(StringUtils.join(calculator.getHeader(), ","));
    try {
      Scanner scanner = new Scanner(inputFile);
      while (scanner.hasNext()) {
        output.append(calculator.measure(scanner.nextLine()) + "\n");
      }
      scanner.close();

      // System.err.println(output.toString());
      String expected = "1.0,1.0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1\n" +
        "0.5,1.0,1,1,1,1,0,1,1,0,0,0,1,0,1,0,0,0\n" +
        "0.6875,1.0,1,1,1,1,0,1,1,1,0,0,1,0,1,0,1,1\n" +
        "0.5,1.0,1,1,1,1,0,1,1,0,0,0,1,0,1,0,0,0\n" +
        "0.5,1.0,1,1,1,1,0,1,1,0,0,0,1,0,1,0,0,0\n" +
        "0.5625,1.0,1,1,1,1,0,1,1,0,0,0,0,0,1,0,1,1\n" +
        "0.5625,1.0,1,1,1,1,0,1,1,1,0,0,1,0,0,0,1,0\n" +
        "0.3125,1.0,1,1,1,1,0,0,1,0,0,0,0,0,0,0,0,0\n" +
        "0.5,1.0,1,1,1,1,0,1,1,0,0,0,1,0,1,0,0,0\n" +
        "0.5625,1.0,1,1,1,1,0,1,1,0,0,0,0,0,1,0,1,1\n" +
        "0.5,1.0,1,1,1,1,0,1,1,0,0,0,1,0,0,0,0,1\n" +
        "0.6875,1.0,1,1,1,1,0,1,1,1,0,0,1,0,1,0,1,1\n" +
        "0.625,1.0,1,1,1,1,0,1,1,0,1,0,0,0,1,0,1,1\n" +
        "0.5,1.0,1,1,1,1,0,1,1,0,0,0,1,0,1,0,0,0\n" +
        "0.625,1.0,1,1,1,1,0,1,1,1,0,0,0,0,1,0,1,1\n" +
        "0.5,1.0,1,1,1,1,0,1,1,0,0,0,1,0,1,0,0,0\n" +
        "0.5,1.0,1,1,1,1,0,1,1,0,0,0,1,0,1,0,0,0\n" +
        "0.5,1.0,1,1,1,1,0,1,1,0,0,0,1,0,1,0,0,0\n" +
        "0.5625,1.0,1,1,1,1,0,1,1,0,0,0,0,0,1,0,1,1\n" +
        "0.5,1.0,1,1,1,1,0,1,1,0,0,0,1,0,1,0,0,0\n" +
        "0.5625,1.0,1,1,1,1,0,1,1,1,0,0,1,0,0,0,1,0\n" +
        "0.5,1.0,1,1,1,1,0,1,1,0,0,0,1,0,1,0,0,0\n" +
        "0.625,1.0,1,1,1,1,0,1,1,1,0,0,0,0,1,0,1,1\n" +
        "0.5625,1.0,1,1,1,1,0,1,1,0,0,0,0,0,1,0,1,1\n" +
        "0.5625,1.0,1,1,1,1,0,1,1,0,0,0,0,0,1,0,1,1\n" +
        "0.5625,1.0,1,1,1,1,0,1,1,0,0,0,0,0,1,0,1,1\n" +
        "0.5625,1.0,1,1,1,1,0,1,1,0,0,0,0,0,1,0,1,1\n" +
        "0.5625,1.0,1,1,1,1,0,1,1,0,0,0,0,0,1,0,1,1\n" +
        "0.5,1.0,1,1,1,1,0,1,1,0,0,0,1,0,1,0,0,0\n" +
        "0.5,1.0,1,1,1,1,0,1,1,0,0,0,1,0,1,0,0,0\n" +
        "0.5,1.0,1,1,1,1,0,1,1,0,0,0,1,0,1,0,0,0\n" +
        "0.5,1.0,1,1,1,1,0,1,1,0,0,0,1,0,1,0,0,0\n" +
        "0.5625,1.0,1,1,1,1,0,1,1,0,0,0,0,0,1,0,1,1\n" +
        "0.5,1.0,1,1,1,1,0,1,1,0,0,0,1,0,1,0,0,0\n" +
        "0.5,1.0,1,1,1,1,0,1,1,0,0,0,1,0,1,0,0,0\n" +
        "0.625,1.0,1,1,1,1,0,1,1,1,0,0,0,0,1,0,1,1\n" +
        "0.5625,1.0,1,1,1,1,0,1,1,0,0,0,0,0,1,0,1,1\n" +
        "0.5,1.0,1,1,1,1,0,1,1,0,0,0,1,0,1,0,0,0\n";

      assertEquals(expected, output.toString());

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

/*  @Test
  public void multilineTest() throws Exception {
    String fileName = "src/test/resources/csv/multiline.csv";
    File inputFile = new File(fileName);

    if (!inputFile.exists())
      return;

    Schema schema = new BaseSchema()
            .setFormat(Format.CSV)
            .addField(
                    new JsonBranch("fragment_id_mam")
                            .setCategories(Category.MANDATORY)
                            .setExtractable()
            )
            .addFields(
                    "mediaobject_id_mam", "cp", "cp_id", "sp_id", "sp_name", "pid", "dc_description", "dc_format",
                    "dc_publisher", "dc_source", "dc_terms", "dc_title", "dcterms_abstract", "dcterms_created",
                    "dcterms_issued"
            )
            ;

    CalculatorFacade calculator = new CalculatorFacade()
            // set the schema which describes the source
            .setSchema(schema)
            // right now it is a CSV source, so we set how to parse it
            .setCsvReader(
                    new CsvReader()
                            .setHeader(((CsvAwareSchema) schema).getHeader()))
            // we will measure completeness now
            .enableCompletenessMeasurement()
            .disableFieldCardinalityMeasurement();

    assertEquals(
            Arrays.asList(
                    "completeness:TOTAL", "completeness:MANDATORY",
                    "existence:fragment_id_mam", "existence:mediaobject_id_mam",
                    "existence:cp", "existence:cp_id", "existence:sp_id", "existence:sp_name", "existence:pid",
                    "existence:dc_description", "existence:dc_format", "existence:dc_publisher", "existence:dc_source",
                    "existence:dc_terms", "existence:dc_title", "existence:dcterms_abstract",
                    "existence:dcterms_created", "existence:dcterms_issued"
            ),
            calculator.getHeader()
    );

    StringBuffer output = new StringBuffer();
    // output.append(StringUtils.join(calculator.getHeader(), ","));
    try {
      Scanner scanner = new Scanner(inputFile);
      while (scanner.hasNext()) {
        output.append(calculator.measure(scanner.nextLine()) + "\n");
      }
      scanner.close();

      // System.err.println(output.toString());
      String expected = "1.0,1.0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1\n" +
              "0.5,1.0,1,1,1,1,0,1,1,1,0,0,1,0,1,0,0,0\n";

      assertEquals(expected, output.toString());

    } catch (IOException e) {
      e.printStackTrace();
    }
  }*/

  @Test
  public void testMeasurement_withYamlSchema() throws FileNotFoundException {
    Schema schema = ConfigurationReader
      .readSchemaYaml(
        "src/test/resources/schema/meemoo.csv.yaml"
      )
     .asSchema();

    MeasurementConfiguration config = new MeasurementConfiguration();
    config.enableCompletenessMeasurement()
            .disableFieldCardinalityMeasurement()
            .enableRuleCatalogMeasurement();

    CalculatorFacade calculator = new CalculatorFacade(config)
      // set the schema which describes the source
      .setSchema(schema)
      // right now it is a CSV source, so we set how to parse it
      .setCsvReader(
        new CsvReader()
          .setHeader(((CsvAwareSchema) schema).getHeader()));

    String fileName = "src/test/resources/csv/head.csv";
    File inputFile = new File(fileName);

    assertEquals(
      Arrays.asList(
        "completeness:TOTAL", "completeness:MANDATORY", "existence:fragment_id_mam",
        "existence:mediaobject_id_mam", "existence:cp", "existence:cp_id", "existence:sp_id",
        "existence:sp_name", "existence:pid", "existence:dc_description", "existence:dc_format",
        "existence:dc_publisher", "existence:dc_source", "existence:dc_terms",
        "existence:dc_title", "existence:dcterms_abstract", "existence:dcterms_created",
        "existence:dcterms_issued", "fragment_id_mam:pattern:1", "mediaobject_id_mam:pattern:2", "dcterms_created:pattern:3", "ruleCatalog:score"
      ),
      calculator.getHeader()
    );

    StringBuffer output = new StringBuffer();
    try {
      Scanner scanner = new Scanner(inputFile);
      int lineNr = 0;
      while (scanner.hasNext()) {
        String line = scanner.nextLine();
        if (++lineNr > 1 && lineNr < 11)
          output.append(calculator.measure(line) + "\n");
      }
      scanner.close();

      String expected = "0.5,0.833333,1,1,1,1,0,1,1,0,0,0,1,0,1,0,0,0,1,0,NA,0\n" +
              "0.6875,1.0,1,1,1,1,0,1,1,1,0,0,1,0,1,0,1,1,1,0,1,0\n" +
              "0.5,0.833333,1,1,1,1,0,1,1,0,0,0,1,0,1,0,0,0,1,0,NA,0\n" +
              "0.5,0.833333,1,1,1,1,0,1,1,0,0,0,1,0,1,0,0,0,1,0,NA\n,0" +
              "0.5625,0.833333,1,1,1,1,0,1,1,0,0,0,0,0,1,0,1,1,1,0,1,0\n" +
              "0.5625,0.833333,1,1,1,1,0,1,1,1,0,0,1,0,0,0,1,0,1,0,1,0\n" +
              "0.3125,0.666667,1,1,1,1,0,0,1,0,0,0,0,0,0,0,0,0,1,0,NA,0\n" +
              "0.5,0.833333,1,1,1,1,0,1,1,0,0,0,1,0,1,0,0,0,1,0,NA,0\n" +
              "0.5625,0.833333,1,1,1,1,0,1,1,0,0,0,0,0,1,0,1,1,1,0,1,0\n";

      assertEquals(expected, output.toString());
    } catch (IOException e) {
      e.printStackTrace();
    }

  }
}
