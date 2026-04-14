import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.text.SimpleDateFormat;

/**
 * Enhanced USB Key Driver with File and Application Locking
 * Integrates both file protection and application blocking
 */
public class USBKeyDriverIntegrated {
    // Configuration
    private static final String USB_IDENTIFIER_FILE = "usb_key.id";
    private static final String PROTECTED_FOLDER = "C:\\JAVA CODE VSC\\COSC439 Project\\ProtectedFiles";
    private static final String LOG_FILE = "C:\\JAVA CODE VSC\\COSC439 Project\\access_log.txt";
    private static final int CHECK_INTERVAL_MS = 2000;
    
    // Protected applications
    private static final String[] PROTECTED_APPS = {
        "notepad.exe",
        // Add more applications here
    };
    
    // State tracking
    private static boolean previousState = false;
    private static String currentUSBPath = null;
    private static PrintWriter logWriter;
    
    public static void main(String[] args) {
        try {
            initializeLogger();
            logEvent("=== USB Key Driver Started ===");
            displayStartupInfo();
            
            // Add shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\n\nShutting down safely...");
                logEvent("USB Key Driver Shutting Down");
                // Lock everything on shutdown for security
                lockFiles();
                lockApplications();
                if (logWriter != null) {
                    logWriter.close();
                }
            }));
            
            // Initial lock state
            lockFiles();
            lockApplications();
            
            // Main monitoring loop
            while (true) {
                boolean currentState = isUSBConnected();
                
                if (currentState && !previousState) {
                    onUSBInserted();
                } else if (!currentState && previousState) {
                    onUSBRemoved();
                }
                
                previousState = currentState;
                
                try {
                    Thread.sleep(CHECK_INTERVAL_MS);
                } catch (InterruptedException e) {
                    logEvent("Thread interrupted: " + e.getMessage());
                    break;
                }
            }
        } catch (Exception e) {
            logEvent("CRITICAL ERROR: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (logWriter != null) {
                logWriter.close();
            }
        }
    }
    
    private static void displayStartupInfo() {
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║        USB KEY DRIVER - ACCESS CONTROL SYSTEM              ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("📋 Configuration:");
        System.out.println("   • USB Identifier: " + USB_IDENTIFIER_FILE);
        System.out.println("   • Protected Folder: " + PROTECTED_FOLDER);
        System.out.println("   • Protected Apps: " + Arrays.toString(PROTECTED_APPS));
        System.out.println("   • Check Interval: " + CHECK_INTERVAL_MS + "ms");
        System.out.println("   • Log File: " + LOG_FILE);
        System.out.println();
        System.out.println("🔍 Status: Monitoring for USB key...");
        System.out.println("⚠️  Press Ctrl+C to stop (will lock everything on exit)");
        System.out.println();
        System.out.println("───────────────────────────────────────────────────────────");
    }
    
    private static boolean isUSBConnected() {
        File[] roots = File.listRoots();
        
        for (File root : roots) {
            // Skip system drives
            if (root.getAbsolutePath().startsWith("C:") || 
                root.getAbsolutePath().startsWith("D:")) {
                continue;
            }
            
            File identifierFile = new File(root, USB_IDENTIFIER_FILE);
            if (identifierFile.exists()) {
                currentUSBPath = root.getAbsolutePath();
                return true;
            }
        }
        
        currentUSBPath = null;
        return false;
    }
    
    private static void onUSBInserted() {
        System.out.println("\n🔑 USB KEY DETECTED at " + currentUSBPath);
        logEvent("USB KEY INSERTED at " + currentUSBPath);
        
        if (verifyUSBKey()) {
            System.out.println("✓ USB key verified - granting access");
            unlockFiles();
            unlockApplications();
        } else {
            System.out.println("❌ USB verification FAILED - access denied");
            logEvent("USB verification failed - unauthorized USB drive");
        }
        
        System.out.println("───────────────────────────────────────────────────────────");
    }
    
    private static void onUSBRemoved() {
        System.out.println("\n⚠️  USB KEY REMOVED - Securing system");
        logEvent("USB KEY REMOVED");
        
        lockFiles();
        lockApplications();
        currentUSBPath = null;
        
        System.out.println("───────────────────────────────────────────────────────────");
    }
    
    private static boolean verifyUSBKey() {
        try {
            File identifierFile = new File(currentUSBPath, USB_IDENTIFIER_FILE);
            if (!identifierFile.exists()) {
                return false;
            }
            
            String content = new String(Files.readAllBytes(identifierFile.toPath())).trim();
            
            if (content.contains("AUTHORIZED_USB_KEY")) {
                logEvent("USB key verified successfully");
                return true;
            }
            
            return false;
        } catch (IOException e) {
            logEvent("USB verification error: " + e.getMessage());
            return false;
        }
    }
    
    // ==================== FILE LOCKING ====================
    
    private static void lockFiles() {
        try {
            System.out.println("   🔒 Locking files...");
            
            File protectedFolder = new File(PROTECTED_FOLDER);
            if (!protectedFolder.exists()) {
                System.out.println("   ⚠️  Protected folder not found - creating it");
                protectedFolder.mkdirs();
            }
            
            String[] commands = {
                "icacls \"" + PROTECTED_FOLDER + "\" /inheritance:r",
                "icacls \"" + PROTECTED_FOLDER + "\" /deny %USERNAME%:(R,W,X)",
                "icacls \"" + PROTECTED_FOLDER + "\\*\" /deny %USERNAME%:(R,W,X)"
            };
            
            for (String command : commands) {
                executeCommand(command);
            }
            
            System.out.println("   ✓ Files locked");
            logEvent("Files locked successfully");
            
        } catch (Exception e) {
            System.err.println("   ❌ Error locking files: " + e.getMessage());
            logEvent("Lock error: " + e.getMessage());
        }
    }
    
    private static void unlockFiles() {
        try {
            System.out.println("   🔓 Unlocking files...");
            
            File protectedFolder = new File(PROTECTED_FOLDER);
            if (!protectedFolder.exists()) {
                System.out.println("   ⚠️  Protected folder not found");
                return;
            }
            
            String[] commands = {
                "icacls \"" + PROTECTED_FOLDER + "\" /grant %USERNAME%:(OI)(CI)F",
                "icacls \"" + PROTECTED_FOLDER + "\\*\" /grant %USERNAME%:F"
            };
            
            for (String command : commands) {
                executeCommand(command);
            }
            
            System.out.println("   ✓ Files unlocked");
            logEvent("Files unlocked successfully");
            
        } catch (Exception e) {
            System.err.println("   ❌ Error unlocking files: " + e.getMessage());
            logEvent("Unlock error: " + e.getMessage());
        }
    }
    
    // ==================== APPLICATION LOCKING ====================
    
    private static void lockApplications() {
        try {
            System.out.println("   🔒 Locking applications...");
            
            // Terminate protected applications
            for (String app : PROTECTED_APPS) {
                if (isApplicationRunning(app)) {
                    terminateProcess(app);
                    System.out.println("      • Terminated: " + app);
                }
            }
            
            System.out.println("   ✓ Applications locked");
            logEvent("Applications locked");
            
        } catch (Exception e) {
            System.err.println("   ❌ Error locking applications: " + e.getMessage());
            logEvent("Application lock error: " + e.getMessage());
        }
    }
    
    private static void unlockApplications() {
        try {
            System.out.println("   🔓 Unlocking applications...");
            System.out.println("   ✓ Applications unlocked (users can now run protected apps)");
            logEvent("Applications unlocked");
            
        } catch (Exception e) {
            System.err.println("   ❌ Error unlocking applications: " + e.getMessage());
        }
    }
    
    private static boolean isApplicationRunning(String processName) {
        try {
            Process process = Runtime.getRuntime().exec("tasklist /FI \"IMAGENAME eq " + processName + "\"");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.toLowerCase().contains(processName.toLowerCase())) {
                    reader.close();
                    return true;
                }
            }
            
            reader.close();
            return false;
            
        } catch (IOException e) {
            return false;
        }
    }
    
    private static void terminateProcess(String processName) {
        try {
            Runtime.getRuntime().exec("taskkill /F /IM " + processName);
        } catch (IOException e) {
            // Process might not be running
        }
    }
    
    // ==================== UTILITIES ====================
    
    private static void executeCommand(String command) throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec("cmd.exe /c " + command);
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        
        String line;
        while ((line = reader.readLine()) != null) {
            // Consume output silently
        }
        
        while ((line = errorReader.readLine()) != null) {
            // Log errors if needed
        }
        
        process.waitFor();
        reader.close();
        errorReader.close();
    }
    
    private static void initializeLogger() {
        try {
            logWriter = new PrintWriter(new FileWriter(LOG_FILE, true), true);
        } catch (IOException e) {
            System.err.println("WARNING: Could not initialize log file: " + e.getMessage());
        }
    }
    
    private static void logEvent(String message) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String logMessage = "[" + timestamp + "] " + message;
        
        if (logWriter != null) {
            logWriter.println(logMessage);
            logWriter.flush();
        }
    }
}
