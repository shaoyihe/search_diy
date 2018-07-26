package search;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.sqlite.SQLiteDataSource;
import search.domain.Document;

/**
 * on 2018/7/26.
 */
public class DbUtil {
    private String createTableSql =
            "CREATE TABLE IF NOT EXISTS document(\n" +
                    "  id INTEGER PRIMARY KEY AUTOINCREMENT ,\n" +
                    "  title text,\n" +
                    "  text text\n" +
                    ");\n" +
                    "\n" +
                    "CREATE TABLE IF NOT EXISTS token(\n" +
                    "  id INTEGER PRIMARY KEY AUTOINCREMENT ,\n" +
                    "  text text,\n" +
                    "  docCount INTEGER,\n" +
                    "  positionsCount INTEGER,\n" +
                    "  post text\n" +
                    ");";
    private final QueryRunner queryRunner;

    public DbUtil(String storeLocation) throws Exception {
        SQLiteDataSource ds = new SQLiteDataSource();
        ds.setUrl("jdbc:sqlite:" + storeLocation);
        queryRunner = new QueryRunner(ds);
        queryRunner.update(createTableSql);
    }

    public Integer getIDFromDocumentTitle(String title) throws Exception {
        ScalarHandler<Integer> scalarHandler = new ScalarHandler<>();
        return queryRunner.query("select id from document where title = ? ", scalarHandler, title);
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
