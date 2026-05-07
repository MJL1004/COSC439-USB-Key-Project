# USB Flash Drive-Based Access Control System

## 📋 Overview

This project uses a **USB Flash Drive-Based Access Control Device Driver** that enhances data security by using a USB flash drive as a physical key to protect your files and apps. The system unlocks specific files and applications when the designated drive is inserted and automatically locks them when removed.


---

## 🎯 Features

Real USB Detection - Detects actual USB drives  
USB Authentication - Only your specific USB drive can unlock the system  
File Protection - Locks/unlocks folders  
Application Control - Terminates and blocks specific applications when usb is removed 
Event Logging - Complete audit trail of all access events  
Automatic Shutdown - Automatically locks everything when program exits  


## 🚀 Quick Start Guide

### Step 1: Compile the Java Files

```bash
javac USBSetup.java
javac USBKeyDriver.java
```

### Step 2: Configure Your USB Drive

1. Insert your USB flash drive
2. Run the setup:
   ```bash
   java USBSetup
   ```
3. Select your USB drive from the list
4. Confirm the selection
5. The process will create a hidden `usb_key.id` file on your USB
6. This will make the USB that you used the key for unlocking/locking your files


### Step 3: Create Protected Folder

Create the folder for your protected files:
```bash
mkdir "C:\JAVA CODE VSC\COSC439 Project\ProtectedFiles"
```

Put any files you want to protect in this folder.

### Step 4: Run the Driver

```bash
java USBKeyDriver
```

### Step 5: Test the System

1. With the driver running, remove your USB drive
   - Files should lock
   - Protected apps should terminate
   
2. Insert your USB drive
   - Files should unlock
   - Apps can now run

---

### Change Protected Folder Location

Edit the `PROTECTED_FOLDER` constant in the Java files:

```java
private static final String PROTECTED_FOLDER = "C:\\Your\\Path\\Here";
```

### Add Protected Applications

Edit the `PROTECTED_APPS` array in `USBKeyDriverIntegrated.java`:

```java
private static final String[] PROTECTED_APPS = {
    "notepad.exe",
    "chrome.exe",
    "excel.exe",
    // Add more applications here
};
```

### Adjust Check Interval

Change how often the system checks for USB (in milliseconds):

```java
private static final int CHECK_INTERVAL_MS = 2000;  // 2 seconds
```

---

## 🔧 How It Works

### USB Detection

1. System continuously scans all drive letters (E:, F:, G:, etc.)
2. Looks for the special `usb_key.id` file
3. Verifies the file contains the "AUTHORIZED_USB_KEY" marker
4. Only grants access if verification passes

### Application Locking

**When USB is removed:**
- Terminates all running instances of protected applications
- Prevents new instances from starting

**When USB is inserted:**
- Allows protected applications to run normally

---

## 📊 Event Logging

All events are logged to `access_log.txt` with timestamps:

```
[2024-01-15 14:30:22] USB Key Driver Started
[2024-01-15 14:30:45] USB KEY INSERTED at E:\
[2024-01-15 14:30:45] USB key verified successfully
[2024-01-15 14:30:45] Files unlocked successfully
[2024-01-15 14:30:45] Applications unlocked
[2024-01-15 14:35:12] USB KEY REMOVED
[2024-01-15 14:35:12] Files locked successfully
[2024-01-15 14:35:12] Applications locked
```

---



## 🚨 Emergency Access

If you lose access to your files:

### Method 1: Reset Permissions Manually
```bash
# Right-click folder → Properties → Security
# Click "Advanced" → Change owner to yourself
# Check "Replace owner on subcontainers and objects"
# Grant yourself Full Control
```

### Method 2: Command Line
```bash
# Run as Administrator
takeown /f "C:\JAVA CODE VSC\COSC439 Project\ProtectedFiles" /r /d y
icacls "C:\JAVA CODE VSC\COSC439 Project\ProtectedFiles" /grant %USERNAME%:F /t
```
---

## 📚 Technical Details

### Dependencies
- Java 8 or higher
- Windows 10/11 (for icacls and taskkill commands)
- Administrator privileges (recommended)

### File Formats
- `.id` files: Plain text USB identifier files
- `.txt` logs: Plain text event logs


---
## 🎓 Project Information

- **Platform:** Windows, Mac/Linux
- **Language:** Java

---

## 📞 Support

If you encounter issues:

1. Check the `access_log.txt` file for error messages
2. Verify you're running as Administrator
3. Ensure the protected folder exists
4. Test with a simple file first
5. Review the troubleshooting section above

