import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Cross-Platform USB Key Setup Utility
 * Works on Windows, Linux, and macOS
 */
public class USBSetup {
    private static final String IDENTIFIER_FILE = "usb_key.id";
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("=== USB Key Setup Utility (Cross-Platform) ===\n");
        System.out.println("This utility will configure your USB drive as an authorized access key.\n");
        
        String os = System.getProperty("os.name").toLowerCase();
        System.out.println("Detected OS: " + os + "\n");
        
        // Get USB mount points based on OS
        List<File> usbDrives = detectUSBDrives();
        
        if (usbDrives.isEmpty()) {
            System.out.println("❌ No USB drives detected!");
            System.out.println("\nTroubleshooting:");
            if (os.contains("linux")) {
                System.out.println("• USB drives are typically mounted at /media/$USER/ or /mnt/");
                System.out.println("• Check: ls /media/$USER/");
                System.out.println("• Or run: lsblk to see all drives");
            } else if (os.contains("mac")) {
                System.out.println("• USB drives are mounted at /Volumes/");
                System.out.println("• Check: ls /Volumes/");
            }
            return;
        }
        
        // List available drives
        System.out.println("Available USB drives:");
        for (int i = 0; i < usbDrives.size(); i++) {
            File drive = usbDrives.get(i);
            long totalSpace = drive.getTotalSpace();
            long freeSpace = drive.getFreeSpace();
            
            System.out.printf("%d. %s\n", i + 1, drive.getAbsolutePath());
            System.out.printf("   Total: %.2f GB, Free: %.2f GB\n", 
                totalSpace / (1024.0 * 1024.0 * 1024.0),
                freeSpace / (1024.0 * 1024.0 * 1024.0)
            );
        }
        
        System.out.print("\nSelect the USB drive number (or 0 to cancel): ");
        int selection = scanner.nextInt();
        
        if (selection <= 0 || selection > usbDrives.size()) {
            System.out.println("Setup cancelled.");
            return;
        }
        
        File selectedDrive = usbDrives.get(selection - 1);
        
        // Check if drive is writable
        if (!selectedDrive.canWrite()) {
            System.out.println("\n❌ ERROR: Selected drive is read-only!");
            System.out.println("\nPossible solutions:");
            if (os.contains("linux")) {
                System.out.println("• Remount with write permissions:");
                System.out.println("  sudo mount -o remount,rw " + selectedDrive.getAbsolutePath());
            } else if (os.contains("mac")) {
                System.out.println("• Check Disk Utility for write protection");
                System.out.println("• Or try: sudo mount -uw " + selectedDrive.getAbsolutePath());
            }
            return;
        }
        
        // Confirm selection
        System.out.println("\n⚠️  WARNING: This will mark " + selectedDrive.getAbsolutePath() + 
                           " as your authorized USB key.");
        System.out.print("Continue? (yes/no): ");
        scanner.nextLine(); // consume newline
        String confirm = scanner.nextLine();
        
        if (!confirm.equalsIgnoreCase("yes")) {
            System.out.println("Setup cancelled.");
            return;
        }
        
