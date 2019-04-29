package com.github.growingspaghetti.eesti;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class EestiWikipediaTest {
    //@Test
    public void wikipedia() throws Exception {
        new File("wikipedia").mkdir();
        List<String> anki = new ArrayList<>();
        String       text = FileUtils.readFileToString(new File("vowels/estonian-vowels"), "UTF-8");
        String[] entries  = text.split("\n\n");
        for (String entry : entries) {
            //System.out.println("---------\n" + entry);
            String[] lines        = entry.split("\n");
            String eestiChar      = lines[0];
            String pronunciatiion = lines[1];
            String name           = lines[2];
            String mp3            = ""; //mut
            String desc           = ""; //mut
            if (lines.length > 3) {
                desc += lines[3];
                mp3 = "[sound:" + lines[4].replaceAll("\\.ogg$", ".mp3") + "]";
                desc += "<br>" + lines[5];
                desc += "<br>" + lines[6];
            }
            String l = String.format("%s\t<img src=\"eesti.jpg\">\t<img src=\"Esling_vowel_chart.png\"><br>%s<br>%s\t%s\t%s", eestiChar, pronunciatiion, name, desc, mp3);
            anki.add(l);
        }
        FileUtils.writeLines(new File("wikipedia/anki.txt"), "UTF-8", anki, false);
    }
}
