package search;

import search.domain.Document;
import search.domain.Env;
import search.domain.Posting;
import search.domain.Token;
import search.util.StringUtils;

import java.util.*;

/**
 * on 2018/7/26.
 */
public class Indexer {
    private DbUtil dbUtil;
    private Map<String, Token> localToken = new HashMap<>();
    private Env env;

    public Indexer(Env env) throws Exception {
        this.dbUtil = new DbUtil(env.getStoreLocation());
        this.env = env;
    }

    public void addDocumentToStore(String title, String text) throws Exception {
        Integer docId = dbUtil.getIDFromDocumentTitle(title);
        if (docId == null) {
            Document document = new Document();
            document.setTitle(title);
            document.setText(text);
            docId = dbUtil.insertNewDocument(document);
        }

        char[] textChar = text.toCharArray();
        Map<String, Posting> postingMap = tokenToPosting(textChar, docId);
        //merge to local
        for (Map.Entry<String, Posting> entry : postingMap.entrySet()) {
            Token token;
            String tokenText = entry.getKey();
            Posting posting = entry.getValue();
            if ((token = localToken.get(tokenText)) == null) {
                token = new Token();
                token.setText(tokenText);
                localToken.put(tokenText, token);
            }
            token.setDocCount(token.getDocCount() + 1);
            token.setPositionsCount(token.getPositionsCount() + posting.getPosCount());
            token.getPosting().add(posting);
        }

        if (localToken.size() >= env.getMemoryTokenCount()) {
            mergeToStore();
        }

    }

    public void flush() throws Exception {
        if (!localToken.isEmpty()) mergeToStore();
    }

    public Map<String, Posting> tokenToPosting(char[] parseChars, Integer docId) {
        Map<String, Posting> tokenMap = new HashMap<>();

        for (int from = 0; ; ++from) {
            int[] tokenPos = nextTokenIndex(parseChars, from, env.getnGram());
            if ((from = tokenPos[0]) < 0) {
                break;
            }
            if (tokenPos[1] - from >= env.getnGram()) {
                String token = new String(parseChars, from, tokenPos[1] - from).toLowerCase();
                Posting posting;
                if ((posting = tokenMap.get(token)) == null) {
                    posting = new Posting();
                    posting.setDocumentId(docId);
                    tokenMap.put(token, posting);
                }
                posting.setPosCount(posting.getPosCount() + 1);
                posting.getPositions().add(tokenPos[0]);
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
            if (isEngChar(parseChars[from])) {
                //英文字符取一个单词
                for (; from < parseChars.length && isEngChar(parseChars[from]); ++from) ;
            } else {
                //取nGram个字符或达到可忽略字符
                for (int count = 0; from < parseChars.length && !isIgnoredChar(parseChars[from]) && count < nGram; ++from, ++count) {
                }
            }

            result[1] = from;
        } else {
            result[0] = -1;
        }
        return result;
    }


    /**
     * 合并内存倒排列表到存储系统
     */
    private void mergeToStore() throws Exception {
        for (Map.Entry<String, Token> entry : localToken.entrySet()) {
            String tokenText = entry.getKey();
            Token token = entry.getValue();
            serializationPost(token);

            Token tokenStore = dbUtil.getTokenByText(tokenText);
            if (tokenStore == null) {
                tokenStore = token;
                dbUtil.insertToken(tokenStore);
            } else {
                tokenStore.setPositionsCount(token.getPositionsCount() + tokenStore.getPositionsCount());
                tokenStore.setDocCount(tokenStore.getDocCount() + token.getDocCount());
                tokenStore.setPost(tokenStore.getPost() + ";" + token.getPost());
                dbUtil.updateTokenById(tokenStore);
            }
        }
        localToken = new HashMap<>();
    }


    /**
     * 序列化
     *
     * @param token
     */
    private void serializationPost(Token token) {
        List<Posting> posting = token.getPosting();
        int size = posting.size();
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < size; ++i) {
            Posting post = posting.get(i);
            result.append(post.getDocumentId());
            result.append(',');
            result.append(post.getPosCount());
            result.append(',');
            result.append(StringUtils.join(post.getPositions(), ","));
            if (i != size - 1) {
                result.append(';');
            }
        }
        token.setPost(result.toString());
    }


    private Set<Character> ignoreChars = new HashSet<>(Arrays.asList(
            ' ', '\'', '"', '\n', '[', ']', '.',
            ';', '<', '<', '>', ':', '/', '\\',
            '&', '；', ')', '(', '（', '）'
    ));


    /**
     * 是否是可忽略字符（终止符 ）
     *
     * @param c
     * @return
     */
    private boolean isIgnoredChar(char c) {
        return ignoreChars.contains(c);
    }

    /**
     * 是否是英文字符
     *
     * @param c
     * @return
     */
    private boolean isEngChar(char c) {
        return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
    }
}
