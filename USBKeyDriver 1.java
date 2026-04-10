import java.io.File;

public class USBKeyDriver {

    // Updated paths based on your folder
    private static final String USB_PATH = "C:\\JAVA CODE VSC\\COSC439 Project\\FakeUSB";
    private static final String PROTECTED_FOLDER = "C:\\JAVA CODE VSC\\COSC439 Project\\ProtectedFiles";

    public static void main(String[] args) {
        boolean previousState = false;

        System.out.println("USB Key Driver Running...");

        while (true) {
            boolean currentState = isUSBConnected();

            if (currentState && !previousState) {
                unlockFiles();
            } else if (!currentState && previousState) {
                lockFiles();
            }

            previousState = currentState;

            try {
                Thread.sleep(2000); // check every 2 seconds
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // Check if "USB" exists
    private static boolean isUSBConnected() {
        File usb = new File(USB_PATH);
        return usb.exists();
    }

    // Lock files
    private static void lockFiles() {
        try {
            System.out.println("Locking files...");
            Process process = Runtime.getRuntime().exec(
                "icacls \"" + PROTECTED_FOLDER + "\" /deny Everyone:(R)"
            );
            process.waitFor();
            System.out.println("Files Locked.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Unlock files
    private static void unlockFiles() {
        try {
            System.out.println("Unlocking files...");
            Process process = Runtime.getRuntime().exec(
                "icacls \"" + PROTECTED_FOLDER + "\" /grant Everyone:(R)"
            );
            process.waitFor();
            System.out.println("Files Unlocked.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}