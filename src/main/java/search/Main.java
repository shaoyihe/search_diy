package search;

import search.domain.Env;
import search.domain.SearchSuccRecord;
import search.util.Asserts;
import search.util.Log;

import java.util.List;

/**
 * on 2018/7/26.
 */
public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            String usage = "usage: java search.Main [options]\n" +
                    "options:\n" +
                    "  -x wikipedia_dump_xml         : 维基百科dump文件路径\n" +
                    "  -q 搜索内容                    : 搜索内容 \n" +
                    "  -m 50                         : 当建立索引时，最大提取数量\n" +
                    "  -s search.db                  : 存储路径\n";
            Log.log(usage);
            return;
        }
        String queryText = null;
        Env env = new Env();
        for (int i = 0; i < args.length; ++i) {
            if (args[i].equalsIgnoreCase("-s")) {
                env.setStoreLocation(args[i + 1]);
                ++i;
            } else if (args[i].equalsIgnoreCase("-x")) {
                env.setWikiLocation(args[i + 1]);
                ++i;
            } else if (args[i].equalsIgnoreCase("-q")) {
                queryText = args[i + 1].toLowerCase();
                ++i;
            } else if (args[i].equalsIgnoreCase("-m")) {
                env.setMaxProcessDocument(Integer.parseInt(args[i + 1]));
                ++i;
            }
        }

        Asserts.isTrue(env.getStoreLocation() != null, "存储路径不能为空");
        if (queryText == null) {
            store(env);
        } else {
            Asserts.isTrue(env.getWikiLocation() != null, "维基路径不能为空");
            search(env, queryText);
        }

    }

    private static void search(Env env, String searchText) throws Exception {
        IndexSearcher indexSearcher = new IndexSearcher(env);
        List<SearchSuccRecord> searchSuccRecords = indexSearcher.search(searchText);
        if (searchSuccRecords == null) {
            Log.log("no records");
        } else {
            searchSuccRecords.forEach(System.err::println);
        }
    }

    private static void store(Env env) throws Exception {
        WikiDataSourceManager wikiDataSourceManager = new WikiDataSourceManager(env);
        wikiDataSourceManager.constructAndProcess();
    }
}
