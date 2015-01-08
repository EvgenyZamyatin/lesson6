package odeen.newrssreader.proj.parser;

import android.content.Intent;
import android.util.Log;
import android.util.Xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import odeen.newrssreader.proj.model.Item;

/**
 * Created by Женя on 04.11.2014.
 */
public class Parser {
    private static final String ns = null;
    private SimpleDateFormat format = new SimpleDateFormat("E, dddd MMMM yyyy k:m:s z");
    private ArrayList<Item> output;

    public List parse(InputStream in) throws XmlPullParserException, IOException, ParserConfigurationException, SAXException {
        FeedHandler feedHandler = new FeedHandler();
        SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
        parser.parse(in, feedHandler);
        return output;
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
        Log.d("pubDate", date);
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

    private class FeedHandler extends DefaultHandler {
        private Item current = null;
        private String last = null;

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            last = "";
            for (int i = 0; i < length; i++)
                last += ch[start + i];
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (qName.equals("item"))
                current = new Item();
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (current == null)
                return;
            if (qName.equals("title"))
                current.setTitle(last);
            if (qName.equals("link"))
                current.setLink(last);
            if (qName.equals("description"))
                current.setDescription(last);
            if (qName.equals("pubDate")) {
                Log.d("pubDate", last);
                Date res = null;
                try {
                    res = format.parse(last);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if (res == null)
                    res = new Date(0);
                current.setPubDate(res);
            }
            if (qName.equals("item"))
                output.add(current);
        }

        @Override
        public void startDocument() throws SAXException {
            super.startDocument();
            output = new ArrayList<Item>();
        }

        @Override
        public void endDocument() throws SAXException {
            super.endDocument();
        }
    }

}
