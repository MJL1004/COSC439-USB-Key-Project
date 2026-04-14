import java.io.*;
import java.util.*;

/**
 * Application Locker - Blocks and unblocks specific applications
 * This component can be integrated into the USBKeyDriver
 */
public class ApplicationLocker {
    
    // List of applications to lock (add .exe names here)
    private static final String[] PROTECTED_APPS = {
        "notepad.exe",
        "chrome.exe",
        "firefox.exe",
        // Add more applications as needed
    };
    
    private static final String BLOCK_SCRIPT = "app_blocker.bat";
    private static final String UNBLOCK_SCRIPT = "app_unblocker.bat";
    
    /**
     * Lock applications - prevent them from running
     */
    public static void lockApplications() {
        try {
            System.out.println("🔒 Locking applications...");
            
            // Create a batch script to block applications using Windows Firewall
            // or Task Scheduler (more reliable than just killing processes)
            createBlockScript();
            executeScript(BLOCK_SCRIPT);
            
            // Also terminate any currently running instances
            for (String app : PROTECTED_APPS) {
                terminateProcess(app);
            }
            
            System.out.println("✓ Applications locked");
            
        } catch (Exception e) {
            System.err.println("ERROR locking applications: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Unlock applications - allow them to run
     */
    public static void unlockApplications() {
        try {
            System.out.println("🔓 Unlocking applications...");
            
            createUnblockScript();
            executeScript(UNBLOCK_SCRIPT);
            
            System.out.println("✓ Applications unlocked");
            
        } catch (Exception e) {
            System.err.println("ERROR unlocking applications: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Create a batch script to block applications using Windows Firewall rules
     */
    private static void createBlockScript() throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(BLOCK_SCRIPT))) {
            writer.println("@echo off");
            writer.println("echo Blocking protected applications...");
            
            for (String app : PROTECTED_APPS) {
                String appPath = findApplicationPath(app);
                if (appPath != null) {
                    // Create outbound firewall rule to block the application
                    writer.println("netsh advfirewall firewall add rule name=\"Block_" + app + 
                                 "\" dir=out program=\"" + appPath + "\" action=block");
                }
            }
            
            writer.println("echo Applications blocked.");
        }
    }
    
    /**
     * Create a batch script to unblock applications
     */
    private static void createUnblockScript() throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(UNBLOCK_SCRIPT))) {
            writer.println("@echo off");
            writer.println("echo Unblocking protected applications...");
            
            for (String app : PROTECTED_APPS) {
                // Remove firewall rule
                writer.println("netsh advfirewall firewall delete rule name=\"Block_" + app + "\"");
            }
            
            writer.println("echo Applications unblocked.");
        }
    }
    
    /**
     * Execute a batch script
     */
    private static void executeScript(String scriptName) throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec("cmd.exe /c " + scriptName);
        
        // Consume output
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            // Optionally log output
        }
        
        process.waitFor();
        reader.close();
    }
    
    /**
     * Terminate a running process by name
     */
    private static void terminateProcess(String processName) {
        try {
            Runtime.getRuntime().exec("taskkill /F /IM " + processName);
        } catch (IOException e) {
            // Process might not be running, which is fine
        }
    }
    
    /**
     * Find the full path of an application
     */
    private static String findApplicationPath(String appName) {
        // Common Windows application directories
        String[] searchPaths = {
            "C:\\Windows\\System32\\",
            "C:\\Program Files\\",
            "C:\\Program Files (x86)\\",
            System.getenv("LOCALAPPDATA") + "\\Programs\\"
        };
        
        for (String path : searchPaths) {
            File file = new File(path, appName);
            if (file.exists()) {
                return file.getAbsolutePath();
            }
            
            // Also search subdirectories (one level deep)
            File directory = new File(path);
            if (directory.exists() && directory.isDirectory()) {
                File[] subdirs = directory.listFiles(File::isDirectory);
                if (subdirs != null) {
                    for (File subdir : subdirs) {
                        File appFile = new File(subdir, appName);
                        if (appFile.exists()) {
                            return appFile.getAbsolutePath();
                        }
                    }
                }
            }
        }
        
        return null; // Not found
    }
    
    /**
     * Check if an application is currently running
     */
    public static boolean isApplicationRunning(String processName) {
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
    
    /**
     * Test method
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("=== Application Locker Test ===");
        System.out.println("1. Lock applications");
        System.out.println("2. Unlock applications");
        System.out.println("3. Check if app is running");
        System.out.print("Select option: ");
        
        int choice = scanner.nextInt();
        
        switch (choice) {
            case 1:
                lockApplications();
                break;
            case 2:
                unlockApplications();
                break;
            case 3:
                System.out.print("Enter process name (e.g., notepad.exe): ");
                scanner.nextLine(); // consume newline
                String processName = scanner.nextLine();
                boolean running = isApplicationRunning(processName);
                System.out.println(processName + " is " + (running ? "RUNNING" : "NOT RUNNING"));
                break;
        }
        
        scanner.close();
    }
}
