package search.domain;

/**
 * on 2018/7/26.
 */
public class Env {
    /**
     * sqlite 文件位置
     */
    private String storeLocation;
    private String wikiLocation;

    /**
     * N-gram（q-gram）分割法
     */
    private int nGram = 2;

    private int memoryTokenCount = 5;

    private int maxProcessDocument = 50;
    private Integer totalDocument;

    public String getStoreLocation() {
        return storeLocation;
    }

    public void setStoreLocation(String storeLocation) {
        this.storeLocation = storeLocation;
    }

    public String getWikiLocation() {
        return wikiLocation;
    }

    public void setWikiLocation(String wikiLocation) {
        this.wikiLocation = wikiLocation;
    }

    public int getnGram() {
        return nGram;
    }

    public void setnGram(int nGram) {
        this.nGram = nGram;
    }

    public int getMemoryTokenCount() {
        return memoryTokenCount;
    }

    public void setMemoryTokenCount(int memoryTokenCount) {
        this.memoryTokenCount = memoryTokenCount;
    }

    public int getMaxProcessDocument() {
        return maxProcessDocument;
    }

    public void setMaxProcessDocument(int maxProcessDocument) {
        this.maxProcessDocument = maxProcessDocument;
    }

    public Integer getTotalDocument() {
        return totalDocument;
    }

    public void setTotalDocument(Integer totalDocument) {
        this.totalDocument = totalDocument;
    }
}
