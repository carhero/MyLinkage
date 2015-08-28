package com.libre.client.activity;

public class DMRDev {
	
	private String ip;
	private String uuid;
	private String devName = "";
	private int volume = 0;

	public DMRDev() {
		// TODO Auto-generated constructor stub
	}

	public DMRDev(String ip, String uuid) {
		this.ip = ip;
		this.uuid = uuid;
	}
	
	public DMRDev(String ip, String uuid, String name) {
		this.ip = ip;
		this.uuid = uuid;
		if (name != null)
			this.devName = name;
	}
	
	public String getUuid() {
		return uuid;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}


	public String getDevName() {
		return devName;
	}

	public void setDevname(String devName) {
		if (devName != null && devName != "")
			this.devName = devName;
	}



	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return ip;
	}


	public int getVolume() {
		return volume;
	}

	public void setVolume(int volume) {
		this.volume = volume;
	}



}
