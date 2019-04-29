package com.github.growingspaghetti.eesti.util;

import java.io.File;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import com.github.growingspaghetti.eesti.data.LoeesenEngEesti;

/**
 * @author ryoji
 */
public class OeesenEngEestiUtils {
    private static File download(URL u, String fname) throws Exception {
        byte[] b = IOUtils.toByteArray(u);
        File f   = new File("loeesen/loeesen_" + fname);
        FileUtils.writeByteArrayToFile(f, b);
        return f;
    }
    public static File downloadMp3(LoeesenEngEesti ee) throws Exception {
        String url   = ee.getMp3();
        String fname = StringUtils.substringAfterLast(url, "/");
        return download(new URL(url), fname);
    }
    public static File downloadSvg(LoeesenEngEesti ee) throws Exception {
        String url   = "https://www.loecsen.com" + ee.getImg();
        String fname = StringUtils.substringAfterLast(url, "/");
        return download(new URL(url), fname);
    }
}
