# Metadata Quality Assessment CLI

This is a wrapper application for the [Metadata Quality API](https://github.com/pkiraly/metadata-qa-api). Read line-based metadata
records and output quality assessment results using various metrics.

## Install

Run `mvn package`.

## Run

```bash
usage: mqa [-f <arg>] [-h <arg>] -i <arg> -m <arg> [-o <arg>] -s <arg> [-v
       <arg>] [-w <arg>]
Command-line application for the Metadata Quality API
(https://github.com/pkiraly/metadata-qa-api). Read line-based metadata
records and output quality assessment results using various metrics.
 -f,--outputFormat <arg>         Format of the output: json, ndjson (new line delimited JSON),
                                 csv, csvjson (json encoded in csv; useful for RDB bulk loading).
                                 Default: ndjson.
 -h,--headers <arg>              Headers to copy from source
 -i,--input <arg>                Input file.
 -m,--measurements <arg>         Config file for measurements.
 -o,--output <arg>               Output file.
 -r,--recordAddress <arg>        An XPath or JSONPath expression to separate individual records
                                 in an XML or JSON files.
 -s,--schema <arg>               Schema file to run assessment against.
 -v,--schemaFormat <arg>         Format of schema file: json, yaml. Default: based on file
                                 extension, else json.
 -w,--measurementsFormat <arg>   Format of measurements config file: json, yaml. Default: based 
                                 on file extension, else json.
 -z,--gzip                       Flag to indicate that input is gzipped.
```

For more information on measurement and schema configuration, visit the [Metadata Quality API README](https://github.com/pkiraly/metadata-qa-api).
