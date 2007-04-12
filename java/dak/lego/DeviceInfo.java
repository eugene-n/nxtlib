
package dak.lego;

/**
 * This object contains information returned by getDeviceInfo().
 *
 * @author David A. Kavanagh
 */
public class DeviceInfo {
	String name;
	String blueToothAddress;
	int blueToothSignalStrength;
	int freeUserFlash;

	public DeviceInfo() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getBlueToothAddress() {
		return blueToothAddress;
	}

	public void setBlueToothAddress(String blueToothAddress) {
		this.blueToothAddress = blueToothAddress;
	}

	public int getBlueToothSignalStrength() {
		return blueToothSignalStrength;
	}

	public void setBlueToothSignalStrength(int blueToothSignalStrength) {
		this.blueToothSignalStrength = blueToothSignalStrength;
	}

	public int getFreeUserFlash() {
		return freeUserFlash;
	}

	public void setFreeUserFlash(int freeUserFlash) {
		this.freeUserFlash = freeUserFlash;
	}
}