        // Create identifier file
        try {
            File identifierFile = new File(selectedDrive, IDENTIFIER_FILE);
            
            // Generate a unique identifier
            String uniqueId = java.util.UUID.randomUUID().toString();
            String content = "AUTHORIZED_USB_KEY\n" +
                           "Generated: " + new java.util.Date() + "\n" +
                           "ID: " + uniqueId + "\n" +
                           "OS: " + os + "\n" +
                           "\n" +
                           "DO NOT DELETE THIS FILE\n" +
                           "This file is required for USB-based access control.";
            
            Files.write(identifierFile.toPath(), content.getBytes());
            
            // Make it hidden (try on all platforms)
            try {
                if (os.contains("windows")) {
                    Runtime.getRuntime().exec("attrib +H \"" + identifierFile.getAbsolutePath() + "\"");
                } else {
                    // On Unix, files starting with . are hidden, but we keep the current name
                    // Just set hidden attribute if supported
                    Files.setAttribute(identifierFile.toPath(), "dos:hidden", true);
                }
            } catch (Exception e) {
                // Hidden attribute might not be supported, that's OK
            }
            
            System.out.println("\n✓ SUCCESS!");
            System.out.println("USB drive configured successfully at: " + selectedDrive.getAbsolutePath());
            System.out.println("Identifier file created: " + identifierFile.getAbsolutePath());
            System.out.println("\n⚠️  Keep this USB drive safe - it's now your access key!");
            
            // Verify the file was created
            if (identifierFile.exists()) {
                System.out.println("\n✓ Verified: File exists and is readable");
                String readBack = new String(Files.readAllBytes(identifierFile.toPath()));
                if (readBack.contains("AUTHORIZED_USB_KEY")) {
                    System.out.println("✓ Verified: File content is correct");
                }
            }
            
        } catch (IOException e) {
            System.err.println("\n❌ ERROR: Failed to create identifier file");
            System.err.println("Details: " + e.getMessage());
            e.printStackTrace();
            
            System.out.println("\nPossible solutions:");
            System.out.println("• Make sure the USB drive is writable");
            System.out.println("• Try running with sudo (Linux/Mac)");
            System.out.println("• Check disk permissions");
        }
        
        scanner.close();
    }
    
    /**
     * Detect USB drives based on the operating system
     */
    private static List<File> detectUSBDrives() {
        List<File> usbDrives = new ArrayList<>();
        String os = System.getProperty("os.name").toLowerCase();
        
        if (os.contains("windows")) {
            // Windows: Check drive letters
            File[] roots = File.listRoots();
            for (File root : roots) {
                // Skip C: and D: (typically system drives)
                if (!root.getAbsolutePath().startsWith("C:") && 
                    !root.getAbsolutePath().startsWith("D:") &&
                    root.getTotalSpace() > 0) {
                    usbDrives.add(root);
                }
            }
        } else if (os.contains("linux")) {
            // Linux: Check /media/$USER/ and /mnt/
            String username = System.getProperty("user.name");
            
            // Check /media/$USER/
            File mediaDir = new File("/media/" + username);
            if (mediaDir.exists() && mediaDir.isDirectory()) {
                File[] drives = mediaDir.listFiles(File::isDirectory);
                if (drives != null) {
                    for (File drive : drives) {
                        if (drive.canRead() && drive.getTotalSpace() > 0) {
                            usbDrives.add(drive);
                        }
                    }
                }
            }
            
            // Check /mnt/
            File mntDir = new File("/mnt");
            if (mntDir.exists() && mntDir.isDirectory()) {
                File[] drives = mntDir.listFiles(File::isDirectory);
                if (drives != null) {
                    for (File drive : drives) {
                        if (drive.canRead() && drive.getTotalSpace() > 0 && 
                            !drive.getName().startsWith(".")) {
                            usbDrives.add(drive);
                        }
                    }
                }
            }
            
            // Also check /run/media/$USER/ (Fedora/RHEL)
            File runMediaDir = new File("/run/media/" + username);
            if (runMediaDir.exists() && runMediaDir.isDirectory()) {
                File[] drives = runMediaDir.listFiles(File::isDirectory);
                if (drives != null) {
                    for (File drive : drives) {
                        if (drive.canRead() && drive.getTotalSpace() > 0) {
                            usbDrives.add(drive);
                        }
                    }
                }
            }
            
        } else if (os.contains("mac")) {
            // macOS: Check /Volumes/
            File volumesDir = new File("/Volumes");
            if (volumesDir.exists() && volumesDir.isDirectory()) {
                File[] volumes = volumesDir.listFiles(File::isDirectory);
                if (volumes != null) {
                    for (File volume : volumes) {
                        // Skip Macintosh HD and other system volumes
                        if (!volume.getName().contains("Macintosh") && 
                            volume.canRead() && 
                            volume.getTotalSpace() > 0) {
                            usbDrives.add(volume);
                        }
                    }
                }
            }
        }
        
        return usbDrives;
    }
}
