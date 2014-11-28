public class Accumulator {
	private String hostName;
	private String ip;
	private String devName;
	private Integer accumulator;// faulure count

	public void addAccumulator() {
		this.accumulator++;
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

	public String getDevName() {
		return devName;
	}

	public void setDevName(String devName) {
		this.devName = devName;
	}

	public Integer getAccumulator() {
		return accumulator;
	}

	public void setAccumulator(Integer accumulator) {
		this.accumulator = accumulator;
	}

}
