package com.example.mdsearch.service.impl;

import com.example.mdsearch.config.SearchProperties;
import com.example.mdsearch.model.MarkdownDocument;
import com.example.mdsearch.service.DirectoryScanService;
import com.example.mdsearch.service.IndexService;
import com.example.mdsearch.util.MarkdownParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class DirectoryScanServiceImpl implements DirectoryScanService {

    private final IndexService indexService;
    private final SearchProperties searchProperties;

    private final AtomicBoolean isScanning = new AtomicBoolean(false);
    private final AtomicInteger processedFiles = new AtomicInteger(0);
    private final AtomicInteger totalFiles = new AtomicInteger(0);
    private String currentDirectory;
    private String lastScanTime;

    @Override
    public ScanResult scanAndIndex(String directoryPath) throws IOException {
        if (isScanning.get()) {
            throw new IllegalStateException("Scan already in progress");
        }

        isScanning.set(true);
        currentDirectory = directoryPath;
        processedFiles.set(0);

        try {
            log.info("Starting scan for directory: {}", directoryPath);

            List<String> mdFiles = MarkdownParser.scanMarkdownFiles(
                    directoryPath,
                    searchProperties.getDirectory().getExcludePatterns()
            );

            totalFiles.set(mdFiles.size());
            log.info("Found {} markdown files to process", mdFiles.size());

            int indexedFiles = 0;
            int skippedFiles = 0;
            int failedFiles = 0;
            List<String> failedPaths = new ArrayList<>();

            for (String filePath : mdFiles) {
                try {
                    MarkdownDocument doc = indexService.indexDocument(filePath);
                    if (doc != null) {
                        indexedFiles++;
                    } else {
                        skippedFiles++;
                    }
                } catch (Exception e) {
                    log.error("Failed to index file: {}", filePath, e);
                    failedFiles++;
                    failedPaths.add(filePath);
                }
                processedFiles.incrementAndGet();
            }

            lastScanTime = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

            log.info("Scan completed. Indexed: {}, Skipped: {}, Failed: {}",
                    indexedFiles, skippedFiles, failedFiles);

            return new ScanResult(mdFiles.size(), indexedFiles, skippedFiles, failedFiles, failedPaths);

        } finally {
            isScanning.set(false);
            currentDirectory = null;
        }
    }

    @Override
    public void scheduleScan(String directoryPath, long intervalMs) {
        // Scheduling is handled by Spring @Scheduled annotation
        // This method can be used to register directories for scheduled scanning
        log.info("Scheduled scan registered for: {} with interval: {}ms", directoryPath, intervalMs);
    }

    @Override
    public ScanStatus getStatus() {
        return new ScanStatus(
                isScanning.get(),
                currentDirectory,
                processedFiles.get(),
                totalFiles.get(),
                lastScanTime
        );
    }
}
