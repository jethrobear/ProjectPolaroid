package edu.fcpc.polaroid.packets;

import java.io.Serializable;

public class SentPackage implements Serializable {
	private static final long serialVersionUID = -798073094253664883L;

	public PackageStatus packageStatus;

	// Images metadata
	public String messageHash;
	public String filename;
	public byte[] imagebinary;

	// Logging information
	public String lastname;
	public String firstname;
	public String birthmonth;
	public String birthday;
	public String birthyear;

	// Credentials
	public String username;
	public String password;

	// Return message
	public String retMessage;
}
