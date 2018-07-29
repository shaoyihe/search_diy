package search.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * 倒排索引
 * on 2018/7/26.
 */
public class Posting {
    /**
     * 文档ID
     */
    private Integer documentId;
    /**
     * 出现位置数量
     */
    private Integer posCount = 0;
    /**
     * 出现位置信息
     */
    private List<Integer> positions = new ArrayList<>();

    public Integer getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Integer documentId) {
        this.documentId = documentId;
    }

    public Integer getPosCount() {
        return posCount;
    }

    public void setPosCount(Integer posCount) {
        this.posCount = posCount;
    }

    public List<Integer> getPositions() {
        return positions;
    }

    public void setPositions(List<Integer> positions) {
        this.positions = positions;
    }
}
