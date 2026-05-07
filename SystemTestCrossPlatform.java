import java.io.*;
import java.nio.file.*;

/**
 * Cross-Platform System Verification Test
 * Works on Windows, Linux, and macOS
 */
public class SystemTestCrossPlatform {
    
    private static final String OS = System.getProperty("os.name").toLowerCase();
    private static final boolean IS_WINDOWS = OS.contains("windows");
    
    private static String PROTECTED_FOLDER;
    private static final String TEST_FILE = "test_file.txt";
    
    private static int passedTests = 0;
    private static int failedTests = 0;
    
    public static void main(String[] args) {
        // Set protected folder based on OS
        if (IS_WINDOWS) {
            PROTECTED_FOLDER = "C:\\JAVA CODE VSC\\COSC439 Project\\ProtectedFiles";
        } else {
            PROTECTED_FOLDER = System.getProperty("user.home") + "/USBProtectedFiles";
        }
        
        System.out.println("╔═══════════════════════════════════════════════════════╗");
        System.out.println("║   USB KEY DRIVER - CROSS-PLATFORM VERIFICATION TEST   ║");
        System.out.println("╚═══════════════════════════════════════════════════════╝\n");
        System.out.println("Operating System: " + OS + "\n");
        
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
        
        if (IS_WINDOWS) {
            testUSBDetectionWindows();
        } else {
            testUSBDetectionUnix();
        }
        System.out.println();
    }
    
    private static void testUSBDetectionWindows() {
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
            
            if (!path.startsWith("C:") && !path.startsWith("D:") && totalSpace > 0) {
                foundRemovable = true;
            }
        }
        
        if (foundRemovable) {
            pass("Found potential USB drives");
        } else {
            warn("No removable drives detected (insert USB to test)");
        }
    }
    
    private static void testUSBDetectionUnix() {
        String username = System.getProperty("user.name");
        System.out.println("   Checking USB mount points:");
        
        boolean foundUSB = false;
        
        // Check /media/$USER/
        File mediaDir = new File("/media/" + username);
        if (mediaDir.exists() && mediaDir.isDirectory()) {
            File[] drives = mediaDir.listFiles(File::isDirectory);
            if (drives != null && drives.length > 0) {
                System.out.println("   • /media/" + username + "/ - " + drives.length + " drive(s)");
                foundUSB = true;
            }
        }
        
        // Check /Volumes/ (Mac)
        File volumesDir = new File("/Volumes");
        if (volumesDir.exists() && volumesDir.isDirectory()) {
            File[] volumes = volumesDir.listFiles(File::isDirectory);
            if (volumes != null) {
                int count = 0;
                for (File vol : volumes) {
                    if (!vol.getName().contains("Macintosh")) {
                        count++;
                    }
                }
                if (count > 0) {
                    System.out.println("   • /Volumes/ - " + count + " volume(s)");
                    foundUSB = true;
                }
            }
        }
        
        if (foundUSB) {
            pass("USB mount points accessible");
        } else {
            warn("No USB drives detected (insert USB to test)");
        }
    }
    
    private static void testPermissionCommands() {
        System.out.println("Test 4: Permission Commands Test");
        System.out.println("─".repeat(55));
        
        if (IS_WINDOWS) {
            testPermissionsWindows();
        } else {
            testPermissionsUnix();
        }
        System.out.println();
    }
    
    private static void testPermissionsWindows() {
        try {
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
    }
    
    private static void testPermissionsUnix() {
        try {
            Path folderPath = Paths.get(PROTECTED_FOLDER);
            if (Files.exists(folderPath)) {
                // Test if we can read permissions
                Files.getPosixFilePermissions(folderPath);
                pass("POSIX file permissions accessible");
            } else {
                warn("Protected folder doesn't exist yet");
            }
        } catch (UnsupportedOperationException e) {
            fail("POSIX permissions not supported on this filesystem");
        } catch (IOException e) {
            fail("Cannot access file permissions: " + e.getMessage());
        }
    }
    
    private static void testApplicationDetection() {
        System.out.println("Test 5: Application Detection Test");
        System.out.println("─".repeat(55));
        
        try {
            String command = IS_WINDOWS ? "tasklist" : "ps aux";
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
            );
            
            int count = 0;
            String line;
            while ((line = reader.readLine()) != null && count < 3) {
                if (IS_WINDOWS && line.toLowerCase().contains(".exe")) {
                    System.out.println("   Found: " + line.substring(0, Math.min(50, line.length())));
                    count++;
                } else if (!IS_WINDOWS && count == 0) {
                    count = 1; // ps aux returns data
                }
            }
            
            reader.close();
            process.waitFor();
            
            if (count > 0) {
                pass("Can detect running processes");
            } else {
                fail("Cannot detect processes");
            }
            
        } catch (Exception e) {
            fail("Cannot execute process list command: " + e.getMessage());
        }
        System.out.println();
    }
    
    private static void testLogFileCreation() {
        System.out.println("Test 6: Log File Creation Test");
        System.out.println("─".repeat(55));
        
        String logPath = IS_WINDOWS ? 
            "C:\\JAVA CODE VSC\\COSC439 Project\\test_log.txt" :
            System.getProperty("user.home") + "/test_log.txt";
        
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
