package search;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.sqlite.SQLiteDataSource;
import search.domain.Document;
import search.domain.Token;

/**
 * on 2018/7/26.
 */
public class DbUtil {
    private String createTableSql[] = new String[]{
            "CREATE TABLE IF NOT EXISTS document(\n" +
                    "  id INTEGER PRIMARY KEY AUTOINCREMENT ,\n" +
                    "  title text,\n" +
                    "  text text\n" +
                    ");\n" +
                    "\n",
            "CREATE TABLE IF NOT EXISTS token(\n" +
                    "  id INTEGER PRIMARY KEY AUTOINCREMENT ,\n" +
                    "  text text,\n" +
                    "  docCount INTEGER,\n" +
                    "  positionsCount INTEGER,\n" +
                    "  post text\n" +
                    ");",
            "create index IF NOT EXISTS  token_text on token (text);",
            "create index IF NOT EXISTS  document_title on document (title);"
    };
    private final QueryRunner queryRunner;

    public DbUtil(String storeLocation) throws Exception {
        SQLiteDataSource ds = new SQLiteDataSource();
        ds.setUrl("jdbc:sqlite:" + storeLocation);
        queryRunner = new QueryRunner(ds);
        for (String sql : createTableSql) queryRunner.update(sql);
    }

    public Integer getIDFromDocumentTitle(String title) throws Exception {
        ScalarHandler<Integer> scalarHandler = new ScalarHandler<>();
        return queryRunner.query("select id from document where title = ? ", scalarHandler, title);
    }

    public Document getDocumentById(Integer id) throws Exception {
        return queryRunner.query("select id ,title,text from document where id = ? ", new BeanHandler<>(Document.class), id);

    }

    public Integer countDocument() throws Exception {
        ScalarHandler<Integer> scalarHandler = new ScalarHandler<>();
        return queryRunner.query("select count(*) from document ", scalarHandler);
    }

    public Token getTokenByText(String tokenText) throws Exception {
        return queryRunner.query("select id ,text,docCount,positionsCount, post from token where text = ? ", new BeanHandler<>(Token.class), tokenText);
    }

    public int updateTokenById(Token token) throws Exception {
        return queryRunner.update("update token set docCount =?,positionsCount=?,post=?  where id = ? ",
                token.getDocCount(), token.getPositionsCount(), token.getPost(), token.getId());
    }

    public Integer insertToken(Token token) throws Exception {
        ScalarHandler<Integer> scalarHandler = new ScalarHandler<>();
        return queryRunner.insert("insert into token(text,docCount,positionsCount,post) values (?,?,?,?) ", scalarHandler,
                token.getText(), token.getDocCount(), token.getPositionsCount(), token.getPost());
    }

    public Integer insertNewDocument(Document document) throws Exception {
        ScalarHandler<Integer> scalarHandler = new ScalarHandler<>();
        Integer newId = queryRunner.insert("insert into document(title,text) values(?,?)  ", scalarHandler, document.getTitle(), document.getText());
        document.setId(newId);
        return newId;
    }

    public static void main(String[] args) throws Exception {
        DbUtil dbUtil = new DbUtil("E:\\file\\test.db");
        Document document = new Document();
        document.setTitle("test");
        document.setText("next");
        System.err.println(dbUtil.insertNewDocument(document));
        System.err.println(dbUtil.getIDFromDocumentTitle("test"));
        ;
    }
}
