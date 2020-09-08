package be.meemoo;

import de.gwdg.metadataqa.api.json.JsonBranch;
import de.gwdg.metadataqa.api.model.Category;
import de.gwdg.metadataqa.api.schema.BaseSchema;
import de.gwdg.metadataqa.api.schema.CsvAwareSchema;
import de.gwdg.metadataqa.api.schema.Format;
import de.gwdg.metadataqa.api.schema.Schema;

public class MeemooCSVSchema extends BaseSchema {

    public MeemooCSVSchema() {
        super();

        setFormat(Format.CSV);

        addField(
                new JsonBranch("fragment_id_mam", Category.MANDATORY)
                        .setExtractable()
        );
        addField(
                new JsonBranch("cp", Category.MANDATORY)
                        .setExtractable()
        );
        addField(
                new JsonBranch("cp_id", Category.MANDATORY)
                        .setExtractable()
        );
        addField(
                new JsonBranch("dc_description", Category.MANDATORY)
                        .setExtractable()
        );
        addField(
                new JsonBranch("pid", Category.MANDATORY)
                        .setExtractable()
        );
        addField(
                new JsonBranch("dc_title", Category.MANDATORY)
                        .setExtractable()
        );
        addFields(
                "mediaobject_id_mam", "sp_id", "sp_name", "dc_format",
                "dc_publisher", "dc_source", "dc_terms", "dcterms_abstract", "dcterms_created",
                "dcterms_issued"
        );
    }

}
