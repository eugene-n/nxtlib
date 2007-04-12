
import java.io.IOException;
import java.util.ArrayList;

import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;

import dak.lego.DeviceInfo;
import dak.lego.NxtControl;

public class NxtTest implements DiscoveryListener {
	private ArrayList<RemoteDevice> devices;
	private RemoteDevice device;
	private StreamConnection sc;
	private NxtControl ctrl;

	public NxtTest() {
		devices = new ArrayList<RemoteDevice>();
	}

	public void run() throws IOException {
		discoverDevices();
	}

	public void connect(RemoteDevice device) throws IOException {
		String connURL = "btspp://"+device.getBluetoothAddress()+":1"; // channel number (1,2,3 for slave NXTs)
		System.err.println("using url : "+connURL);
		// Open connection
		sc = (StreamConnection) Connector.open(connURL);
		ctrl = new NxtControl(sc.openOutputStream(), sc.openInputStream());
	}

	public void sendMessage(String msg, int mailbox) throws IOException {
		ctrl.sendMessage(mailbox, msg);
	}

	private void discoverDevices() {
		System.err.println("going to test bluetooth discovery");
		try {
			LocalDevice local = LocalDevice.getLocalDevice();
			local.setDiscoverable(DiscoveryAgent.GIAC);
			DiscoveryAgent discovery = local.getDiscoveryAgent();
			RemoteDevice [] remote = discovery.retrieveDevices(DiscoveryAgent.CACHED);
			UUID [] searchList = new UUID[] {new UUID(0x1000), new UUID(0x0003)};
			if (remote != null) {
				System.err.println("found cached devices : "+remote.length);
				for (int i=0; i<remote.length; i++) {
        			System.err.println(remote[i].getFriendlyName(true));
					devices.add(remote[i]);
					discovery.searchServices(null, searchList, remote[i], this);
				}
			}
			remote = discovery.retrieveDevices(DiscoveryAgent.PREKNOWN);
			if (remote != null) {
				System.err.println("found pre known devices : "+remote.length);
				for (int i=0; i<remote.length; i++) {
        			System.err.println(remote[i].getFriendlyName(true));
					devices.add(remote[i]);
					discovery.searchServices(null, searchList, remote[i], this);
				}
			}
			boolean inquiryStarted = false;
			try {
				inquiryStarted = discovery.startInquiry(DiscoveryAgent.GIAC, this);
			} catch (BluetoothStateException bse) {
				System.err.println("inquiry failed");
			}
			if (inquiryStarted) {
				System.err.println("inquiry in progress");
			}
			else {
				System.err.println("inquiry failed");
			}
		} catch (BluetoothStateException bse) {
			System.err.println("error : "+bse.getMessage());
		} catch (IOException ioe) {
			System.err.println("error : "+ioe.getMessage());
		}
	}

	public void showDevices() {
		RemoteDevice device = null;
		for (RemoteDevice dev : devices) {
			try {
				String name = dev.getFriendlyName(true);
				if (name.toLowerCase().indexOf("nxt") > -1) {
					System.err.println("device found : "+name);
					device = dev;
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		if (device != null) {
			try {
				connect(device);
				/*
				ctrl.playTone((char)1000, (char)500);
				try { Thread.sleep(700); } catch (Exception ex) {}
				ctrl.playTone((char)4000, (char)500);
				try { Thread.sleep(700); } catch (Exception ex) {}
				ctrl.playTone((char)1000, (char)500);
				try { Thread.sleep(700); } catch (Exception ex) {}
				*/

while (true) {
				DeviceInfo info = ctrl.getDeviceInfo();
				System.err.println("name : "+info.getName());
				System.err.println("address : "+info.getBlueToothAddress());
				System.err.println("signal strength = "+info.getBlueToothSignalStrength());
				System.err.println("free user FLASH = "+info.getFreeUserFlash());
				System.err.println("battery level = "+ctrl.getBatteryLevel()+"v");
				try { Thread.sleep(2000); } catch (Exception ex) {}
}
//				sendMessage("f", 0);
//				try { Thread.sleep(750); } catch (Exception ex) {}
//				sendMessage("s", 0);
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
		devices.add(btDevice);
		System.err.println("Device Found");
	}

	public void inquiryCompleted(int discType) {
		if (discType != DiscoveryListener.INQUIRY_COMPLETED) {
			System.err.println("No Devices Found");
		}
		else {
			System.err.println("inquriy complete");
			showDevices();
		}
	}

	public void servicesDiscovered(int transId, ServiceRecord [] servRecord) {
		for (int i=0 ; i<servRecord.length; i++) {
			System.err.println("Service :"+(String)servRecord[i].getAttributeValue(0).getValue());
		}
	}

	public void serviceSearchCompleted(int transId, int respCode) {
		System.err.println("search completed.");
	}

	public static void main(String [] args) throws Exception {
		NxtTest test = new NxtTest();
		test.run();
		while (true) {
			try { Thread.sleep(5000); } catch (Exception ex) {}
		}
	}
}
