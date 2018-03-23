package dk.kb.cumulus.model;

import dk.kb.cumulus.WordStatus;

import java.util.List;

/**
 * Created by dgj on 22-02-2018.
 */
public class Word {
    private int id;
    private String text_en;
    private String text_da;
    private String category;
    private WordStatus status;
    private List<ImageWord> imageWords;

    public Word(int id, String text_en, String text_da, String category, WordStatus status) {
        this.id = id;
        this.text_en = text_en;
        this.text_da = text_da;
        this.category = category;
        this.status = status;
    }

    public Word(String text_en, String text_da, String category, WordStatus status) {
        this.id = -1;
        this.text_en = text_en;
        this.text_da = text_da;
        this.category = category;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getText_en() {
        return text_en;
    }

    public void setText_en(String text_en) {
        this.text_en = text_en;
    }

    public String getText_da() {
        return text_da;
    }

    public void setText_da(String text_da) {
        this.text_da = text_da;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public WordStatus getStatus() {
        return status;
    }

    public void setStatus(WordStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Word{" +
                "id=" + id +
                ", text_en='" + text_en + '\'' +
                ", text_da='" + text_da + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
