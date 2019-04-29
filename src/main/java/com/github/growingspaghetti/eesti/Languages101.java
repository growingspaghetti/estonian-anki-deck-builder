package com.github.growingspaghetti.eesti;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author ryoji
 * @since 2019/4/28
 */
public class Languages101 {
    private static Optional<File> download(String url) throws RuntimeException {
        try {
            String fname = new File(url).getName();
            byte[] b     = IOUtils.toByteArray(new URL(url));
            File f       = new File("101languages/101languages_" + fname);
            FileUtils.writeByteArrayToFile(f, b);
            return Optional.of(f);
        } catch (Exception ex) {
            ex.printStackTrace();
            //Caused by: java.io.FileNotFoundException:
            // https://www.101languages.net/audio/common-words/estonian/siin.mp3
            //throw new IllegalStateException("exception at download", ex);
            return Optional.empty();
        }
    }

    public static void main(String args[]) throws Exception {
        new File("101languages").mkdir();
        URL    u     = new URL("https://www.101languages.net/estonian/most-common-estonian-words/");
        String page  = IOUtils.toString(u, "UTF-8");
        File   saved = new File("101languages/most-common-estonian-words");
        FileUtils.writeStringToFile(saved, page, "UTF-8");

        String lines  = FileUtils.readFileToString(saved, "UTF-8");
        String table  = StringUtils.substringBetween(lines, "<tbody class=\"row-hover\">", "</tbody>");
        String[] rows = StringUtils.substringsBetween(table, "<tr", "</tr>");

        List<String> anki = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            String row = rows[i];

            String[] tds = StringUtils.substringsBetween(row, "<td", "</td>");
            String ee    = "<" + tds[1]; //mut
            String en    = "<" + tds[2]; //mut

            Optional<String> mp3
                = Optional.ofNullable(StringUtils.substringBetween(ee, "src=\"", "\""));
            // https://stackoverflow.com/questions/4154239/java-regex-replaceall-multiline
            ee = ee.replaceAll("(?s)<script type=\"text/javascript\">.*</script>", "</script>");

            ee = ee.replaceAll("<[^>]*>", "").trim();
            en = en.replaceAll("<[^>]*>", "");

            String sound
                = mp3
                      .flatMap(Languages101::download)
                      .map(f -> "[sound:" + f.getName() + "]")
                      .orElse("");

            String l = String.format("%s\t%s\t%s", en, ee, sound);
            anki.add(l);
        }

        FileUtils.writeLines(new File("101languages/anki.txt"), "UTF-8", anki, false);
    }
}
