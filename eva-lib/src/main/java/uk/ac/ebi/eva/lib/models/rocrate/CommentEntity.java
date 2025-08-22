package uk.ac.ebi.eva.lib.models.rocrate;

public class CommentEntity extends RoCrateEntity {

    private String name;

    private String text;

    public CommentEntity() {
    }

    public CommentEntity(String name, String text) {
        super("#" + name, "Comment");
        this.name = name;
        this.text = text;
    }

    public CommentEntity(String prefix, String name, String text) {
        // Allows us to specify a prefix to disambiguate between multiple comments with the same name,
        // e.g. md5sums for different files
        super("#" + prefix + "-" + name, "Comment");
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
