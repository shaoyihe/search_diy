package search;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import search.domain.Env;
import search.util.Log;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;

public class WikiDataSourceManager {
    private Env env;

    private Indexer indexer;

    public WikiDataSourceManager(Env env) throws Exception {
        this.env = env;
        this.indexer = new Indexer(env);
    }

    public void constructAndProcess() throws Exception {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();
        DefaultHandler handler = new DefaultHandler() {
            private String title;
            private StringBuilder result = new StringBuilder();
            private boolean startHandle = false;
            private int hadProcessDocument = 0;


            public void startElement(String uri, String localName, String qName,
                                     Attributes attributes) throws SAXException {

                if (qName.equalsIgnoreCase("text") || qName.equalsIgnoreCase("title")) {
                    result.setLength(0);
                    startHandle = true;
                }

            }

            public void endElement(String uri, String localName,
                                   String qName) throws SAXException {

                if (qName.equalsIgnoreCase("text")) {
                    //find
                    try {
                        Log.log("start with title " + title);
                        indexer.addDocumentToStore(title, result.toString());
                    } catch (Exception e) {
                        throw new SAXException(e);
                    }
                    if (++hadProcessDocument >= env.getMaxProcessDocument()) {
                        throw new EndSAXException();
                    }
                    startHandle = false;

                } else if (qName.equalsIgnoreCase("title")) {
                    title = result.toString();
                    startHandle = false;
                }

            }

            public void characters(char ch[], int start, int length) throws SAXException {
                if (startHandle) result.append(new String(ch, start, length));
            }

        };
        try {
            saxParser.parse(new File(env.getWikiLocation()), handler);
        } catch (EndSAXException e) {
            //
        }
    }

    private class EndSAXException extends SAXException {
    }

}
