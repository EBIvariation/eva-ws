package uk.ac.ebi.eva.lib.models.rocrate;

public class Comment extends RoCrateEntity {

    private String name;

    private String text;

    public Comment() {}

    public Comment(String name, String text) {
        // TODO what should the ID be?
        super(name, "schema.org/Comment");
        this.name = name;
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public String getText() {
        return text;
    }

}
