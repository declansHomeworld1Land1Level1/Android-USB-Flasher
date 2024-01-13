import com.hoho.usb.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class USBDroidToolbox {
    private static final NumberFormat FORMATTER = NumberFormat.getPercentInstance(Locale.US);

    private final USB_Interface usbInterface = new USB_Interface();

    private JFrame frame;
    private JTextArea textArea;
    private JProgressBar progressBar;
    private JLabel progressLabel;
    private JButton browseButton;
    private JButton flashButton;
    private JTextField fileNameField;

    private void initComponents() {
        frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBounds(100, 100, 640, 480);

        JMenuBar menuBar = new JMenuBar();
        frame.setJMenuBar(menuBar);

        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);

        JMenuItem quitItem = new JMenuItem("Quit");
        quitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(quitItem);

        Container container = frame.getContentPane();
        container.setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout());
        container.add(topPanel, BorderLayout.NORTH);

        textArea = new JTextArea();
        textArea.setEditable(false);
        container.add(new JScrollPane(textArea), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout());
        container.add(bottomPanel, BorderLayout.SOUTH);

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setStringPainted(true);
        bottomPanel.add(progressBar);

        progressLabel = new JLabel("Ready");
        bottomPanel.add(progressLabel);

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new FlowLayout());
        bottomPanel.add(inputPanel);

        fileNameField = new JTextField(20);
        inputPanel.add(fileNameField);

        browseButton = new JButton("Browse...");
        browseButton.addActionListener(e -> browseButton_ActionPerformed());
        inputPanel.add(browseButton);

        flashButton = new JButton("Flash");
        flashButton.setEnabled(false);
        flashButton.addActionListener(e -> flashButton_ActionPerformed());
        bottomPanel.add(flashButton);

        createMenubar();

        pack();
        centerFrame();
    }

    private void createMenubar() {
        JMenuBar menuBar = new JMenuBar();
        frame.setJMenuBar(menuBar);

        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);
    }

    private void centerFrame() {
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation(dim.width / 2 - frame.getSize().width / 2, dim.height / 2 - frame.getSize().height / 2);
    }

    private void browseButton_ActionPerformed() {
        JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(new File(System.getProperty("user.home")));
        String[] acceptedExtensions = {".tar.md5", ".tar", ".ums", ".img", ".tgz", ".zip", ".img.lz4", ".xip", ".bin", ".odin_img", ".tar.md1", ".tar.sha256", ".tgz.img", ".tgz.zip", ".tgz.md1", ".tgz.gz.md5"};
        fc.setFileFilter(new FileNameExtensionFilter("Firmware Files", acceptedExtensions));
        fc.setAcceptAllFileFilterUsed(false);

        int returnVal = fc.showOpenDialog(frame);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            fileNameField.setText(file.getAbsolutePath());
            flashButton.setEnabled(true);
        }
    }

    private void flashButton_ActionPerformed() {
        String fileName = fileNameField.getText();
        if (fileName.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please select a firmware file.");
            return;
        }

        progressBar.setIndeterminate(true);
        progressLabel.setText("Flashing...");
        textArea.setText("");

        Thread t = new Thread(() -> {
            try {
                flushRom(new File(fileName));
            } catch (Exception ex) {
                textArea.append("Error: " + ex.getMessage() + "\n");
            }

            SwingUtilities.invokeLater(() -> {
                progressBar.setIndeterminate(false);
                progressLabel.setText("Done");
            });
        });

        t.start();
    }

    private void flushRom(File romFile) throws Exception {
        // TODO: Implement the flash ROM functionality here.
    }

    public static void main(String[] args) {
        USBDroidToolbox toolbox = new USBDroidToolbox();
        toolbox.initComponents();
        toolbox.frame.setVisible(true);
    }

    private abstract static class USB_Interface {
        public abstract UsbServices getUsbServices();

        public abstract UsbDevice findDeviceByVendorId(short vendorId);

        public abstract void controlTransfer(UsbDevice device, UsbRequest request) throws UsbException;

        public abstract void interruptTransfer(UsbDevice device, UsbEndpoint endpoint, byte[] data, int timeout) throws UsbException;

        public abstract void bulkTransfer(UsbDevice device, UsbEndpoint endpoint, byte[] data, int timeout) throws UsbException;

        public abstract void bulkTransferAsync(UsbDevice device, UsbEndpoint endpoint, byte[] data, BulkTransferCallback callback) throws UsbException;
    }

    private static class Usb4JavaWrapper extends USB_Interface {
        private final UsbServices services;

        public Usb4JavaWrapper() {
            services = UsbHostManager.getUsbServices();
        }

        @Override
        public UsbServices getUsbServices() {
            return services;
        }

        @Override
        public UsbDevice findDeviceByVendorId(short vendorId) {
            List<UsbDevice> devices = services.getAttachedUsbDevices();

            for (UsbDevice device : devices) {
                if (device.getUsbDeviceDescriptor().idVendor == vendorId) {
                    return device;
                }
            }

            return null;
        }

        @Override
        public void controlTransfer(UsbDevice device, UsbRequest request) throws UsbException {
            device.getUsbControlPoint().controlTransfer(request);
        }

        @Override
        public void interruptTransfer(UsbDevice device, UsbEndpoint endpoint, byte[] data, int timeout) throws UsbException {
            UsbRequest request = new UsbRequest();
            request.setEndpoint(endpoint);
            request.setData(data);
            request.setCompletionInterval(timeout);

            device.getUsbControlPoint().interruptTransfer(request);
        }

        @Override
        public void bulkTransfer(UsbDevice device, UsbEndpoint endpoint, byte[] data, int timeout) throws UsbException {
            UsbRequest request = new UsbRequest();
            request.setEndpoint(endpoint);
            request.setData(data);
            request.setCompletionInterval(timeout);

            device.getUsbControlPoint().bulkTransfer(request);
        }

        @Override
        public void bulkTransferAsync(UsbDevice device, UsbEndpoint endpoint, byte[] data, BulkTransferCallback callback) throws UsbException {
            UsbRequest request = new UsbRequest();
            request.setEndpoint(endpoint);
            request.setData(data);
            request.setCompletionInterval(Integer.MAX_VALUE);
            request.setTag(callback);

            device.getUsbControlPoint().bulkTransferAsync(request);
        }
    }
}
