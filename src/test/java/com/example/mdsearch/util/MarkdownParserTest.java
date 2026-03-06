package com.example.mdsearch.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MarkdownParser
 */
class MarkdownParserTest {

    @TempDir
    Path tempDir;

    @Test
    void testExtractTitleFromH1() {
        String content = "# Main Title\n\nSome content here";

        String title = MarkdownParser.extractTitle(content);

        assertEquals("Main Title", title, "Should extract title from H1");
    }

    @Test
    void testExtractTitleFromContentWithMultipleHeaders() {
        String content = "## Sub Title\n# Main Title\n### Another Title";

        String title = MarkdownParser.extractTitle(content);

        assertEquals("Main Title", title, "Should extract first H1 title");
    }

    @Test
    void testExtractTitleNoH1() {
        String content = "## Only Sub Title\nSome content";

        String title = MarkdownParser.extractTitle(content);

        assertEquals("Untitled", title, "Should return 'Untitled' when no H1 found");
    }

    @Test
    void testExtractTitleEmptyContent() {
        String title = MarkdownParser.extractTitle("");

        assertEquals("Untitled", title, "Should return 'Untitled' for empty content");
    }

    @Test
    void testExtractTitleNullContent() {
        // Null check - should handle gracefully
        assertThrows(NullPointerException.class, () -> {
            MarkdownParser.extractTitle(null);
        });
    }

    @Test
    void testExtractTitleWithLeadingWhitespace() {
        String content = "   # Title with leading whitespace";

        String title = MarkdownParser.extractTitle(content);

        assertEquals("Title with leading whitespace", title, "Should handle leading whitespace");
    }

    @Test
    void testExtractTitleWithTrailingHash() {
        String content = "# Title #\n\nContent";

        String title = MarkdownParser.extractTitle(content);

        assertEquals("Title #", title, "Should preserve trailing hash in title");
    }

    @Test
    void testExtractPlainText() {
        String markdown = "# Title\n\nThis is **bold** and *italic* text.";

        String plainText = MarkdownParser.extractPlainText(markdown);

        assertNotNull(plainText, "Plain text should not be null");
        assertTrue(plainText.contains("Title"), "Should contain title text");
        assertTrue(plainText.contains("bold"), "Should contain bold text");
        assertTrue(plainText.contains("italic"), "Should contain italic text");
    }

    @Test
    void testExtractPlainTextWithLinks() {
        String markdown = "Check out [this link](https://example.com) for more info.";

        String plainText = MarkdownParser.extractPlainText(markdown);

        assertTrue(plainText.contains("this link"), "Should contain link text");
    }

    @Test
    void testExtractPlainTextWithCodeBlock() {
        String markdown = "Here is some code:\n\n```java\nSystem.out.println(\"Hello\");\n```\n\nEnd.";

        String plainText = MarkdownParser.extractPlainText(markdown);

        assertTrue(plainText.contains("System.out.println"), "Should contain code content");
    }

    @Test
    void testExtractPlainTextWithList() {
        String markdown = "- Item 1\n- Item 2\n- Item 3";

        String plainText = MarkdownParser.extractPlainText(markdown);

        assertTrue(plainText.contains("Item 1"), "Should contain list item 1");
        assertTrue(plainText.contains("Item 2"), "Should contain list item 2");
        assertTrue(plainText.contains("Item 3"), "Should contain list item 3");
    }

    @Test
    void testReadFileContent() throws IOException {
        String testContent = "Test file content\nLine 2\nLine 3";
        Path testFile = tempDir.resolve("test.md");
        Files.write(testFile, testContent.getBytes());

        String content = MarkdownParser.readFileContent(testFile.toString());

        assertEquals(testContent, content, "Should read file content correctly");
    }

    @Test
    void testReadFileContentNonExistent() {
        assertThrows(IOException.class, () -> {
            MarkdownParser.readFileContent("/non/existent/file.md");
        }, "Should throw IOException for non-existent file");
    }

    @Test
    void testCalculateHash() {
        String content1 = "Test content";
        String content2 = "Test content";
        String content3 = "Different content";

        String hash1 = MarkdownParser.calculateHash(content1);
        String hash2 = MarkdownParser.calculateHash(content2);
        String hash3 = MarkdownParser.calculateHash(content3);

        assertEquals(hash1, hash2, "Same content should produce same hash");
        assertNotEquals(hash1, hash3, "Different content should produce different hash");
    }

    @Test
    void testCalculateHashFormat() {
        String content = "Test content";
        String hash = MarkdownParser.calculateHash(content);

        // MD5 hash is 32 hex characters
        assertEquals(32, hash.length(), "MD5 hash should be 32 characters");
        assertTrue(hash.matches("[0-9a-f]+"), "Hash should be lowercase hex");
    }

