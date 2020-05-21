package edu.unlu.sdypp.ej1;

public class Seeder {
	private String pId;

	public Seeder(String ip, String p) {
		super();
		pId = ip+":"+p;	
	}
	
	public String getPeerId() {
		return pId;
	}
	
	public void setPeerId(String ip, String p) {
		pId = ip+":"+p;
	}
	
	public String getIp() {
		return pId.split(":")[0];
	}
	
	public String getPort() {
		return pId.split(":")[1];
	}
}