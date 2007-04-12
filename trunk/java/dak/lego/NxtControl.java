
package dak.lego;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

/**
 * This class implements all of the commands that can be sent to the NXT.
 * The streams can be from either a bluetooth connection, or a USB connection.
 *
 * @author David A. Kavanagh
 */
public class NxtControl {
	private boolean debug = false;
	private OutputStream oStr;
	private InputStream iStr;

	/**
	 * The contructor requires io streams for communication with the NXT.
	 *
	 * @param oStr the stream to send commands to
	 * @param iStr the stream to read responses from
	 */
	public NxtControl(OutputStream oStr, InputStream iStr) {
		this.oStr = oStr;
		this.iStr = iStr;
	}

	/**
	 * Enables debug.
	 */
	public void setDebug(boolean debugOn) {
		debug = debugOn;
	}

	/**
	 * Instructs the NXT to load and execute a program stored on it.
	 *
	 * @param fileName the name of the program file to start
	 */
	public void startProgram(String fileName) throws IOException {
		if (fileName.length() > 19)
			throw new IllegalArgumentException("Filename can be (at most) 15.3 characters");
		byte [] msg = new byte [fileName.length()+3];
		msg[0] = (byte)0x80;
		msg[1] = (byte)0x00;
		for (int i=0; i<fileName.length(); i++) {
			msg[i+2] = (byte)(fileName.charAt(i) & 0xff);
		}
		msg[fileName.length()+2] = 0;	// null terminator
		sendMessage(msg);
	}

	/**
	 * Stops execution of the currently running program.
	 */
	public void stopProgram() throws IOException {
		byte [] msg = new byte [2];
		msg[0] = (byte)0x80;
		msg[1] = (byte)0x01;
		sendMessage(msg);
	}

	/**
	 * Instructs the NXT to play the sound found in the file passed in. It can play
	 * once, or continuously.
	 *
	 * @param fileName the file containing the sound to be played
	 * @param loop set to true to repeat the sound (endlessly).
	 */
	public void playSoundFile(String fileName, boolean loop) throws IOException {
		if (fileName.length() > 19) {
			throw new IllegalArgumentException("Filename can be (at most) 15.3 characters");
		}
		byte [] msg = new byte [fileName.length()+4];
		msg[0] = (byte)0x80;
		msg[1] = (byte)0x02;
		msg[2] = (byte)(loop?1:0);
		for (int i=0; i<fileName.length(); i++) {
			msg[i+3] = (byte)(fileName.charAt(i) & 0xff);
		}
		msg[fileName.length()+3] = 0;	// null terminator
		sendMessage(msg);
	}

	/**
	 * This causes the NXT to pay a tone defined by the parameters.
	 *
	 * @param frequency cycles per second for the tone
	 * @param msec the duration of the tone
	 */
	public void playTone(char frequency, char msec) throws IOException {
		byte [] msg = new byte [6];
		msg[0] = (byte)0x80;
		msg[1] = (byte)0x03;
		msg[2] = (byte)(frequency & 0xff);
		msg[3] = (byte)((frequency >> 8) & 0xff);
		msg[4] = (byte)(msec & 0xff);
		msg[5] = (byte)((msec >> 8) & 0xff);
		sendMessage(msg);
	}

	/**
	 * Sends a message to a mailbox
	 *
	 * @param mailbox which mailbox to sent the message to (0-9)
	 * @param cargo the message to be sent
	 */
	public void sendMessage(int mailbox, String cargo) throws IOException {
		byte [] msg = new byte [cargo.length()+5];
		msg[0] = (byte)0x80;
		msg[1] = (byte)0x09;
		msg[2] = (byte)mailbox;
		msg[3] = (byte)(cargo.length()+1);
		for (int i=0; i<cargo.length(); i++) {
			msg[i+4] = (byte)(cargo.charAt(i) & 0xff);
		}
		msg[cargo.length()+4] = 0;	// null terminator
		sendMessage(msg);
	}

	/**
	 * Resets the position of a motor.
	 *
	 * @param port the port where the motor is conected (0-2)
	 * @param relative false for absolutin position
	 */
	public void resetMotorPosition(int port, boolean relative) throws IOException {
		if (port < 0 || port > 2) {
			throw new IllegalArgumentException("port must be 0, 1 or 2");
		}
		byte [] msg = new byte [4];
		msg[0] = (byte)0x00;
		msg[1] = (byte)0x0a;
		msg[2] = (byte)port;
		msg[3] = (byte)(relative?1:0);
		sendMessage(msg);
	}

	/**
	 * Queries the NXT for battery voltage
	 *
	 * @return the voltage of the battery
	 */
	public float getBatteryLevel() throws IOException {
		byte [] msg = new byte [2];
		msg[0] = (byte)0x00;
		msg[1] = (byte)0x0b;
		sendMessage(msg);
		byte [] response = getResponse();
		int level = response[3] | ((int)response[4] << 8);
		return level/(float)1000.0;
	}

