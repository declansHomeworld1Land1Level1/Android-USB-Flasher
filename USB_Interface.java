import com.hoho.usb.*;

import java.util.List;

public class USB_Interface {
    private UsbServices services;
    private UsbDevice device;
    private UsbConfiguration configuration;
    private UsbInterface iface;
    private UsbEndpoint endpoint;

    public USB_Interface() {
        // Initialize the usb4java library
        this.services = UsbHostManager.getUsb Services();
    }

    public void findDevice(String vendorId, String productId) {
        // Search for devices with matching Vendor and Product IDs
        List<UsbDevice> devices = service. getRootUsbHub().getAttachedUsbDevices();
        for (UsbDevice dev : devices) {
            if (dev.getVendor Id().equals(vendorId) && dev.getProductId().equals(productId)) {
                this.device = dev;
                break;
            }
        }
    }

    public void open() throws UsbException {
        // Open the connection to the device
        device.open();

        // Claim the default configuration and interface
        configuration = device.getActiveUsb Configuration();
        configuration.claim();
        iface = configuration.getUsb Interface(0);
        iface.claim();

        // Get the first available endpoint
        endpoint = iface.getUsb Endpoint(0);
    }

    public void close() throws UsbException {
        // Release resources
        endpoint.releaseInterrupt();
        iface.release();
        configuration.release();
        device.close();
    }

    public byte[] read() throws UsbException {
        // Read data from the endpoint
        int timeout = 500;
        ByteBuffer buffer = ByteBuffer.allocateDirect(endpoint.getMaxPacketSize());
        endpoint.controlTransfer(buffer, timeout);
        return new byte[buffer.position()];
    }

    public void write(byte[] data) throws UsbException {
        // Write data to the endpoint
        ByteBuffer buffer = ByteBuffer.wrap(data);
        endpoint.controlTransfer(buffer, 0);
    }
}
