package edu.unlu.sdypp.ej1;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class StoreFile {
	private String checksum;
	private String name;
	private String pathname;

	public StoreFile(String pathname) throws NoSuchAlgorithmException, IOException {
		String[] file = pathname.split("/");
		this.setName(file[file.length-1]);
		this.setPathname(pathname);
		this.setChecksum(calculateChecksum(pathname));
	}
	
	public StoreFile(String pathname,  String checksum) throws NoSuchAlgorithmException, IOException {
		String[] file = pathname.split("/");
		this.setName(file[file.length-1]);
		this.setPathname(pathname);
		this.setChecksum(checksum);
	}

	private String calculateChecksum(String pathname) throws NoSuchAlgorithmException, IOException {
		MessageDigest messagdig = null;
		DigestInputStream digstr = null;
		messagdig = MessageDigest.getInstance("SHA-256");
		InputStream is = new FileInputStream(pathname);
		digstr = new DigestInputStream(is, messagdig);
		while (digstr.read() != -1) { /*READING FILE -> STORES THE HASH*/}
		return getMessageDigest(digstr);
	}

	private static String getMessageDigest(DigestInputStream digestInputStream) {
		MessageDigest messagdigest = digestInputStream.getMessageDigest();
		byte[] digestBytes = messagdigest.digest();
		String digestStr = getHexaString(digestBytes);
		return digestStr;
	}

	private static String getHexaString(byte[] data) {
		String r= new BigInteger(1, data).toString(16);
		return r;
	}
	public String getChecksum() {
		return checksum;
	}

	public void setChecksum(String checksum) {
		this.checksum = checksum;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPathname() {
		return pathname;
	}

	public void setPathname(String pathname) {
		this.pathname = pathname;
	}
}