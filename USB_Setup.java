import java.io.*;
import java.nio.file.*;
import java.util.Scanner;

/**
 * USB Key Setup Utility
 * Run this once to set up your USB drive as an authorized key
 */
public class USBSetup {
    private static final String IDENTIFIER_FILE = "usb_key.id";
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("=== USB Key Setup Utility ===\n");
        System.out.println("This utility will configure your USB drive as an authorized access key.\n");
        
        // List available drives
        System.out.println("Available drives:");
        File[] roots = File.listRoots();
        for (int i = 0; i < roots.length; i++) {
            File root = roots[i];
            long totalSpace = root.getTotalSpace();
            long freeSpace = root.getFreeSpace();
            
            System.out.printf("%d. %s (Total: %.2f GB, Free: %.2f GB)\n", 
                i + 1, 
                root.getAbsolutePath(),
                totalSpace / (1024.0 * 1024.0 * 1024.0),
                freeSpace / (1024.0 * 1024.0 * 1024.0)
            );
        }
        
        System.out.print("\nSelect the USB drive number (or 0 to cancel): ");
        int selection = scanner.nextInt();
        
        if (selection <= 0 || selection > roots.length) {
            System.out.println("Setup cancelled.");
            return;
        }
        
        File selectedDrive = roots[selection - 1];
        
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
                           "\n" +
                           "DO NOT DELETE THIS FILE\n" +
                           "This file is required for USB-based access control.";
            
            Files.write(identifierFile.toPath(), content.getBytes());
            
            // Make it hidden (Windows)
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                Runtime.getRuntime().exec("attrib +H \"" + identifierFile.getAbsolutePath() + "\"");
            }
            
            System.out.println("\n✓ SUCCESS!");
            System.out.println("USB drive configured successfully at: " + selectedDrive.getAbsolutePath());
            System.out.println("Identifier file created: " + identifierFile.getAbsolutePath());
            System.out.println("\n⚠️  Keep this USB drive safe - it's now your access key!");
            
        } catch (IOException e) {
            System.err.println("ERROR: Failed to create identifier file");
            e.printStackTrace();
        }
        
        scanner.close();
    }
}
