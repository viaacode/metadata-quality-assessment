package be.meemoo;

import de.gwdg.metadataqa.api.json.FieldGroup;
import de.gwdg.metadataqa.api.json.JsonBranch;
import de.gwdg.metadataqa.api.model.Category;
import de.gwdg.metadataqa.api.rule.PatternChecker;
import de.gwdg.metadataqa.api.rule.RuleChecker;
import de.gwdg.metadataqa.api.schema.Format;
import de.gwdg.metadataqa.api.schema.Schema;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MeemooJsonSchema implements Schema {

    private static final List<FieldGroup> FIELD_GROUPS = new ArrayList<FieldGroup>();
    private static final List<String> NO_LANGUAGE_FIELDS = new ArrayList<String>();
    private static final Map<String, String> SOLR_FIELDS = new LinkedHashMap<String, String>();
    private Map<String, String> extractableFields = new LinkedHashMap<String, String>();
    private static final List<String> EMPTY_STRINGS = new ArrayList<String>();
    private static final Map<String, JsonBranch> PATHS = new LinkedHashMap<String, JsonBranch>();
    private static final Map<String, JsonBranch> COLLECTION_PATHS = new LinkedHashMap<String, JsonBranch>();
    private static List<Category> categories = new ArrayList<>();
    private List<RuleChecker> ruleCheckers;

    static {
        addPath(new JsonBranch("fragment_id_mam",
                "$.['fragment_id_mam']",
                Category.MANDATORY));

        addPath(new JsonBranch("mediaobject_id_mam",
                "$.['mediaobject_id_mam']",
                Category.MANDATORY));

        addPath(new JsonBranch("cp",
                "$.['cp']",
                Category.MANDATORY));

        addPath(new JsonBranch("cp_id",
                "$.['cp_id']",
                Category.MANDATORY));

        addPath(new JsonBranch("sp_id",
                "$.['sp_id']",
                Category.MANDATORY));

        addPath(new JsonBranch("pid",
                "$.['pid']",
                Category.MANDATORY));

        addPath(new JsonBranch("dc_description",
                "$.['dc_description']",
                Category.MANDATORY));

        addPath(new JsonBranch("dc_format",
                "$.['dc_format']",
                Category.MANDATORY));

        addPath(new JsonBranch("dc_publisher",
                "$.['dc_publisher']",
                Category.MANDATORY));

        addPath(new JsonBranch("dc_source",
                "$.['dc_source']",
                Category.MANDATORY));

        addPath(new JsonBranch("dc_terms",
                "$.['dc_terms']",
                Category.MANDATORY));

        addPath(new JsonBranch("dc_title",
                "$.['dc_title']",
                Category.MANDATORY));

        addPath(new JsonBranch("dcterms_abstract",
                "$.['dcterms_abstract']",
                Category.MANDATORY));

        addPath(new JsonBranch("dcterms_created",
                "$.['dcterms_created']",
                Category.MANDATORY));

        addPath(new JsonBranch("dcterms_issued",
                "$.['dcterms_issued']",
                Category.MANDATORY));
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
        if (branch.isCollection())
            COLLECTION_PATHS.put(branch.getLabel(), branch);

        if (!branch.getCategories().isEmpty())
            for (Category category : branch.getCategories())
                if (!categories.contains(category))
                    categories.add(category);
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

    @Override
    public List<Category> getCategories() {
        return categories;
    }

    @Override
    public List<RuleChecker> getRuleCheckers() {
        if (ruleCheckers == null) {
            ruleCheckers = new ArrayList<>();
            for (JsonBranch branch : PATHS.values())
                if (StringUtils.isNotBlank(branch.getPattern()))
                    ruleCheckers.add(new PatternChecker(branch, branch.getPattern(), branch.getLabel()));
            categories = Category.extractCategories(PATHS.values());
        }
        return ruleCheckers;
    }
}
