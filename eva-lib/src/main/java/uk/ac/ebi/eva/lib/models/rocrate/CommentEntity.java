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

    public String getName() {
        return name;
    }

    public String getText() {
        return text;
    }

}
