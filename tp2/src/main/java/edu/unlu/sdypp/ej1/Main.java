package edu.unlu.sdypp.ej1;

public class Main {
	private String ip;
	private int p; //puerto
	private int level;
	
	public Main(String ip, int p, int level) {
		super();
		this.ip = ip;
		this.p = p;
		this.level = level;
	}
	
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public int getPort() {
		return p;
	}
	public void setPort(int p) {
		this.p = p;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	
	@Override
	public String toString() {
		return "ip: "+ip+", port : "+String.valueOf(p)+", level : "+level;
	}

}