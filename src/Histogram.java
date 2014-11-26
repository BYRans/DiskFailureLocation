public class Histogram {
	private String hostName;
	private String ip;
	private String devName;
	private String indicator;
	private int[] histInfo;
	
	public Histogram(int bins) {
		this.histInfo = new int[bins];
	}

	public String getIndicator() {
		return indicator;
	}


	public void setIndicator(String indicator) {
		this.indicator = indicator;
	}


	public String getDevName() {
		return devName;
	}

	public void setDevName(String devName) {
		this.devName = devName;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int[] getHistInfo() {
		return histInfo;
	}

	public void setHistInfo(int[] histInfo) {
		this.histInfo = histInfo;
	}


}
