import java.io.*;
import java.nio.file.*;

/**
 * System Verification Test
 * Run this to verify your USB Key Driver setup is correct
 */
public class SystemTest {
    
    private static final String PROTECTED_FOLDER = "C:\\JAVA CODE VSC\\COSC439 Project\\ProtectedFiles";
    private static final String TEST_FILE = "test_file.txt";
    
    private static int passedTests = 0;
    private static int failedTests = 0;
    
    public static void main(String[] args) {
        System.out.println("╔═══════════════════════════════════════════════════════╗");
        System.out.println("║       USB KEY DRIVER - SYSTEM VERIFICATION TEST       ║");
        System.out.println("╚═══════════════════════════════════════════════════════╝\n");
        
        // Run all tests
        testProtectedFolderExists();
        testCanCreateTestFile();
        testUSBDriveDetection();
        testPermissionCommands();
        testApplicationDetection();
        testLogFileCreation();
        
        // Summary
        System.out.println("\n" + "═".repeat(55));
        System.out.println("TEST SUMMARY");
        System.out.println("═".repeat(55));
        System.out.printf("✓ Passed: %d\n", passedTests);
        System.out.printf("✗ Failed: %d\n", failedTests);
        System.out.printf("Total:    %d\n", passedTests + failedTests);
        
        if (failedTests == 0) {
            System.out.println("\n🎉 All tests passed! Your system is ready.");
        } else {
            System.out.println("\n⚠️  Some tests failed. Please review the errors above.");
        }
    }
    
    // ==================== TEST CASES ====================
    
    private static void testProtectedFolderExists() {
        System.out.println("Test 1: Protected Folder Exists");
        System.out.println("─".repeat(55));
        
        File folder = new File(PROTECTED_FOLDER);
        
        if (folder.exists() && folder.isDirectory()) {
            pass("Protected folder exists at: " + PROTECTED_FOLDER);
        } else {
            fail("Protected folder not found!");
            System.out.println("   Creating folder...");
            if (folder.mkdirs()) {
                System.out.println("   ✓ Folder created successfully");
            } else {
                System.out.println("   ✗ Failed to create folder");
            }
        }
        System.out.println();
    }
    
    private static void testCanCreateTestFile() {
        System.out.println("Test 2: File Creation Test");
        System.out.println("─".repeat(55));
        
        try {
            File testFile = new File(PROTECTED_FOLDER, TEST_FILE);
            Files.write(testFile.toPath(), "This is a test file.".getBytes());
            
            if (testFile.exists()) {
                pass("Can create files in protected folder");
                testFile.delete(); // Clean up
            } else {
                fail("File creation failed");
            }
        } catch (IOException e) {
            fail("Cannot create files: " + e.getMessage());
        }
        System.out.println();
    }
    
    private static void testUSBDriveDetection() {
        System.out.println("Test 3: USB Drive Detection");
        System.out.println("─".repeat(55));
        
        File[] roots = File.listRoots();
        System.out.println("   Detected drives:");
        
        boolean foundRemovable = false;
        for (File root : roots) {
            String path = root.getAbsolutePath();
            long totalSpace = root.getTotalSpace();
            
            System.out.printf("   • %s (%.2f GB)\n", 
                path, 
                totalSpace / (1024.0 * 1024.0 * 1024.0)
            );
            
            // Check if likely removable (not C: or D:)
            if (!path.startsWith("C:") && !path.startsWith("D:") && totalSpace > 0) {
                foundRemovable = true;
            }
        }
        
        if (foundRemovable) {
            pass("Found potential USB drives");
        } else {
            warn("No removable drives detected (insert USB to test)");
        }
        System.out.println();
    }
    
    private static void testPermissionCommands() {
        System.out.println("Test 4: Permission Commands Test");
        System.out.println("─".repeat(55));
        
        try {
            // Try to run icacls to verify it's available
            Process process = Runtime.getRuntime().exec("icacls \"" + PROTECTED_FOLDER + "\"");
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
            );
            
            String line = reader.readLine();
            process.waitFor();
            reader.close();
            
            if (line != null) {
                pass("icacls command is available");
            } else {
                fail("icacls command not responding");
            }
            
        } catch (Exception e) {
            fail("Cannot execute icacls: " + e.getMessage());
            System.out.println("   Note: You may need Administrator privileges");
        }
        System.out.println();
    }
    
    private static void testApplicationDetection() {
        System.out.println("Test 5: Application Detection Test");
        System.out.println("─".repeat(55));
        
        try {
            // Test if we can detect running processes
            Process process = Runtime.getRuntime().exec("tasklist");
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
            );
            
            int count = 0;
            String line;
            while ((line = reader.readLine()) != null && count < 3) {
                if (line.toLowerCase().contains(".exe")) {
                    System.out.println("   Found: " + line.substring(0, Math.min(50, line.length())));
                    count++;
                }
            }
            
            reader.close();
            process.waitFor();
            
            if (count > 0) {
                pass("Can detect running applications");
            } else {
                fail("Cannot detect applications");
            }
            
        } catch (Exception e) {
            fail("Cannot execute tasklist: " + e.getMessage());
        }
        System.out.println();
    }
    
    private static void testLogFileCreation() {
        System.out.println("Test 6: Log File Creation Test");
        System.out.println("─".repeat(55));
        
        String logPath = "C:\\JAVA CODE VSC\\COSC439 Project\\test_log.txt";
        
        try {
            PrintWriter writer = new PrintWriter(new FileWriter(logPath, true));
            writer.println("Test log entry");
            writer.close();
            
            File logFile = new File(logPath);
            if (logFile.exists()) {
                pass("Can create log files");
                logFile.delete(); // Clean up
            } else {
                fail("Log file not created");
            }
            
        } catch (IOException e) {
            fail("Cannot create log file: " + e.getMessage());
        }
        System.out.println();
    }
    
    // ==================== HELPER METHODS ====================
    
    private static void pass(String message) {
        System.out.println("   ✓ PASS: " + message);
        passedTests++;
    }
    
    private static void fail(String message) {
        System.out.println("   ✗ FAIL: " + message);
        failedTests++;
    }
    
    private static void warn(String message) {
        System.out.println("   ⚠ WARN: " + message);
        // Warnings don't count as pass or fail
    }
}
