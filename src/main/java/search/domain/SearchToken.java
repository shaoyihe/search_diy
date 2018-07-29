package search.domain;

public class SearchToken {
    private Token token;
    private int searchPostingIndex = 0;
    private int userSearchPos;
    private int searchPostingPosIndex = 0;

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public int getSearchPostingIndex() {
        return searchPostingIndex;
    }

    public void setSearchPostingIndex(int searchPostingIndex) {
        this.searchPostingIndex = searchPostingIndex;
    }

    public int getUserSearchPos() {
        return userSearchPos;
    }

    public void setUserSearchPos(int userSearchPos) {
        this.userSearchPos = userSearchPos;
    }

    public int getSearchPostingPosIndex() {
        return searchPostingPosIndex;
    }

    public void setSearchPostingPosIndex(int searchPostingPosIndex) {
        this.searchPostingPosIndex = searchPostingPosIndex;
    }

    public Posting searchPosting() {
        return getToken().getPosting().get(getSearchPostingIndex());
    }

    public Integer searchPostingPos() {
        return getToken().getPosting().get(getSearchPostingIndex()).getPositions().get(searchPostingPosIndex);
    }
}
