package search;

import search.domain.Document;
import search.domain.Env;
import search.domain.Posting;
import search.domain.Token;

import java.util.HashMap;
import java.util.Map;

/**
 * on 2018/7/26.
 */
public class Store {
    private DbUtil dbUtil;
    private Map<String, Token> local = new HashMap<>();
    private Env env;

    public Store(Env env) throws Exception {
        this.dbUtil = new DbUtil(env.getStoreLocation());
        this.env = env;
    }

    public void store(String title, String text) throws Exception {
        Integer docId = dbUtil.getIDFromDocumentTitle(title);
        if (docId == null) {
            Document document = new Document();
            document.setTitle(title);
            document.setText(text);
            docId = dbUtil.insertNewDocument(document);
        }

        char[] textChar = text.toCharArray();

    }

    public Map<String, Posting> tokenToPosting(char[] parseChars, Integer docId) {
        Map<String, Posting> tokenMap = new HashMap<>();

        for (int from = 0; ; ) {
            int[] tokenPos = nextTokenIndex(parseChars, from, env.getnGram());
            if ((from = tokenPos[0]) < 0) {
                break;
            }
            if (tokenPos[0] - from == env.getnGram()) {

            }
        }
        return tokenMap;
    }

    public int[] nextTokenIndex(char[] parseChars, int from, int nGram) {
        int[] result = new int[2];
        //去掉湖绿字符
        while (from < parseChars.length && isIgnoredChar(parseChars[from])) ++from;
        if (from < parseChars.length) {
            result[0] = from;
            //取nGram个字符或达到可忽略字符
            for (int count = 0; from < parseChars.length && !isIgnoredChar(parseChars[from]) && count < nGram; ++from, ++count) {
            }
            result[1] = from;
        } else {
            result[0] = -1;
        }
        return result;
    }

    private boolean isIgnoredChar(char c) {
        return c == ' ';
    }
}
