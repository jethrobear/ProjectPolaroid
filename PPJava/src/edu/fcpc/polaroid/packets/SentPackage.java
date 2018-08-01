package edu.fcpc.polaroid.packets;

import java.io.Serializable;

public class SentPackage implements Serializable {
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

    public String username;
    public String password;
}
