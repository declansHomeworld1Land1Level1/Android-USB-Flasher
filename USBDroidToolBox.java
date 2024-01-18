import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.io.*;
import java.math.*;
import java.net.*;
import java.security.*;
import java.sql.*;
import java.text.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.*;
import java.util.prefs.*;
import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.colorchooser.*;
import javax.swing.event.*;
import javax.swing.filechooser.*;
import javax.swing.groupLayout.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;
import javax.swing.plaf.metal.*;
import javax.swing.table.*;
import javax.swing.tree.*;
import javax.tools.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import javax.xml.validation.*;
import com.fasterxml.jackson.databind.*;
import com.formdev.flatlaf.*;
import com.hoho.usb.*;
import org.apache.commons.compress.*;
import org.apache.commons.compress.archivers.*;
import org.apache.commons.compress.archivers.sevenz.*;
import org.apache.commons.compress.archivers.tar.*;
import org.apache.commons.compress.compressors.*;
import org.apache.commons.compress.compressors.gzip.*;
import org.apache.commons.compress.compressors.xz.*;
import org.apache.commons.compress.utils.*;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.*;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.*;
import org.apache.poi.sl.usermodel.*;
import org.apache.xmlbeans.*;
import org.jdesktop.layout.*;
import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.OngoingStubbing;
import org.w3c.dom.*;
import sun.audio.*;

@SuppressWarnings({"unused", "unchecked", "rawtypes"})
public class USBDroidToolBox {

    /////////////////////////
    /// CLASSES AND OBJECTS ///
    /////////////////////////

    private enum Commands {
        FLASH, RESET, REBOOT, SHUTDOWN, SLEEP, INFO, HELP, EXIT, UNKNOWN
    }

    private class ClipBoardContents implements Transferable {
        private String string;

