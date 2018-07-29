package search;

import search.domain.*;

import java.util.*;
import java.util.stream.Collectors;

public class IndexSearcher {
    private DbUtil dbUtil;
    private Env env;
    private Indexer indexer;

    public IndexSearcher(Env env) throws Exception {
        this.dbUtil = new DbUtil(env.getStoreLocation());
        this.env = env;
        this.indexer = new Indexer(env);
        if (env.getTotalDocument() == null) {
            env.setTotalDocument(dbUtil.countDocument());
        }
    }

    public List<SearchSuccRecord> search(String text) throws Exception {
        Map<String, Token> tokenMap = new HashMap<>();
        //分词
        Map<String, Posting> postingMap = indexer.tokenToPosting(text.toCharArray(), null);
        if (postingMap.isEmpty()) {
            return null;
        }
        //找到存储token
        for (Map.Entry<String, Posting> entry : postingMap.entrySet()) {
            String tokenText = entry.getKey();
            if (!tokenMap.containsKey(tokenText)) {
                Token tokenStore = dbUtil.getTokenByText(tokenText);
                if (tokenStore == null) {
                    return null;
                }
                deserializationPost(tokenStore);
                tokenMap.put(tokenText, tokenStore);
            }
        }
        //开始划分排序
        List<SearchToken> searchTokens = new ArrayList<>();
        for (Map.Entry<String, Posting> entry : postingMap.entrySet()) {
            for (Integer pos : entry.getValue().getPositions()) {
                SearchToken searchToken = new SearchToken();
                searchToken.setToken(tokenMap.get(entry.getKey()));
                searchToken.setUserSearchPos(pos);
                searchTokens.add(searchToken);
            }
        }
        Collections.sort(searchTokens, Comparator.comparing(SearchToken::getUserSearchPos));

        int minDocCount = searchTokens.get(0).getToken().getDocCount();
        int minDocCountIndex = 0;
        for (int i = 1; i < searchTokens.size() - 1; ++i) {
            int docCount = searchTokens.get(i).getToken().getDocCount();
            if (docCount < minDocCount) {
                minDocCountIndex = i;
                minDocCount = docCount;
            }
        }
        List<SearchSuccRecord> searchSuccRecords = searchPhase(searchTokens, minDocCountIndex);
        searchSuccRecords.sort(Comparator.comparing(SearchSuccRecord::getScore).reversed());
        return searchSuccRecords;
    }

    /**
     * 开始搜索短语
     *
     * @param searchTokens
     * @param minDocCountIndex
     */
    private List<SearchSuccRecord> searchPhase(List<SearchToken> searchTokens, int minDocCountIndex) throws Exception {
        List<SearchSuccRecord> searchSuccRecords = new ArrayList<>();

        SearchToken minSearchToken = searchTokens.get(minDocCountIndex);
        for (; ; ) {
            Posting minPosting = minSearchToken.getToken().getPosting().get(minSearchToken.getSearchPostingIndex());
            int minDocument = minPosting.getDocumentId();
            Integer newMinDocumentId = null;

            outLoop:
            for (int i = 0; i < searchTokens.size(); ++i) {
                if (i != minDocCountIndex) {
                    //对于其他文档一直调整到不小于curDocument，如果没有找到就返回
                    SearchToken curSearchToken = searchTokens.get(i);
                    List<Posting> curPosting = curSearchToken.getToken().getPosting();
                    int t = curSearchToken.getSearchPostingIndex();
                    for (; t < curPosting.size(); ++t) {
                        Integer curDocumentId = curPosting.get(t).getDocumentId();
                        if (curDocumentId == minDocument) {
                            //find
                            curSearchToken.setSearchPostingIndex(t);
                            break;
                        } else if (curDocumentId > minDocument) {
                            //进行下一轮搜索
                            newMinDocumentId = curDocumentId;
                            break outLoop;
                        }
                    }
                    if (t == curPosting.size()) {
                        //说明已经没有记录了
                        return searchSuccRecords;
                    }
                }
            }

            if (newMinDocumentId == null) {
                //找到匹配的文档，开始匹配位置
                SearchSuccRecord searchSuccRecord = searchPos(searchTokens);
                if (searchSuccRecord != null) {
                    searchSuccRecords.add(searchSuccRecord);
                }
                //下一轮
                minSearchToken.setSearchPostingIndex(minSearchToken.getSearchPostingIndex() + 1);
                if (minSearchToken.getSearchPostingIndex() >= minSearchToken.getToken().getPosting().size()) {
                    return searchSuccRecords;
                }
            } else {
                //
                List<Posting> curPosting = minSearchToken.getToken().getPosting();
                int t = minSearchToken.getSearchPostingIndex();
                for (; t < curPosting.size(); ++t) {
                    Integer curDocumentId = curPosting.get(t).getDocumentId();
                    if (curDocumentId >= newMinDocumentId) {
                        minSearchToken.setSearchPostingIndex(t);
                        break;
                    }
                }

                if (t == curPosting.size()) {
                    //end
                    return searchSuccRecords;
                }
            }
        }

    }

