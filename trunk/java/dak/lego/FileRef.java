
package dak.lego;

/**
 * This is a file ref to be used when reading/writing files on the NXT.
 *
 * @author David A. Kavanagh
 */
public class FileRef {
	String name;
	int handle;
	int length;

	public FileRef(String name, int handle, int length) {
		this.name = name;
		this.handle = handle;
		this.length = length;
	}

	public String getName() {
		return name;
	}

	public int getHandle() {
		return handle;
	}

	public int length() {
		return length;
	}
}
