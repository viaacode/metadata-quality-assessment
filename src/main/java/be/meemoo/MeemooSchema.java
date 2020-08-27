package be.meemoo;

import de.gwdg.metadataqa.api.json.FieldGroup;
import de.gwdg.metadataqa.api.json.JsonBranch;
import de.gwdg.metadataqa.api.schema.Format;
import de.gwdg.metadataqa.api.schema.Schema;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MeemooSchema implements Schema {

    private static final List<FieldGroup> FIELD_GROUPS = new ArrayList<FieldGroup>();
    private static final List<String> NO_LANGUAGE_FIELDS = new ArrayList<String>();
    private static final Map<String, String> SOLR_FIELDS = new LinkedHashMap<String, String>();
    private Map<String, String> extractableFields = new LinkedHashMap<String, String>();
    private static final List<String> EMPTY_STRINGS = new ArrayList<String>();
    private static final Map<String, JsonBranch> PATHS = new LinkedHashMap<String, JsonBranch>();
    private static final Map<String, JsonBranch> COLLECTION_PATHS = new LinkedHashMap<String, JsonBranch>();

    static {


    }


    public Format getFormat() {
        return Format.JSON;
    }

    public List<FieldGroup> getFieldGroups() {
        return FIELD_GROUPS;
    }

    public List<String> getNoLanguageFields() {
        return NO_LANGUAGE_FIELDS;
    }

    public Map<String, String> getSolrFields() {
        return SOLR_FIELDS;
    }

    public Map<String, String> getExtractableFields() {
        return extractableFields;
    }

    public void setExtractableFields(Map<String, String> extractableFields) {
        this.extractableFields = extractableFields;
    }

    public List<String> getEmptyStringPaths() {
        return EMPTY_STRINGS;
    }

    public void addExtractableField(String label, String jsonPath) {
        extractableFields.put(label, jsonPath);
    }

    public List<JsonBranch> getCollectionPaths() {
        return new ArrayList(COLLECTION_PATHS.values());
    }

    private static void addPath(JsonBranch branch) {
        PATHS.put(branch.getLabel(), branch);
        if (branch.isCollection()) {
            COLLECTION_PATHS.put(branch.getLabel(), branch);
        }
    }

    public List<JsonBranch> getPaths() {
        return new ArrayList(PATHS.values());
    }

    public List<JsonBranch> getRootChildrenPaths() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public JsonBranch getPathByLabel(String label) {
        return PATHS.get(label);
    }
}