    /**
     * 位置匹配
     *
     * @param searchTokens
     */
    private SearchSuccRecord searchPos(List<SearchToken> searchTokens) throws Exception {
        //reset
        for (SearchToken searchToken : searchTokens) {
            searchToken.setSearchPostingPosIndex(0);
        }

        SearchToken firstSearchToken = searchTokens.get(0);
        while (true) {
            SearchToken lastSearchToken = searchTokens.get(0);
            boolean nextRound = false;
            for (int i = 1; i < searchTokens.size(); lastSearchToken = searchTokens.get(i), ++i) {
                SearchToken searchToken = searchTokens.get(i);
                Posting posting = searchToken.searchPosting();
                int t = searchToken.getSearchPostingPosIndex();
                for (; t < posting.getPositions().size() && searchToken.searchPostingPos() <= lastSearchToken.searchPostingPos(); ++t) {
                    searchToken.setSearchPostingPosIndex(searchToken.getSearchPostingPosIndex() + 1);
                }
                if (t == posting.getPositions().size()) {
                    return null;
                }

                if (lastSearchToken.getUserSearchPos() + 1 == searchToken.getUserSearchPos()) {
                    if (lastSearchToken.searchPostingPos() + 1 != searchToken.searchPostingPos()) {
                        nextRound = true;
                        break;
                    }
                }
            }
            if (nextRound) {
                firstSearchToken.setSearchPostingPosIndex(firstSearchToken.getSearchPostingPosIndex() + 1);
                if (lastSearchToken.getSearchPostingPosIndex() >= firstSearchToken.searchPosting().getPositions().size()) {
                    return null;
                }
            } else {
                //found
                SearchSuccRecord searchSuccRecord = new SearchSuccRecord();
                searchSuccRecord.setScore(score(searchTokens));
                searchSuccRecord.setDocument(dbUtil.getDocumentById(firstSearchToken.searchPosting().getDocumentId()));
                return searchSuccRecord;
            }
        }
    }

    private double score(List<SearchToken> searchTokens) {
        double score = 0;
        for (SearchToken searchToken : searchTokens) {
            double idf = Math.log10(env.getTotalDocument() / searchToken.getToken().getDocCount());
            score += searchToken.searchPosting().getPosCount() * idf;
        }
        return score;
    }


    private void deserializationPost(Token tokenStore) {
        if (tokenStore.getPosting().isEmpty()) {
            String postings = tokenStore.getPost();
            for (String posting : postings.split(";")) {
                if (!posting.isEmpty()) {
                    String[] split = posting.split(",");
                    Posting memoryPosting = new Posting();
                    memoryPosting.setDocumentId(Integer.valueOf(split[0]));
                    memoryPosting.setPosCount(Integer.valueOf(split[1]));
                    memoryPosting.setPositions(Arrays.stream(split).skip(2).mapToInt(Integer::parseInt).boxed().collect(Collectors.toList()));
                    tokenStore.getPosting().add(memoryPosting);
                }
            }
        }
    }
}
