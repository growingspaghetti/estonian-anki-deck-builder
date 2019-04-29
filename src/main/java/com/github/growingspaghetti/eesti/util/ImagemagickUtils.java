package com.github.growingspaghetti.eesti.util;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;

/**
 * @author ryoji
 */
public class ImagemagickUtils {
    public static File convert(File input, File output) throws Exception {
        List<String> command = new ArrayList<String>();
        command.add("convert");
        command.add(input.getAbsolutePath());
        command.add(output.getAbsolutePath());

        System.out.println(command.stream().collect(Collectors.joining(" ")));
        ProcessBuilder builder = new ProcessBuilder(command);
        Process        process = builder.start();

        try (InputStream stream = process.getErrorStream();) {
            String out = IOUtils.toString(stream, "UTF-8");
            System.out.println(out);
        }
        return output;
    }
}
