package com.github.growingspaghetti.eesti;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import com.github.growingspaghetti.eesti.util.CsvUtils;

public class AppTest {
    //@Test
    public void downloadResources() throws Exception {
        for (int i = 1; i < 19; i++) {
            HashMap<String, Boolean> queue = new LinkedHashMap<String, Boolean>() {
                private static final long serialVersionUID = 1L;
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, Boolean> eldest) {
                    return this.size() > 4;
                }
            };
            for (int j = 1; j < 61; j++) {
                String img1 = String.format("https://www.loecsen.com/OrizonFlash_V2/ressources/svg/LABLANG_V2_%d_%s.svg", i, j);
                String mp31 = String.format("https://www.loecsen.com/OrizonFlash_V2/ressources/son/estonien-%d-%s.mp3", i, j);
                String img2 = String.format("https://www.loecsen.com/OrizonFlash_V2/ressources/svg/LABLANG_V2_%d_%sV.svg", i, j);
                String mp32 = String.format("https://www.loecsen.com/OrizonFlash_V2/ressources/son/estonien-%d-%sV.mp3", i, j);
                for (String s : new String[] {img1, mp31, img2, mp32}) {
                    try {
                        URL u    = new URL(s);
                        byte[] b = IOUtils.toByteArray(u);
                        File f   = new File("loeesen/loeesen_" + u.getFile());
                        FileUtils.writeByteArrayToFile(f, b);
                        queue.put(s, true);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        queue.put(s, false);
                    }
                }
                if (queue.values().stream().filter(e -> !e).count() == queue.size()) {
                    break;
                };
            }
        }
    }

    //@Test
    public void tsv() throws Exception {
        List<String[]> lines = CsvUtils.readCsv(new File("loeesen.csv"), ',');
        List<String>   anki  = new ArrayList<>();
        for (String[] l : lines) {
            //String dataId = l[0];
            String eng = l[1];
            String img = l[2];
            String ee  = l[3];
            String mp3 = l[4];
            if (StringUtils.isEmpty(eng)) {
                continue;
            }
            if (StringUtils.isEmpty(img)) {
                String chapter  = StringUtils.substringBetween(mp3, "-");
                String lastPart = StringUtils.substringAfterLast(mp3, "-");
                String index    = StringUtils.substringBefore(lastPart, ".");
                //LABLANG_V2_8_8.svg
                img = String.format("LABLANG_V2_%s_%s.svg", chapter, index);
                System.out.println(Arrays.asList(l).stream().collect(Collectors.joining(" ")));
                System.out.println(mp3);
                System.out.println(chapter + "\t" + index);
            }
            img = img.replaceAll(".svg$", ".png");

            String a = String.format("%s\t<img src=\"%s\">\t%s\t[sound:%s]", eng, img, ee, mp3);
            anki.add(a);
        }
        FileUtils.writeLines(new File("loeesen/anki.txt"), "UTF-8", anki, false);
    }

    @Test
    public void mapEvalCodes() throws Exception {
        List<String[]> lines = CsvUtils.readCsv(new File("loeesen.csv"), ',');
        StringBuilder  sbMp3 = new StringBuilder();
        StringBuilder  sbSvg = new StringBuilder();
        for (String[] l : lines) {
            String dataId = l[0];
            String eng    = l[1];
            String img    = l[2];
            String mp3    = l[4];
            if (StringUtils.isEmpty(eng)) {
                continue;
            }
            if (StringUtils.isEmpty(img)) {
                String chapter  = StringUtils.substringBetween(mp3, "-");
                String lastPart = StringUtils.substringAfterLast(mp3, "-");
                String index    = StringUtils.substringBefore(lastPart, ".");
                //LABLANG_V2_8_8.svg
                img = String.format("LABLANG_V2_%s_%s.svg", chapter, index);
            }
            sbMp3.append(String.format("mp3Map.put(\"%s\",\"%s\");\n", dataId, mp3));
            sbSvg.append(String.format("svgMap.put(\"%s\",\"%s\");\n", dataId, img));
        }
        System.out.println(sbMp3.toString());
        System.out.println("--------");
        System.out.println(sbSvg.toString());
    }

    //@Test
    public void mvResource() throws Exception {
        List<String[]> lines = CsvUtils.readCsv(new File("loeesen/anki.txt"), '\t');
        for (String[] l : lines) {
            String img = StringUtils.substringBetween(l[1], "\"");
            String mp3 = StringUtils.substringBetween(l[3], ":", "]");
            FileUtils.moveFileToDirectory(new File("loeesen/loeesen_/OrizonFlash_V2/ressources/png/" + img),
                                          new File("loeesen-resources"),
                                          true);
            FileUtils.moveFileToDirectory(new File("loeesen/loeesen_/OrizonFlash_V2/ressources/son/" + mp3),
                                          new File("loeesen-resources"),
                                          true);
        }
    }
}
