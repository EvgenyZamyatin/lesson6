package odeen.newrssreader.proj.parser;

import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import odeen.newrssreader.proj.model.Item;

/**
 * Created by Женя on 04.11.2014.
 */
public class Parser {
    private static final String ns = null;
    SimpleDateFormat format = new SimpleDateFormat("E, dddd MMMM yyyy k:m:s z");
    public List parse(InputStream in) throws XmlPullParserException, IOException{
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readFeed(parser);
        } finally {
            in.close();
        }
    }

    private List readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
        List items = new ArrayList();
        parser.require(XmlPullParser.START_TAG, ns, "rss");
        parser.nextTag();
        parser.require(XmlPullParser.START_TAG, ns, "channel");

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG)
                continue;
            String name = parser.getName();
            if (name.equals("item")) {
                items.add(readItem(parser));
            } else {
                skip(parser);
            }
        }
        return items;
    }

    private Item readItem(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "item");
        String title = null;
        String description = null;
        String link = null;
        Date pubdate = null;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("title")) {
                title = readTitle(parser);
            } else if(name.equals("link")) {
                link = readLink(parser);
            } else if (name.equals("description")) {
                description = readDescription(parser);
            } else if (name.equals("pubDate")) {
                pubdate = readPubdate(parser);
            } else {
                skip(parser);
            }
        }
        Item item = new Item();
        item.setLink(link);
        item.setTitle(title);
        item.setPubDate(pubdate);
        item.setDescription(description);
        return item;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    --depth;
                    break;
                case XmlPullParser.START_TAG:
                    ++depth;
                    break;
            }
        }

    }

    private String readTitle(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "title");
        String title = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "title");
        return title;
    }
    private String readLink(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "link");
        String link = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "link");
        return link;
    }
    private String readDescription(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "description");
        String description = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "description");
        return description;
    }
    private Date readPubdate(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "pubDate");
        String date = readText(parser);
        Date res = null;
        try {
            res = format.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        parser.require(XmlPullParser.END_TAG, ns, "pubDate");
        return res;
    }

    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

}
