package com.github.growingspaghetti.eesti;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.github.growingspaghetti.eesti.util.DomUtils;

class GoetheVerlagEeEng {
    final String ee;
    final String en;
    final String mp3;

    public GoetheVerlagEeEng(String ee, String en, String mp3) {
        this.ee  = ee;
        this.en  = en;
        this.mp3 = mp3;
    }
    public String getEe() {
        return ee;
    }
    public String getEn() {
        return en;
    }
    public String getMp3() {
        return mp3;
    }
}

/**
 * @author ryoji
 * @since 2019/4/29
 */
public class GoetheVerlag {
    private enum XpathExpression {
        TR("//table[@class='span5 table-centered']//tr"),
        DIV35(".//div[@class='Stil35']"),
        EE(".//div[@class='Stil45']/div[@style='display:none']"),
        MP3(".//audio/source");
        private final XPathExpression expr;
        private XpathExpression(String s) {
            try {
                XPathFactory xPathFactory = XPathFactory.newInstance();
                XPath        xpath        = xPathFactory.newXPath();
                expr                      = xpath.compile(s);
            } catch (Exception ex) {
                throw new IllegalArgumentException("wrong xpath expression.", ex);
            }
        }
        public XPathExpression getExpr() {
            return expr;
        }
    }

    private static File download(String url) throws Exception {
        String fname = new File(url).getName();
        byte[] b     = IOUtils.toByteArray(new URL(url));
        File f       = new File("goethe/goethe_" + fname);
        FileUtils.writeByteArrayToFile(f, b);
        return f;
    }

    private static void processPage(String lines) throws Exception {
        XPathExpression trxp    = XpathExpression.TR.getExpr();
        XPathExpression div35xp = XpathExpression.DIV35.getExpr();
        XPathExpression eestXp  = XpathExpression.EE.getExpr();
        XPathExpression mp3xp   = XpathExpression.MP3.getExpr();

        Document doc = DomUtils.parseHtml(lines);
        NodeList trs = (NodeList)trxp.evaluate(doc, XPathConstants.NODESET);

        Map<String, GoetheVerlagEeEng> entries = new TreeMap<>();
        for (int i = 0; i < trs.getLength(); i++) {
            Node tr            = trs.item(i);
            Node eestiNullable = (Node)eestXp.evaluate(tr, XPathConstants.NODE);

            Optional<Node> oeesti = Optional.ofNullable(eestiNullable);
            if (!oeesti.isPresent()) {
                continue;
            }

            String ee    = ((Element)oeesti.get()).getTextContent().trim();
            Node   div35 = (Node)div35xp.evaluate(tr, XPathConstants.NODE);
            String en    = ((Element)div35).getTextContent().trim();
            Node   audio = (Node)mp3xp.evaluate(tr, XPathConstants.NODE);
            String mp3   = ((Element)audio).getAttribute("src");

            GoetheVerlagEeEng entry = new GoetheVerlagEeEng(ee, en, mp3);
            entries.put(mp3, entry);

            System.out.println(String.format("%s\n%s\n%s\n", en, ee, mp3));
        }

        List<String> ankiPage = new ArrayList<>();
        for (GoetheVerlagEeEng enee : entries.values()) {
            File   mp3 = download(enee.getMp3());
            String l   = String.format("%s\t%s\t[sound:%s]",
                                     enee.getEn(),
                                     enee.getEe(),
                                     mp3.getName());
            ankiPage.add(l);
        }
        FileUtils.writeLines(new File("goethe/anki.txt"), "UTF-8", ankiPage, true);
    }

    public static void main(String args[]) throws Exception {
        File dir = new File("goethe");
        dir.mkdir();
        FileUtils.cleanDirectory(dir);

        for (int i = 3; i < 103; i++) {
            String s     = String.format("http://www.goethe-verlag.com/book2/EN/ENET/ENET%03d.HTM", i);
            URL    u     = new URL(s);
            String page  = IOUtils.toString(u, "UTF-8");
            String fname = new File(s).getName();
            FileUtils.writeStringToFile(new File("goethe/" + fname), page, "UTF-8");
        }

        List<File> files
            = FileUtils
                  .listFiles(new File("goethe"), new String[] {"HTM"}, false)
                  .stream()
                  .sorted()
                  .collect(Collectors.toList());

        for (File f : files) {
            String lines = FileUtils.readFileToString(f, "UTF-8");
            processPage(lines);
        }
    }
}
