import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.text.SimpleDateFormat;

public class USBKeyDriver {
    // Configuration
    private static final String USB_IDENTIFIER_FILE = "usb_key.id"; // Unique file to identify YOUR specific USB
    private static final String PROTECTED_FOLDER = "C:\\JAVA CODE VSC\\COSC439 Project\\ProtectedFiles";
    private static final String LOG_FILE = "C:\\JAVA CODE VSC\\COSC439 Project\\access_log.txt";
    private static final int CHECK_INTERVAL_MS = 2000;
    
    // State tracking
    private static boolean previousState = false;
    private static String currentUSBPath = null;
    private static PrintWriter logWriter;
    
    public static void main(String[] args) {
        try {
            initializeLogger();
            logEvent("USB Key Driver Started");
            System.out.println("=== USB Key Driver Running ===");
            System.out.println("Monitoring for USB drive with identifier: " + USB_IDENTIFIER_FILE);
            System.out.println("Protected folder: " + PROTECTED_FOLDER);
            System.out.println("Press Ctrl+C to stop\n");
            
            // Add shutdown hook for graceful cleanup
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logEvent("USB Key Driver Shutting Down");
                if (logWriter != null) {
                    logWriter.close();
                }
            }));
            
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
    
    /**
     * Enhanced USB detection that looks for a specific identifier file
     * This ensures ONLY your designated USB drive can unlock the system
     */
    private static boolean isUSBConnected() {
        File[] roots = File.listRoots();
        
        for (File root : roots) {
            // Skip system drives (C:, D: if it's local, etc.)
            if (root.getAbsolutePath().startsWith("C:") || 
                root.getAbsolutePath().startsWith("D:")) {
                continue;
            }
            
            // Check if this drive has our identifier file
            File identifierFile = new File(root, USB_IDENTIFIER_FILE);
            if (identifierFile.exists()) {
                currentUSBPath = root.getAbsolutePath();
                return true;
            }
        }
        
        currentUSBPath = null;
        return false;
    }
    
    /**
     * Called when USB drive is inserted
     */
    private static void onUSBInserted() {
        String message = "USB KEY INSERTED at " + currentUSBPath;
        System.out.println("\n" + message);
        logEvent(message);
        
        if (verifyUSBKey()) {
            unlockFiles();
            // Future enhancement: unlock applications here
        } else {
            System.out.println("WARNING: USB verification failed!");
            logEvent("USB verification failed - access denied");
        }
    }
    
    /**
     * Called when USB drive is removed
     */
    private static void onUSBRemoved() {
        String message = "USB KEY REMOVED";
        System.out.println("\n" + message);
        logEvent(message);
        
        lockFiles();
        currentUSBPath = null;
        // Future enhancement: lock applications here
    }
    
    /**
     * Verify the USB key is authentic by checking the identifier file content
     */
    private static boolean verifyUSBKey() {
        try {
            File identifierFile = new File(currentUSBPath, USB_IDENTIFIER_FILE);
            if (!identifierFile.exists()) {
                return false;
            }
            
            // Read the identifier file content for verification
            String content = new String(Files.readAllBytes(identifierFile.toPath())).trim();
            
            // Simple verification - you can enhance this with encryption/hashing
            // For now, just check if it contains a specific passphrase
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
    
    /**
     * Lock the protected files using icacls
     * Enhanced with better error handling and output capture
     */
    private static void lockFiles() {
        try {
            System.out.println("🔒 Locking files...");
            
            File protectedFolder = new File(PROTECTED_FOLDER);
            if (!protectedFolder.exists()) {
                System.out.println("WARNING: Protected folder does not exist!");
                logEvent("Lock failed - protected folder not found");
                return;
            }
            
            // Remove all permissions first, then deny
            String[] commands = {
                "icacls \"" + PROTECTED_FOLDER + "\" /inheritance:r",  // Remove inherited permissions
                "icacls \"" + PROTECTED_FOLDER + "\" /deny %USERNAME%:(R,W,X)",  // Deny current user
                "icacls \"" + PROTECTED_FOLDER + "\\*\" /deny %USERNAME%:(R,W,X)"  // Deny for all files inside
            };
            
            for (String command : commands) {
                executeCommand(command);
            }
            
            System.out.println("✓ Files LOCKED");
            logEvent("Files locked successfully");
            
        } catch (Exception e) {
            System.err.println("ERROR locking files: " + e.getMessage());
            logEvent("Lock error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Unlock the protected files using icacls
     * Enhanced with better error handling and output capture
     */
    private static void unlockFiles() {
        try {
            System.out.println("🔓 Unlocking files...");
            
            File protectedFolder = new File(PROTECTED_FOLDER);
            if (!protectedFolder.exists()) {
                System.out.println("WARNING: Protected folder does not exist!");
                logEvent("Unlock failed - protected folder not found");
                return;
            }
            
            // Grant full permissions
            String[] commands = {
                "icacls \"" + PROTECTED_FOLDER + "\" /grant %USERNAME%:(OI)(CI)F",  // Grant full control
                "icacls \"" + PROTECTED_FOLDER + "\\*\" /grant %USERNAME%:F"  // Grant for all files inside
            };
            
            for (String command : commands) {
                executeCommand(command);
            }
            
            System.out.println("✓ Files UNLOCKED");
            logEvent("Files unlocked successfully");
            
        } catch (Exception e) {
            System.err.println("ERROR unlocking files: " + e.getMessage());
            logEvent("Unlock error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Execute a command and properly handle process streams
     */
    private static void executeCommand(String command) throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec("cmd.exe /c " + command);
        
        // Consume the output streams to prevent blocking
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        
        String line;
        while ((line = reader.readLine()) != null) {
            // Optionally log command output
        }
        
        while ((line = errorReader.readLine()) != null) {
            System.err.println("Command error: " + line);
        }
        
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            System.err.println("Command failed with exit code: " + exitCode);
        }
        
        reader.close();
        errorReader.close();
    }
    
    /**
     * Initialize the logging system
     */
    private static void initializeLogger() {
        try {
            logWriter = new PrintWriter(new FileWriter(LOG_FILE, true), true);
        } catch (IOException e) {
            System.err.println("WARNING: Could not initialize log file: " + e.getMessage());
        }
    }
    
    /**
     * Log an event with timestamp
     */
    private static void logEvent(String message) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String logMessage = "[" + timestamp + "] " + message;
        
        if (logWriter != null) {
            logWriter.println(logMessage);
            logWriter.flush();
        }
    }
}