        ClipBoardContents(String s) {
            string = s;
        }

        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{DataFlavor.stringFlavor};
        }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return flavor.equals(DataFlavor.stringFlavor);
        }

        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (isDataFlavorSupported(flavor)) {
                return string;
            } else {
                throw new UnsupportedFlavorException(flavor);
            }
        }
    }

    private class CommandLineParser {
        ArrayList commandList;

        CommandLineParser() {
            commandList = new ArrayList();
        }

        public void Parse(String line) {
            Scanner scnr = new Scanner(line);
            while (scnr.hasNext()) {
                commandList.add(scnr.next());
            }
        }

        public Commands getCommand() {
            switch (((String) commandList.get(0)).toUpperCase()) {
                case "FLASH":
                    return Commands.FLASH;
                case "RESET":
                    return Commands.RESET;
                case "REBOOT":
                    return Commands.REBOOT;
                case "SHUTDOWN":
                    return Commands.SHUTDOWN;
                case "SLEEP":
                    return Commands.SLEEP;
                case "INFO":
                    return Commands.INFO;
                case "HELP":
                    return Commands.HELP;
                case "EXIT":
                    return Commands.EXIT;
                default:
                    return Commands.UNKNOWN;
            }
        }

        public String getArg(int index) {
            return (String) commandList.get(index);
        }

        public int length() {
            return commandList.size();
        }
    }

    ////////////////
    /// VARIABLES ///
    ////////////////

    private CommandLineParser parser = new CommandLineParser();

    private Font font = new Font("Monospaced", Font.PLAIN, 12);

    private Color colorBackground = new Color(204, 204, 204);
    private Color colorForeground = new Color(0, 0, 0);

    private JSplitPane splitter;
    private JEditorPane editorOutput;
    private JEditorPane editorInput;
    private JScrollPane scrollPaneLeft;
    private JScrollPane scrollPaneRight;

    private JButton buttonSend;

    private Console console;

    //////////////////
    /// CONSTRUCTORS ///
    //////////////////

    public USBDroidToolBox() {
        console = new Console();
        console.redirectSystemStreams();
        console.setOut(new PrintStream(new StreamPrinter(editorOutput)));

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] fontNames = ge.getAvailableFontFamilyNames();
        Vector fontVector = new Vector();
        for (String fontName : fontNames) {
            fontVector.addElement(fontName);
        }

        JFrame frame = new JFrame("USBDroidToolBox - Version 1.0");

        GroupLayout groupLayout = new GroupLayout(frame.getContentPane());
        frame.getContentPane().setLayout(groupLayout);

        groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGap(0, 800, Short.MAX_VALUE)
                .addGroup(groupLayout.createSequentialGroup().addContainerGap().addComponent(splitter, GroupLayout.DEFAULT_SIZE, 784, Short.MAX_VALUE).addContainerGap())
                .addGroup(groupLayout.createSequentialGroup().addContainerGap().addComponent(buttonSend, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE).addGap(18, 18, 18).addComponent(scrollPaneLeft, GroupLayout.DEFAULT_SIZE, 784, Short.MAX_VALUE).addContainerGap())
        );
        groupLayout.setVerticalGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGap(0, 571, Short.MAX_VALUE)
                .addGroup(groupLayout.createSequentialGroup().addContainerGap().addComponent(buttonSend, GroupLayout.PREFERRED_SIZE, 31, GroupLayout.PREFERRED_SIZE).addGap(18, 18, 18).addComponent(scrollPaneLeft, GroupLayout.DEFAULT_SIZE, 517, Short.MAX_VALUE).addContainerGap())
        );

        scrollPaneLeft = new JScrollPane();
        scrollPaneLeft.setViewportView(editorInput);
        scrollPaneLeft.setRowHeaderView(new RowNumberColumn(editorInput));

        scrollPaneRight = new JScrollPane();
        scrollPaneRight.setViewportView(editorOutput);

        editorInput = new JEditorPane();
        editorInput.setFont(font);
        editorInput.setForeground(colorForeground);
        editorInput.setBackground(colorBackground);
        editorInput.setEditable(false);

        editorOutput = new JEditorPane();
        editorOutput.setFont(font);
        editorOutput.setForeground(colorForeground);
        editorOutput.setBackground(colorBackground);
        editorOutput.setEditable(false);

        buttonSend = new JButton("Send!");
        buttonSend.addActionListener(new SendButtonAction());

        splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollPaneLeft, scrollPaneRight);
        splitter.setDividerLocation(250);
        splitter.setOneTouchExpandable(true);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setVisible(true);

        /* Start listening to incoming connections */
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(ConsoleServer.PORT);
            while (true) {
                new ClientHandler(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            System.out.println("Listen socket creation failed: ");
            System.out.println(e.getMessage());
            System.exit(-1);
        } finally {
            try {
                serverSocket.close();
            } catch (IOException e) {
                System.out.println("Could not close listen socket: ");
                System.out.println(e.getMessage());
                System.exit(-1);
            }
        }
    }

    /////////////////////
    /// EVENT LISTENERS ///
    /////////////////////

    private class SendButtonAction implements ActionListener {
        public void actionPerformed(ActionEvent ev) {
            String cmd = editorInput.getText();
            if (cmd.compareTo("") == 0) {
                return;
            }

            try {
                println(cmd);
                processor(cmd);
            } catch (Exception ex) {
                println("[EXCEPTION THROWN]: \"" + ex.getMessage() + '"');
            }
            editorInput.setText("");
        }
    }

    /////////////////////
    /// MAIN FUNCTIONS ///
    /////////////////////

    public void println(String msg) {
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss.SSS");
        editorOutput.setText(editorOutput.getText() + format.format(date) + ": " + msg + "\n");
        editorOutput.setCaretPosition(editorOutput.getText().length());
    }

    private void processor(String line) {
        parser.Parse(line);

        switch (parser.getCommand()) {
            case FLASH:
                if (parser.length() <= 1) {
                    println("Usage: flash <filename>");
                    return;
                }
                flashSelectedFirmware(parser.getArg(1));
                break;
            case RESET:
                resetSignal();
                break;
            case REBOOT:
                reboot();
                break;
            case SHUTDOWN:
                shutdown();
                break;
            case SLEEP:
                sleep();
                break;
            case INFO:
                info();
                break;
            case HELP:
                help();
                break;
            case EXIT:
                exit();
                break;
            case UNKNOWN:
                unknown();
                break;
            default:
                break;
        }
    }

    ///////////////////////
    /// USB COMMUNICATION ///
    ///////////////////////

    private final AbstractUsb4JavaInterface usbInterface = new AbstractUsb4JavaInterface() {
        @Override
        public UsbServices getUsbServices() {
            return UsbServices.getInstance();
        }

        @Override
        public UsbDevice findDeviceByVendorId(short vendorId) {
            List<UsbDevice> devices = getUsbServices().getdevices();
            Iterator<UsbDevice> iterator = devices.iterator();

            while (iterator.hasNext()) {
                UsbDevice device = iterator.next();
                if (device.getVendorId() == vendorId) {
                    return device;
                }
            }

            return null;
        }

        @Override
        public boolean controlTransfer(
                UsbDevice device,
                int bmRequestType,
                byte request,
                byte requestType,
                byte[] data,
                UsbEndpoint endpoint,
                Duration timeout) {
            try {
                UsbRequest urb = new UsbRequest();
                urb.setRecipient(UsbConst.REQUEST_RECIPIENT_DEVICE);
                urb.setRequestType(bmRequestType & UsbConst.REQUEST_TYPE_MASK);
                urb.setRequest(request);
                urb.setTimeout(timeout.toMillis());
                urb.setData(data);

                UsbControlPoint ctrlPt = device.getcontrolpoint();
                synchronized (ctrlPt) {
                    if (!ctrlPt.syncSubmit(urb)) {
                        return false;
                    }
                    urb.waitUntilComplete();
                }

                return true;
            } catch (InterruptedException e) {
                return false;
            } catch (UsbException e) {
                return false;
            }
        }
    };

    private abstract static class AbstractUsb4JavaInterface {
        public abstract UsbServices getUsbServices();

        public abstract UsbDevice findDeviceByVendorId(short vendorId);

        public abstract boolean controlTransfer(
                UsbDevice device,
                int bmRequestType,
                byte request,
                byte requestType,
                byte[] data,
                UsbEndpoint endpoint,
                Duration timeout);
    }

    private void flashSelectedFirmware(String firmwareFile) {
        // Open the file
        File f = new File(firmwareFile);
        if (!f.exists()) {
            println("ERROR: Could not find the firmware file: " + firmwareFile);
            return;
        }

        // Browse for the firmware image
        browseForFirmwareImage();

        // Validate the selected firmware file
        validateFirmwareFile();

        // Prepare the user interface for the flashing process
        prepareUserInterfaceForFlashingProcess();

        // Execute the flashing process
        executeFlashingProcess();

        // Update the user interface based on the outcome
        updateUserInterfaceBasedOnOutcome();
    }

    private void browseForFirmwareImage() {
        // Invoke platform-specific file browser
        JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(new File(System.getProperty("user.home")));
        fc.setFileFilter(new FileNameExtensionFilter("Supported Formats", "img", "tar", "tar.md5", "udc", "tar.ude", "zip", "rar", "7z", "gpg", "asc", "sig", "enc"));
        fc.setAcceptAllFileFilterUsed(false);
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            fileNameField.setText(file.getAbsolutePath());
        }
    }

    private void validateFirmwareFile() {
        // Performs validation checks against the firmware file

        // Verify that the file is accessible
        File f = new File(fileNameField.getText());
        if (!f.canRead()) {
            println("ERROR: Could not read the firmware file: " + fileNameField.getText());
            return;
        }

        // Verify that the firmware file is not too big
        long maxFileSize = 1024 * 1024 * 1024; // Maximum allowed size: 1 GB
        if (f.length() > maxFileSize) {
            println("ERROR: The firmware file exceeds the maximum allowed size of " + maxFileSize + " bytes (" + HumanReadable.humanReadableByteCount(maxFileSize, true) + ")");
            return;
        }
    }

    private void prepareUserInterfaceForFlashingProcess() {
        // Clear the console
        clearConsole();

        // Update the status label
        statusLabel.setText("Connecting to the device...");

        // Disable the browse button
        browseButton.setEnabled(false);

        // Enable or disable UI components based on whether the firmware is currently being flashed
        enableDisableComponents();
    }

    private void executeFlashingProcess() {
        // Create the thread pool executor
        ExecutorService executor = Executors.newSingleThreadExecutor();

        // Submit the task to the thread pool executor
        Future<?> future = executor.submit(() -> {
            try {
                // Acquire exclusive access to the device
                acquireExclusiveAccessToDevice();

                // Initiate the flashing process
                initiateFlashingProcess();

                // Wait until the flashing process completes
                waitForFlashingProcessToComplete();

                // Release exclusive access to the device
                releaseExclusiveAccessToDevice();
            } catch (Exception e) {
                displayFlashingFailureWarningDialog(e.getMessage());
            }
        });

        // Shutdown the thread pool executor
        executor.shutdown();

        // Wait for the completion of the submitted task
        try {
            future.get(1, TimeUnit.HOURS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            displayFlashingFailureWarningDialog("Timed Out waiting for Operation to Finish: " + e.getMessage());
        }
    }

    private void acquireExclusiveAccessToDevice() throws InterruptedException, UsbException {
        // Request explicit usage of the device
        device.claim();

        // Make sure the device is ready before continuing
        Thread.sleep(1000);
    }

    private void initiateFlashingProcess() throws InterruptedException, UsbException {
        // Enter bootloader mode
        enterBootloaderMode();

        // Erase the flash memory
        eraseFlashMemory();

        // Write the firmware image to the flash memory
        writeFirmwareImageToFlashMemory();
    }

    private void waitForFlashingProcessToComplete() throws InterruptedException {
        // Poll the status of the flashing process periodically
        while (!flashingCompleted) {
            Thread.sleep(100);
        }
    }

    private void releaseExclusiveAccessToDevice() throws InterruptedException {
        // Release exclusive access to the device
        device.release();

        // Give the device some time to settle
        Thread.sleep(1000);
    }

    private void enterBootloaderMode() throws InterruptedException, UsbException {
        // Send the magic sequence to enter bootloader mode
        sendMagicSequenceToEnterBootloaderMode();

        // Confirm that the device is in bootloader mode
        confirmDeviceIsInBootloaderMode();
    }

    private void sendMagicSequenceToEnterBootloaderMode() throws InterruptedException, UsbException {
        // Sequence borrowed from ADB protocol specification
        controlTransfer(device, (byte) (UsbConst.REQUEST_TYPE_CLASS | UsbConst.RECIPIENT_INTERFACE), (byte) 0x22, (byte) 0x00, new byte[0], endpoint, Duration.ofSeconds(1));
        controlTransfer(device, (byte) (UsbConst.REQUEST_TYPE_CLASS | UsbConst.RECIPIENT_INTERFACE), (byte) 0x23, (byte) 0x00, new byte[0], endpoint, Duration.ofSeconds(1));
    }

    private void confirmDeviceIsInBootloaderMode() throws InterruptedException, UsbException {
        // Query the device for its status
        byte[] status = new byte[1];
        controlTransfer(device, (byte) (UsbConst.REQUEST_TYPE_VENDOR | UsbConst.RECIPIENT_INTERFACE), (byte) 0x00, (byte) 0x00, status, endpoint, Duration.ofSeconds(1));

        // Expect the device to respond with a non-zero value
        if (status[0] == 0x00) {
            throw new RuntimeException("Device not in Bootloader Mode.");
        }
    }

    private void eraseFlashMemory() throws InterruptedException, UsbException {
        // Issue the mass erase command
        controlTransfer(device, (byte) (UsbConst.REQUEST_TYPE_CLASS | UsbConst.RECIPIENT_INTERFACE), (byte) 0x43, (byte) 0x00, new byte[0], endpoint, Duration.ofSeconds(1));

        // Wait for the mass erase to complete
        Thread.sleep(1000);
    }

    private void writeFirmwareImageToFlashMemory() throws InterruptedException, UsbException {
        // Open the input stream
        InputStream fis = new FileInputStream(firmwareFile);

        // Compute the number of packets to transmit
        int remainingBytes = (int) firmwareFile.length();
        int packetSize = endpoint.getMaxPacketSize();
        int numPackets = (remainingBytes / packetSize) + (remainingBytes % packetSize == 0 ? 0 : 1);

        // Transmit the firmware image in packets
        byte[] buffer = new byte[packetSize];
        int transferredBytes = 0;
        for (int i = 0; i < numPackets; ++i) {
            int bytesToRead = Math.min(packetSize, remainingBytes);
            int bytesRead = fis.read(buffer, 0, bytesToRead);
            if (bytesRead < 0) {
                throw new RuntimeException("Unexpected End Of File encountered.");
            }
            transferedBytes += bytesRead;
            controlTransfer(device, (byte) (UsbConst.REQUEST_TYPE_CLASS | UsbConst.RECIPIENT_INTERFACE), (byte) 0x21, (byte) 0x00, buffer, endpoint, Duration.ofSeconds(1));
            remainingBytes -= bytesRead;
        }

        // Close the input stream
        fis.close();
    }

    private void updateUserInterfaceBasedOnOutcome() {
        // Show the notification dialog
        if (flashingSuccessful) {
            showFlashedConfirmationDialog();
        } else {
            showFlashingFailureWarningDialog();
        }

        // Reset the user interface controls to their initial states
        resetUserInterfaceControls();
    }

    private void showFlashedConfirmationDialog() {
        // Display a modal confirmation dialogue indicating success
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                    rootPane,
                    "Firmware successfully flashed.
Device will restart shortly.",
                    "Flashing Complete",
                    JOptionPane.INFORMATION_MESSAGE
            );
        });
    }

    private void showFlashingFailureWarningDialog() {
        // Display a modal warning dialogue reporting failure in the flashing process
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                    rootPane,
                    "An error occurred during flashing:
" + failingReason,
                    "Flashing Failure",
                    JOptionPane.ERROR_MESSAGE
            );
        });
    }

    private void resetUserInterfaceControls() {
        // Reset the fields
        fileNameField.setText("");

        // Reactivate the browse button
        browseButton.setEnabled(true);

        // Enable or disable UI components based on whether the firmware is currently being flashed
        enableDisableComponents();
    }

    /////////////////////
    /// ADDITIONAL CODE ///
    /////////////////////

    private void enableDisableComponents() {
        // Enable or disable UI components based on whether the firmware is currently being flashed
    }

    private void saveSettings() {
        // Serialize and store settings prior to shutdown
    }

    private void loadSettings() {
        // Deserialize and restore settings upon launch
    }

    public static void main(String[] argv) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                USBDroidToolBox window = new USBDroidToolBox();
                window.setVisible(true);
            }
        });
    }

    private void resetSignal() {
        // Reset signal implementation
    }

    private void reboot() {
        
    }

    private void shutdown() {
        // Shutdown implementation
    }

    private void sleep() {
        // Sleep implementation
    }

    private void info() {
        // Info implementation
    }

    private void help() {
        // Help implementation
    }

    private void exit() {
        // Exit implementation
    }

    private void unknown() {
        // Unknown implementation
    }

    private void clearConsole() {
        // Clearing console implementation
    }

    private String copyToClipboard(String text) {
        // Copies text to clipboard implementation
    }

    private static class RowNumberColumn extends JComponent {
        private final JTextArea textArea;

        RowNumberColumn(JTextArea textArea) {
            this.textArea = textArea;
            setFont(textArea.getFont());
            setOpaque(true);
        }

        @Override
        protected void paintComponent(Graphics g) {
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());

            List CaretUpdateEvent = Collections.list(textArea.getCaretUpdates());
            CaretUpdateEvent.sort(Comparator.comparing(caretUpdateEvent -> caretUpdateEvent.param1));

            int rowHeight = textArea.getFontMetrics(textArea.getFont()).getHeight();
            int yStart = textArea.viewToModel(new Point(0, 0));

            int modelIndex = 0;
            int visibleRows = textArea.getVisibleRowCount();
            for (int i = 0; i < visibleRows; i++) {
                int viewRow = textArea.modelToView(modelIndex).y / rowHeight;

                if (i == CaretUpdateEvent.size() - 1) {
                    g.setColor(Color.BLACK);
                } else {
                    g.setColor(Color.GRAY);
                }

                if (viewRow == i) {
                    g.drawString(String.valueOf(modelIndex + 1), 5, yStart + i * rowHeight + rowHeight / 2);
                }

                if (++modelIndex == textArea.getDocument().getLength()) {
                    break;
                }
            }
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(40, textArea.getHeight());
        }
    }

    private static class ConsoleServer extends Thread {

        public static final int PORT = 2192;

        private ServerSocket listener;

        public ConsoleServer() throws IOException {
            listener = new ServerSocket(PORT);
        }

        @Override
        public void run() {
            try {
                while (true) {
                    new ClientHandler(listener.accept()).start();
                }
            } catch (IOException e) {
                System.out.println("Acceptance failed: ");
                System.out.println(e.getMessage());
            }
        }
    }

    private static class ClientHandler extends Thread {

        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;

        ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                String receivedMessage;
                while ((receivedMessage = in.readLine()) != null) {
                    println(receivedMessage);
                    processor(receivedMessage);
                }
            } catch (IOException e) {
                System.out.println("Connection closed: ");
                System.out.println(e.getMessage());
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    System.out.println("Could not close client socket: ");
                    System.out.println(e.getMessage());
                }
            }
        }
    }
}
