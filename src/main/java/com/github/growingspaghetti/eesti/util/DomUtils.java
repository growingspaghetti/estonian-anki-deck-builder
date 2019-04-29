package com.github.growingspaghetti.eesti.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.apache.xerces.parsers.DOMParser;
import org.apache.xerces.xni.parser.XMLDocumentFilter;
import org.cyberneko.html.HTMLConfiguration;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class DomUtils {
    XPathFactory xPathFactory = XPathFactory.newInstance();
    XPath        xpath        = xPathFactory.newXPath();

    // http://d.hatena.ne.jp/kaiseh/20090219/1235058388
    public static Document parseHtml(String s) throws Exception {
        HTMLConfiguration config = new HTMLConfiguration();
        DOMParser         parser = new DOMParser(config);
        parser.setProperty("http://cyberneko.org/html/properties/names/elems", "default");
        parser.setProperty("http://cyberneko.org/html/properties/names/attrs", "default");
        parser.setProperty("http://cyberneko.org/html/properties/filters", new XMLDocumentFilter[0]);
        parser.setFeature("http://cyberneko.org/html/features/balance-tags", false);
        try (InputStream is = new ByteArrayInputStream(s.getBytes("UTF-8"))) {
            InputSource in = new InputSource(is);
            in.setEncoding("UTF-8");
            parser.parse(in);
            return parser.getDocument();
        }
    }
}
