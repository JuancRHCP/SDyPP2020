package edu.unlu.sdypp.ej1;

import java.util.HashSet;
import java.util.Set;

public class FilesP {
	private String checksum;
	private String name;
	private Set<Seeder> peers;

	public FilesP(String checksum, String name) {
		this.setChecksum(checksum);
		this.setName(name);
		peers = new HashSet<>();
	}
	
	public void AddPeer(Seeder p) {
		peers.add(p);
	}
	
	public void DeletePeer(Seeder p) {
		peers.remove(p);
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

	
	public Set<Seeder> getPeers() {
		return peers;
	}
	
	public void setName(String name) {
		this.name = name;
	}
}