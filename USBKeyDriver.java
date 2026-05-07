import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.*;
import java.text.SimpleDateFormat;

/**
 * Cross-Platform USB Key Driver
 * Works on Windows, Linux, and macOS
 */
public class USBKeyDriver {
    // Configuration
    private static final String USB_IDENTIFIER_FILE = "usb_key.id";
    private static String PROTECTED_FOLDER;  // Set based on OS
    private static String LOG_FILE;
    private static final int CHECK_INTERVAL_MS = 2000;
    
    // OS Detection
    private static final String OS = System.getProperty("os.name").toLowerCase();
    private static final boolean IS_WINDOWS = OS.contains("windows");
    private static final boolean IS_LINUX = OS.contains("linux");
    private static final boolean IS_MAC = OS.contains("mac");
    
    // Protected applications (process names vary by OS)
    private static String[] PROTECTED_APPS;
    
    // State tracking
    private static boolean previousState = false;
    private static String currentUSBPath = null;
    private static PrintWriter logWriter;
    
    public static void main(String[] args) {
        try {
            // Initialize paths based on OS
            initializePaths();
            initializeProtectedApps();
            
            initializeLogger();
            logEvent("=== USB Key Driver Started (Cross-Platform) ===");
            logEvent("Operating System: " + OS);
            displayStartupInfo();
            
            // Add shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\n\nShutting down safely...");
                logEvent("USB Key Driver Shutting Down");
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
    
    private static void initializePaths() {
        String userHome = System.getProperty("user.home");
        
        if (IS_WINDOWS) {
            PROTECTED_FOLDER = "C:\\JAVA CODE VSC\\COSC439 Project\\ProtectedFiles";
            LOG_FILE = "C:\\JAVA CODE VSC\\COSC439 Project\\access_log.txt";
        } else {
            // Linux/Mac: Use user's home directory
            PROTECTED_FOLDER = userHome + "/USBProtectedFiles";
            LOG_FILE = userHome + "/usb_access_log.txt";
        }
    }
    
    private static void initializeProtectedApps() {
        if (IS_WINDOWS) {
            PROTECTED_APPS = new String[]{"notepad.exe", "calc.exe"};
        } else if (IS_LINUX) {
            PROTECTED_APPS = new String[]{"gedit", "gnome-calculator"};
        } else if (IS_MAC) {
            PROTECTED_APPS = new String[]{"TextEdit", "Calculator"};
        } else {
            PROTECTED_APPS = new String[]{};
        }
    }
    
    private static void displayStartupInfo() {
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║    USB KEY DRIVER - CROSS-PLATFORM ACCESS CONTROL          ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("📋 Configuration:");
        System.out.println("   • Operating System: " + OS);
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
        List<File> usbDrives = detectUSBDrives();
        
        for (File drive : usbDrives) {
            File identifierFile = new File(drive, USB_IDENTIFIER_FILE);
            if (identifierFile.exists()) {
                currentUSBPath = drive.getAbsolutePath();
                return true;
            }
        }
        
        currentUSBPath = null;
        return false;
    }
    
    /**
     * Detect USB drives based on the operating system
     */
    private static List<File> detectUSBDrives() {
        List<File> usbDrives = new ArrayList<>();
        
        if (IS_WINDOWS) {
            File[] roots = File.listRoots();
            for (File root : roots) {
                if (!root.getAbsolutePath().startsWith("C:") && 
                    !root.getAbsolutePath().startsWith("D:") &&
                    root.getTotalSpace() > 0) {
                    usbDrives.add(root);
                }
            }
        } else if (IS_LINUX) {
            String username = System.getProperty("user.name");
            
            // Check /media/$USER/
            addDrivesFromDirectory(usbDrives, "/media/" + username);
            // Check /mnt/
            addDrivesFromDirectory(usbDrives, "/mnt");
            // Check /run/media/$USER/ (Fedora/RHEL)
            addDrivesFromDirectory(usbDrives, "/run/media/" + username);
            
        } else if (IS_MAC) {
            addDrivesFromDirectory(usbDrives, "/Volumes", true);
        }
        
        return usbDrives;
    }
    
    private static void addDrivesFromDirectory(List<File> usbDrives, String dirPath) {
        addDrivesFromDirectory(usbDrives, dirPath, false);
    }
    
    private static void addDrivesFromDirectory(List<File> usbDrives, String dirPath, boolean skipMacHD) {
        File dir = new File(dirPath);
        if (dir.exists() && dir.isDirectory()) {
            File[] drives = dir.listFiles(File::isDirectory);
            if (drives != null) {
                for (File drive : drives) {
                    if (skipMacHD && drive.getName().contains("Macintosh")) {
                        continue;
                    }
                    if (drive.canRead() && drive.getTotalSpace() > 0 && 
                        !drive.getName().startsWith(".")) {
                        usbDrives.add(drive);
                    }
                }
            }
        }
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
            
            if (IS_WINDOWS) {
                lockFilesWindows();
            } else {
                lockFilesUnix();
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
            
            if (IS_WINDOWS) {
                unlockFilesWindows();
            } else {
                unlockFilesUnix();
            }
            
            System.out.println("   ✓ Files unlocked");
            logEvent("Files unlocked successfully");
            
        } catch (Exception e) {
            System.err.println("   ❌ Error unlocking files: " + e.getMessage());
            logEvent("Unlock error: " + e.getMessage());
        }
    }
    
    private static void lockFilesWindows() throws IOException, InterruptedException {
        String[] commands = {
            "icacls \"" + PROTECTED_FOLDER + "\" /inheritance:r",
            "icacls \"" + PROTECTED_FOLDER + "\" /deny %USERNAME%:(R,W,X)",
            "icacls \"" + PROTECTED_FOLDER + "\\*\" /deny %USERNAME%:(R,W,X)"
        };
        
        for (String command : commands) {
            executeCommand(command);
        }
    }
    
    private static void unlockFilesWindows() throws IOException, InterruptedException {
        String[] commands = {
            "icacls \"" + PROTECTED_FOLDER + "\" /grant %USERNAME%:(OI)(CI)F",
            "icacls \"" + PROTECTED_FOLDER + "\\*\" /grant %USERNAME%:F"
        };
        
        for (String command : commands) {
            executeCommand(command);
        }
    }
    
    private static void lockFilesUnix() throws IOException {
        // Use Java NIO file permissions
        Path folderPath = Paths.get(PROTECTED_FOLDER);
        
        // Remove all permissions (000)
        Set<PosixFilePermission> noPerms = new HashSet<>();
        Files.setPosixFilePermissions(folderPath, noPerms);
        
        // Lock all files inside
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(folderPath)) {
            for (Path file : stream) {
                try {
                    Files.setPosixFilePermissions(file, noPerms);
                } catch (IOException e) {
                    // Skip files we can't lock
                }
            }
        } catch (IOException e) {
            // Folder might be empty or already locked
        }
    }
    
    private static void unlockFilesUnix() throws IOException {
        // Grant read, write, execute permissions (700)
        Path folderPath = Paths.get(PROTECTED_FOLDER);
        
        Set<PosixFilePermission> fullPerms = new HashSet<>(Arrays.asList(
            PosixFilePermission.OWNER_READ,
            PosixFilePermission.OWNER_WRITE,
            PosixFilePermission.OWNER_EXECUTE
        ));
        
        Files.setPosixFilePermissions(folderPath, fullPerms);
        
        // Unlock all files inside
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(folderPath)) {
            for (Path file : stream) {
                try {
                    Set<PosixFilePermission> filePerms = new HashSet<>(Arrays.asList(
                        PosixFilePermission.OWNER_READ,
                        PosixFilePermission.OWNER_WRITE
                    ));
                    Files.setPosixFilePermissions(file, filePerms);
                } catch (IOException e) {
                    // Skip files we can't unlock
                }
            }
        }
    }
    
    // ==================== APPLICATION LOCKING ====================
    
    private static void lockApplications() {
        try {
            System.out.println("   🔒 Locking applications...");
            
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
        System.out.println("   🔓 Unlocking applications...");
        System.out.println("   ✓ Applications unlocked (users can now run protected apps)");
        logEvent("Applications unlocked");
    }
    
    private static boolean isApplicationRunning(String processName) {
        try {
            String command;
            if (IS_WINDOWS) {
                command = "tasklist /FI \"IMAGENAME eq " + processName + "\"";
            } else {
                command = "pgrep -f " + processName;
            }
            
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
            );
            
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.toLowerCase().contains(processName.toLowerCase()) ||
                    line.matches("\\d+")) { // pgrep returns PIDs
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
            String command;
            if (IS_WINDOWS) {
                command = "taskkill /F /IM " + processName;
            } else {
                command = "pkill -9 " + processName;
            }
            Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            // Process might not be running
        }
    }
    
    // ==================== UTILITIES ====================
    
    private static void executeCommand(String command) throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec("cmd.exe /c " + command);
        
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(process.getInputStream())
        );
        BufferedReader errorReader = new BufferedReader(
            new InputStreamReader(process.getErrorStream())
        );
        
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