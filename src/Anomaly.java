
public class Anomaly {
	private String hostName;
	private String ip;
	private String devName;
	private Integer anomalyCount;//total anomaly window count,exceed k regard as 1 failure
	private Integer windowCount;//how many windows now,clear when reach 2k-1
	private Integer vote;//device vote
	
	
	public void addWindowCount(){
		this.windowCount++;
	}
	public void addAnomalyCount(){
		this.anomalyCount++;
	}
	public void minusAnomalyCount(){
		this.anomalyCount--;
	}
	public void addVote(){
		this.vote++;
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
	public Integer getAnomalyCount() {
		return anomalyCount;
	}
	public void setAnomalyCount(Integer anomalyCount) {
		this.anomalyCount = anomalyCount;
	}
	public Integer getWindowCount() {
		return windowCount;
	}
	public void setWindowCount(Integer windowCount) {
		this.windowCount = windowCount;
	}
	public Integer getVote() {
		return vote;
	}
	public void setVote(Integer vote) {
		this.vote = vote;
	}	
	
	
}
