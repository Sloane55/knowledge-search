package com.example.mdsearch.util;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.text.TextContentRenderer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class MarkdownParser {

    private static final Parser parser = Parser.builder().build();
    private static final TextContentRenderer textRenderer = TextContentRenderer.builder().build();

    public static String extractTitle(String content) {
        String[] lines = content.split("\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.startsWith("# ")) {
                return trimmed.substring(2).trim();
            }
        }
        return "Untitled";
    }

    public static String extractPlainText(String markdown) {
        Node document = parser.parse(markdown);
        return textRenderer.render(document);
    }

    public static String readFileContent(String filePath) throws IOException {
        return new String(Files.readAllBytes(Path.of(filePath)));
    }

    public static String calculateHash(String content) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(content.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<String> scanMarkdownFiles(String directoryPath) throws IOException {
        List<String> mdFiles = new ArrayList<>();
        Path startPath = Path.of(directoryPath);

        if (!Files.exists(startPath)) {
            throw new IOException("Directory does not exist: " + directoryPath);
        }

        Files.walk(startPath)
                .filter(Files::isRegularFile)
                .filter(p -> p.toString().endsWith(".md"))
                .forEach(p -> mdFiles.add(p.toString()));

        return mdFiles;
    }

    public static List<String> scanMarkdownFiles(String directoryPath, String[] excludePatterns) throws IOException {
        List<String> allFiles = scanMarkdownFiles(directoryPath);

        if (excludePatterns == null || excludePatterns.length == 0) {
            return allFiles;
        }

        List<String> filteredFiles = new ArrayList<>();
        for (String file : allFiles) {
            boolean excluded = false;
            for (String pattern : excludePatterns) {
                if (file.contains(pattern)) {
                    excluded = true;
                    break;
                }
            }
            if (!excluded) {
                filteredFiles.add(file);
            }
        }

        return filteredFiles;
    }
}