	/**
	 * Halts sound playback
	 */
	public void stopSoundPlayback() throws IOException {
		byte [] msg = new byte [2];
		msg[0] = (byte)0x80;
		msg[1] = (byte)0x0c;
		sendMessage(msg);
	}

	/**
	 * Opens a file on the NXT for read.
	 *
	 * @param fileName name of the file to be read
	 * @return reference to the file to be used on subsequent commands
	 */
	public FileRef openRead(String fileName) throws IOException {
		if (fileName.length() > 19) {
			throw new IllegalArgumentException("Filename can be (at most) 15.3 characters");
		}
		return open(fileName, (byte)0x80);
	}

	/**
	 * Opens a file on the NXT for writing.
	 *
	 * @param fileName name of the file to be read
	 * @return reference to the file to be used on subsequent commands
	 */
	public FileRef openWrite(String fileName) throws IOException {
		if (fileName.length() > 19) {
			throw new IllegalArgumentException("Filename can be (at most) 15.3 characters");
		}
		return open(fileName, (byte)0x81);
	}

	private FileRef open(String fileName, byte cmd) throws IOException {
		byte [] msg = new byte [fileName.length()+2];
		msg[0] = (byte)0x01;
		msg[1] = cmd;
		for (int i=0; i<fileName.length(); i++) {
			msg[i+2] = (byte)(fileName.charAt(i) & 0xff);
		}
		msg[fileName.length()+2] = 0;	// null terminator
		sendMessage(msg);
		byte [] response = getResponse();
		return new FileRef(fileName, response[3], byteArrayToInt(response, 4));
	}

	/**
	 * Queries the NXT for the firmware version
	 *
	 * @return version numbers (0: protocol, 1: firmware)
	 */
	public float [] getFirmwareVersion() throws IOException {
		byte [] msg = new byte [2];
		msg[0] = (byte)0x01;
		msg[1] = (byte)0x88;
		sendMessage(msg);
		byte [] response = getResponse();
		return new float [] {response[4]+response[3]/(float)10.0, response[6]+response[5]/(float)10.0};
	}

	/**
	 * Sets the name of the NXT
	 *
	 * @param name the new brick name
	 */
	public void setBrickName(String name) throws IOException {
		if (name.length() > 15) {
			throw new IllegalArgumentException("Name can be (at most) 15 characters");
		}
		byte [] msg = new byte [2+name.length()];
		msg[0] = (byte)0x81;
		msg[1] = (byte)0x98;
		for (int i=0; i<name.length(); i++) {
			msg[i+3] = (byte)(name.charAt(i) & 0xff);
		}
		msg[name.length()+4] = 0;	// null terminator
		sendMessage(msg);
	}

	/**
	 * Queries the NXT for a bunch of useful information including;
	 *  - brick name
	 *  - bluetooth address
	 *  - bluetooth signal strength
	 *  - free user FLASH
	 *
	 * @return device information
	 */
	public DeviceInfo getDeviceInfo() throws IOException {
		byte [] msg = new byte [2];
		msg[0] = (byte)0x01;
		msg[1] = (byte)0x9b;
		sendMessage(msg);
		byte [] response = getResponse();
		DeviceInfo ret = new DeviceInfo();
		ret.setName(new String(response, 3, 14));
		String addr = "";
		for (int i=18; i<24; i++) {
			addr = addr+byteToHex(response[i]);
		}
		ret.setBlueToothAddress(addr);
		ret.setBlueToothSignalStrength(byteArrayToInt(response, 25));
		ret.setFreeUserFlash(byteArrayToInt(response, 29));
		return ret;
	}

	private void sendMessage(byte [] msg) throws IOException {
		dumpMessage(msg);
		oStr.write(msg.length & 0xff);
		oStr.write((msg.length >> 8) & 0xff);
		oStr.write(msg);
		oStr.flush();
	}

	private byte [] getResponse() throws IOException {
		int length = iStr.read() | (iStr.read()<<8);
		byte [] msg = new byte[length];
		for (int i=0; i<msg.length; i++) {
			msg[i] = (byte)iStr.read();
		}
		dumpMessage(msg);
		if (msg[0] != 0x02) {
			throw new IOException("Response not of proper format");
		}

		return msg;
	}

	private void dumpMessage(byte [] msg) {
		if (debug == false) return;

		System.err.print("msg : ");
		for (int i=0; i<msg.length; i++) {
			System.err.print(byteToHex(msg[i])+" ");
		}
		System.err.println("");
	}

	private String byteToHex(byte val) {
		String [] hex = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};
		byte tmp = (byte)((val & 0xf0) >>> 4);
		tmp = (byte)(tmp & 0x0f);
		String ret = hex[(int)tmp];
		tmp = (byte)(val & 0x0f);
		ret = ret + hex[(int)tmp];
		return ret;
	}

	private int byteArrayToInt(byte [] vals, int offset) {
		int ret = vals[offset+3] & 0xff;
		ret = (ret << 8) | (vals[offset+2] & 0xff);
		ret = (ret << 8) | (vals[offset+1] & 0xff);
		ret = (ret << 8) | (vals[offset] & 0xff);
		return ret;
	}
}
