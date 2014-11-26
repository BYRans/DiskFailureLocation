import java.util.ArrayList;
import java.util.List;


public class DevInfo {
	private String devName;
	private String hostName;
	private String ip;
	
	private List<Double> tps;
	private List<Double> rdSec;
	private List<Double> wrSec;
	private List<Double> avgrqSz;
	private List<Double> avgquSz;
	private List<Double> await;
	private List<Double> svctm;
	private List<Double> util;
	
	private List<Double> FDR;
	
	
	public DevInfo(){
		this.tps = new ArrayList<Double>();
		this.rdSec = new ArrayList<Double>();
		this.wrSec = new ArrayList<Double>();
		this.avgrqSz = new ArrayList<Double>();
		this.avgquSz = new ArrayList<Double>();
		this.await = new ArrayList<Double>();
		this.svctm = new ArrayList<Double>();
		this.util = new ArrayList<Double>();
		this.FDR = new ArrayList<Double>();
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


	public List<Double> getTps() {
		return tps;
	}


	public void setTps(List<Double> tps) {
		this.tps = tps;
	}


	public List<Double> getRdSec() {
		return rdSec;
	}


	public void setRdSec(List<Double> rdSec) {
		this.rdSec = rdSec;
	}


	public List<Double> getWrSec() {
		return wrSec;
	}


	public void setWrSec(List<Double> wrSec) {
		this.wrSec = wrSec;
	}


	public List<Double> getAvgrqSz() {
		return avgrqSz;
	}


	public void setAvgrqSz(List<Double> avgrqSz) {
		this.avgrqSz = avgrqSz;
	}


	public List<Double> getAvgquSz() {
		return avgquSz;
	}


	public void setAvgquSz(List<Double> avgquSz) {
		this.avgquSz = avgquSz;
	}


	public List<Double> getAwait() {
		return await;
	}


	public void setAwait(List<Double> await) {
		this.await = await;
	}


	public List<Double> getSvctm() {
		return svctm;
	}


	public void setSvctm(List<Double> svctm) {
		this.svctm = svctm;
	}


	public List<Double> getUtil() {
		return util;
	}


	public void setUtil(List<Double> util) {
		this.util = util;
	}


	public List<Double> getFDR() {
		return FDR;
	}


	public void setFDR(List<Double> fDR) {
		FDR = fDR;
	}
	
	
}
