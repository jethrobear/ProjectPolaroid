package edu.fcpc.polaroid;

import java.io.Serializable;

public class SentPackage implements Serializable{
	public PackageStatus packageStatus;
	
	// Images metadata
	public String messageHash;
	public String filename;
	public byte[] imagebinary;
	
	// Logging information
	public String username;
	public String password;
}
