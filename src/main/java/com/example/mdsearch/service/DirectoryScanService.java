package com.example.mdsearch.service;

import java.io.IOException;
import java.util.List;

public interface DirectoryScanService {

    /**
     * Scan and index a directory
     */
    ScanResult scanAndIndex(String directoryPath) throws IOException;

    /**
     * Schedule periodic scanning
     */
    void scheduleScan(String directoryPath, long intervalMs);

    /**
     * Get current scan status
     */
    ScanStatus getStatus();

    class ScanResult {
        public int totalFiles;
        public int indexedFiles;
        public int skippedFiles;
        public int failedFiles;
        public List<String> failedPaths;

        public ScanResult(int totalFiles, int indexedFiles, int skippedFiles, int failedFiles, List<String> failedPaths) {
            this.totalFiles = totalFiles;
            this.indexedFiles = indexedFiles;
            this.skippedFiles = skippedFiles;
            this.failedFiles = failedFiles;
            this.failedPaths = failedPaths;
        }
    }

    class ScanStatus {
        public boolean isScanning;
        public String currentDirectory;
        public int processedFiles;
        public int totalFiles;
        public String lastScanTime;

        public ScanStatus(boolean isScanning, String currentDirectory, int processedFiles, int totalFiles, String lastScanTime) {
            this.isScanning = isScanning;
            this.currentDirectory = currentDirectory;
            this.processedFiles = processedFiles;
            this.totalFiles = totalFiles;
            this.lastScanTime = lastScanTime;
        }
    }
}
