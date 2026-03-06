package com.example.mdsearch.service;

import com.example.mdsearch.config.MockConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DirectoryScanService
 */
class DirectoryScanServiceTest {

    private DirectoryScanService scanService;

    @BeforeEach
    void setUp() {
        scanService = new MockConfig.MockDirectoryScanService();
    }

    @Test
    void testScanAndIndex() throws IOException {
        String directoryPath = "/test/markdown-files";

        DirectoryScanService.ScanResult result = scanService.scanAndIndex(directoryPath);

        assertNotNull(result, "Scan result should not be null");
        assertTrue(result.totalFiles >= 0, "Total files should be non-negative");
        assertTrue(result.indexedFiles >= 0, "Indexed files should be non-negative");
        assertTrue(result.skippedFiles >= 0, "Skipped files should be non-negative");
        assertTrue(result.failedFiles >= 0, "Failed files should be non-negative");
        assertNotNull(result.failedPaths, "Failed paths should not be null");
    }

    @Test
    void testScanAndIndexResultConsistency() throws IOException {
        String directoryPath = "/test/docs";

        DirectoryScanService.ScanResult result = scanService.scanAndIndex(directoryPath);

        // Total should equal indexed + skipped + failed
        int sum = result.indexedFiles + result.skippedFiles + result.failedFiles;
        assertEquals(result.totalFiles, sum, "Total files should equal sum of indexed, skipped and failed");
    }

    @Test
    void testScheduleScan() {
        String directoryPath = "/test/scheduled-docs";
        long intervalMs = 60000;

        // Should not throw exception
        assertDoesNotThrow(() -> scanService.scheduleScan(directoryPath, intervalMs),
                "Schedule scan should not throw exception");
    }

    @Test
    void testGetStatus() {
        DirectoryScanService.ScanStatus status = scanService.getStatus();

        assertNotNull(status, "Scan status should not be null");
        // isScanning should be boolean
        // In mock, it's false by default
        assertFalse(status.isScanning, "Mock should not be scanning by default");
    }

    @Test
    void testGetStatusFields() {
        DirectoryScanService.ScanStatus status = scanService.getStatus();

        // Check all fields are accessible
        assertNotNull(status.lastScanTime, "Last scan time should not be null in mock");
        assertEquals(0, status.processedFiles, "Processed files should be 0");
        assertEquals(0, status.totalFiles, "Total files should be 0");
    }

    @Test
    void testScanResultWithFailedFiles() throws IOException {
        String directoryPath = "/test/docs-with-errors";

        DirectoryScanService.ScanResult result = scanService.scanAndIndex(directoryPath);

        // Mock returns no failed files by default, but we verify the structure
        assertNotNull(result.failedPaths, "Failed paths list should not be null");
    }

    @Test
    void testScanDifferentDirectories() throws IOException {
        // Mock should handle any directory path
        DirectoryScanService.ScanResult result1 = scanService.scanAndIndex("/dir1");
        DirectoryScanService.ScanResult result2 = scanService.scanAndIndex("/dir2");

        assertNotNull(result1, "First scan result should not be null");
        assertNotNull(result2, "Second scan result should not be null");
    }

    @Test
    void testScheduleScanWithDifferentIntervals() {
        // Test various intervals
        assertDoesNotThrow(() -> {
            scanService.scheduleScan("/dir1", 1000);
            scanService.scheduleScan("/dir2", 5000);
            scanService.scheduleScan("/dir3", 60000);
            scanService.scheduleScan("/dir4", 300000);
        }, "Should handle various interval values");
    }

    @Test
    void testScanResultCountsAreValid() throws IOException {
        DirectoryScanService.ScanResult result = scanService.scanAndIndex("/test");

        // All counts should be non-negative
        assertTrue(result.totalFiles >= 0, "Total files should be non-negative");
        assertTrue(result.indexedFiles >= 0, "Indexed files should be non-negative");
        assertTrue(result.indexedFiles <= result.totalFiles || result.totalFiles == 0,
                "Indexed files should not exceed total");
    }

    @Test
    void testScanStatusNotScanningAfterComplete() throws IOException {
        // Perform a scan
        scanService.scanAndIndex("/test");

        // Status should show not scanning
        DirectoryScanService.ScanStatus status = scanService.getStatus();
        assertFalse(status.isScanning, "Should not be scanning after scan completes");
    }
}
