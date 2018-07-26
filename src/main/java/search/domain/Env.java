package search.domain;

/**
 * on 2018/7/26.
 */
public class Env {
    /**
     * sqlite 文件位置
     */
    private String storeLocation;

    /**
     * N-gram（q-gram）分割法
     */
    private int nGram = 2;

    public String getStoreLocation() {
        return storeLocation;
    }

    public void setStoreLocation(String storeLocation) {
        this.storeLocation = storeLocation;
    }

    public int getnGram() {
        return nGram;
    }

    public void setnGram(int nGram) {
        this.nGram = nGram;
    }
}