    @Test
    void testCalculateHashEmptyString() {
        String hash = MarkdownParser.calculateHash("");

        assertNotNull(hash, "Hash should not be null for empty string");
        assertEquals(32, hash.length(), "Hash should still be 32 characters");
    }

    @Test
    void testScanMarkdownFiles() throws IOException {
        // Create test markdown files
        Files.createFile(tempDir.resolve("doc1.md"));
        Files.createFile(tempDir.resolve("doc2.md"));
        Files.createFile(tempDir.resolve("readme.txt")); // Not a markdown file

        List<String> mdFiles = MarkdownParser.scanMarkdownFiles(tempDir.toString());

        assertEquals(2, mdFiles.size(), "Should find 2 markdown files");
    }

    @Test
    void testScanMarkdownFilesNonExistentDirectory() {
        assertThrows(IOException.class, () -> {
            MarkdownParser.scanMarkdownFiles("/non/existent/directory");
        }, "Should throw IOException for non-existent directory");
    }

    @Test
    void testScanMarkdownFilesEmptyDirectory() throws IOException {
        Path emptyDir = tempDir.resolve("empty");
        Files.createDirectory(emptyDir);

        List<String> mdFiles = MarkdownParser.scanMarkdownFiles(emptyDir.toString());

        assertTrue(mdFiles.isEmpty(), "Empty directory should return empty list");
    }

    @Test
    void testScanMarkdownFilesRecursive() throws IOException {
        // Create nested directory structure
        Path subDir = tempDir.resolve("subdir");
        Files.createDirectory(subDir);
        Files.createFile(tempDir.resolve("root.md"));
        Files.createFile(subDir.resolve("nested.md"));

        List<String> mdFiles = MarkdownParser.scanMarkdownFiles(tempDir.toString());

        assertEquals(2, mdFiles.size(), "Should find markdown files in nested directories");
    }

    @Test
    void testScanMarkdownFilesWithExcludePatterns() throws IOException {
        // Create files in different directories
        Path nodeModules = tempDir.resolve("node_modules");
        Path gitDir = tempDir.resolve(".git");
        Files.createDirectory(nodeModules);
        Files.createDirectory(gitDir);

        Files.createFile(tempDir.resolve("main.md"));
        Files.createFile(nodeModules.resolve("excluded.md"));
        Files.createFile(gitDir.resolve("config.md"));

        String[] excludePatterns = {"node_modules", ".git"};
        List<String> mdFiles = MarkdownParser.scanMarkdownFiles(tempDir.toString(), excludePatterns);

        assertEquals(1, mdFiles.size(), "Should exclude files in node_modules and .git");
        assertFalse(mdFiles.get(0).contains("node_modules"), "Should not include node_modules files");
        assertFalse(mdFiles.get(0).contains(".git"), "Should not include .git files");
    }

    @Test
    void testScanMarkdownFilesWithNullExcludePatterns() throws IOException {
        Files.createFile(tempDir.resolve("doc1.md"));
        Files.createFile(tempDir.resolve("doc2.md"));

        List<String> mdFiles = MarkdownParser.scanMarkdownFiles(tempDir.toString(), null);

        assertEquals(2, mdFiles.size(), "Null exclude patterns should not filter any files");
    }

    @Test
    void testScanMarkdownFilesWithEmptyExcludePatterns() throws IOException {
        Files.createFile(tempDir.resolve("doc1.md"));

        List<String> mdFiles = MarkdownParser.scanMarkdownFiles(tempDir.toString(), new String[]{});

        assertEquals(1, mdFiles.size(), "Empty exclude patterns should not filter any files");
    }

    @Test
    void testExtractPlainTextWithChinese() {
        String markdown = "# \u4e2d\u6587\u6807\u9898\n\n\u8fd9\u662f\u4e2d\u6587\u5185\u5bb9\u3002";

        String plainText = MarkdownParser.extractPlainText(markdown);

        assertTrue(plainText.contains("\u4e2d\u6587\u6807\u9898"), "Should handle Chinese title");
        assertTrue(plainText.contains("\u4e2d\u6587\u5185\u5bb9"), "Should handle Chinese content");
    }

    @Test
    void testExtractTitleWithChinese() {
        String content = "# \u4e2d\u6587\u6807\u9898\n\n\u5185\u5bb9";

        String title = MarkdownParser.extractTitle(content);

        assertEquals("\u4e2d\u6587\u6807\u9898", title, "Should extract Chinese title correctly");
    }
}
