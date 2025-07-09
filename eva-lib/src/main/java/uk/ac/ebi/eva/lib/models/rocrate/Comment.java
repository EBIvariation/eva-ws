package uk.ac.ebi.eva.lib.models.rocrate;

public class Comment extends RoCrateEntity {

    private String name;

    private String text;

    public Comment(String name, String text) {
        super(name, "schema.org/Comment");
        this.name = name;
        this.text = text;
    }

}
