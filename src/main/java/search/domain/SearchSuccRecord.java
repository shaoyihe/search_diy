package search.domain;

public class SearchSuccRecord {
    private double score;
    private Document document;

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    @Override
    public String toString() {
        return "document_id :" + document.getId() +
                " title : " + document.getTitle() +
                " score : " + score;
    }
}
