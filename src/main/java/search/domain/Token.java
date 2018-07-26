package search.domain;

import java.util.List;

/**
 * 词元信息
 * on 2018/7/26.
 */
public class Token {
    /**
     * 词元ID
     */
    private int id;
    /**
     * 词元内容
     */
    private String text;
    /**
     * 存在的文档数量
     */
    private int docCount;
    /**
     * 存在的文档位置数量
     */
    private int positionsCount;
    /**
     * 倒排索引列表
     */
    private List<Posting> posting;
    /**
     * 倒排索引DB存储形式
     */
    private String post;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getDocCount() {
        return docCount;
    }

    public void setDocCount(int docCount) {
        this.docCount = docCount;
    }

    public int getPositionsCount() {
        return positionsCount;
    }

    public void setPositionsCount(int positionsCount) {
        this.positionsCount = positionsCount;
    }

    public List<Posting> getPosting() {
        return posting;
    }

    public void setPosting(List<Posting> posting) {
        this.posting = posting;
    }

    public String getPost() {
        return post;
    }

    public void setPost(String post) {
        this.post = post;
    }
}
