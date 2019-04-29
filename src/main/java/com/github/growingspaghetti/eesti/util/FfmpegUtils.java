package com.github.growingspaghetti.eesti.util;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;

/**
 * @author ryoji
 */
public class FfmpegUtils {
    public static Optional<File> ffmpeg(File output, List<File> mp3s) throws Exception {
        if (mp3s.isEmpty()) {
            return Optional.empty();
        }

        StringBuilder sb = new StringBuilder();
        for (File f : mp3s) {
            sb.append("|" + f.getAbsolutePath());
        }

        List<String> command = new ArrayList<String>();
        command.add("ffmpeg");
        command.add("-i");
        if (mp3s.size() == 1) {
            command.add(sb.substring(1));
        } else {
            command.add("concat:" + sb.substring(1));
        }
        command.add("-y");
        command.add(output.getAbsolutePath());

        System.out.println(command.stream().collect(Collectors.joining(" ")));
        ProcessBuilder builder = new ProcessBuilder(command);
        Process        process = builder.start();

        try (InputStream stream = process.getErrorStream();) {
            String out = IOUtils.toString(stream, "UTF-8");
            System.out.println(out);
        }
        return Optional.of(output);
    }
}
