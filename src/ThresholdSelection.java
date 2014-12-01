import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

public class ThresholdSelection {

	public final static int JudgeCount = 30;// �����������һ��ֱ��ͼ
	public final static int JudgeWindows = 5;// ����widows�쳣���ж�Ϊfault
	public final static int TotalCount = 2500;// ���������������������ȥ��
	public final static String DataPath = "C:/Users/Administrator/Desktop/log/";// ���ݼ��ļ���Ŀ¼
	public final static String[] Indicator = new String[] { "tps", "rdSec",// ����ָ������,��ʱû�õ����������������÷�����ƣ������Ͳ�����get��setʱ����ָ����
			"wrSec", "avgrqSz", "avgquSz", "await", "svctm", "util" };
	public static HashMap<String, Integer> THMap = new HashMap<String, Integer>();
	static {
		initTHMap();// ��ʼ����ֵmap
	}

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub

		List<DevInfo> dataSet = new ArrayList<DevInfo>();
		dataSet = readDataSet();// ��ȡ�������ļ������ݣ�List--devInfo
		int winCount = 0;
		for (int i = 0; i < TotalCount / JudgeCount; i++) {
			List<DevInfo> allDevInfoList = new ArrayList<DevInfo>();
			allDevInfoList = pullData(dataSet, winCount++);
			List<List<Histogram>> allDevAllHistList = calHistogram(allDevInfoList);
			calThreshold(allDevAllHistList);
		}

		for (int i = 0; i < Indicator.length; i++) {
			changeConfigFile("src/Threshold", Indicator[i],
					THMap.get(Indicator[i]).toString());
		}

		System.out.println("Threshold generate success.");
	}

	public static void changeConfigFile(String filename, String key,
			String value) throws IOException {
		Properties prop = new Properties();
		prop.load(new FileInputStream(filename));
		prop.setProperty(key, value);
		prop.store(new FileOutputStream(filename), null);
	}

	public static void initTHMap() {
		for (int i = 0; i < Indicator.length; i++) {
			THMap.put(Indicator[i], 0);
		}
	}

	public static void calThreshold(List<List<Histogram>> allDevAllHistList) {
		Histogram histogram = null;
		for (int i = 0; i < allDevAllHistList.size(); i++) {// ��������dev
			for (int j = 0; j < allDevAllHistList.get(i).size(); j++) {// ����һ��dev������ָ��
				histogram = allDevAllHistList.get(i).get(j);
				Integer threshold = getThreshold(histogram.getIndicator());
				for (int k = 0; k < allDevAllHistList.size(); k++) {// ĳ��dev��ĳ��ָ���ֱ��ͼ�����������е�ȥ�Ƚ�
					Integer distance = calDistance(allDevAllHistList.get(k)
							.get(j), histogram);

					if (distance > threshold) {// ���������ֵ
						THMap.put(histogram.getIndicator(), distance);// ����THMap���Ӧֵָ�����ֵ
					}
				}
			}// ����һ��dev����ָ���ж�ѭ������

		}
	}

	// ����ֱ��ͼ����
	public static Integer calDistance(Histogram fHist, Histogram sHist) {
		Integer distance = 0;
		for (int i = 0; i < fHist.getHistInfo().length; i++)
			distance += Math.abs(fHist.getHistInfo()[i]
					- sHist.getHistInfo()[i]);
		return distance;
	}

	public static Integer getThreshold(String indicator) {
		Integer threshold = THMap.get(indicator);
		return threshold;
	}


	public static List<Accumulator> initAccumulator(List<DevInfo> dataSet) {
		List<Accumulator> devAccumulatorList = new ArrayList<Accumulator>();
		for (int i = 0; i < dataSet.size(); i++) {
			Accumulator accumulator = new Accumulator();
			accumulator.setDevName(dataSet.get(i).getDevName());
			accumulator.setHostName(dataSet.get(i).getHostName());
			accumulator.setIp(dataSet.get(i).getIp());
			accumulator.setAccumulator(0);
			devAccumulatorList.add(accumulator);
		}
		return devAccumulatorList;
	}

	public static List<DevInfo> pullData(List<DevInfo> dataSet, int count) {
		List<DevInfo> allDevInfoList = new ArrayList<DevInfo>();
		DevInfo dev = new DevInfo();
		for (int i = 0; i < dataSet.size(); i++) {// ��������dev
			dev = new DevInfo();
			dev.setHostName(dataSet.get(i).getHostName());
			dev.setDevName(dataSet.get(i).getDevName());
			dev.setIp(dataSet.get(i).getIp());
			for (int j = 0; j < JudgeCount; j++) {// ����ĳ��dev�����ָ��ĸ�����¼��ȡJudgeCount��
				dev.getTps().add(
						dataSet.get(i).getTps().get(count * JudgeCount + j));// histCount*JudgeCount+j��ÿ�ζ���JudgeCount������
				dev.getRdSec().add(
						dataSet.get(i).getRdSec().get(count * JudgeCount + j));
				dev.getWrSec().add(
						dataSet.get(i).getWrSec().get(count * JudgeCount + j));
				dev.getAvgrqSz()
						.add(dataSet.get(i).getAvgrqSz()
								.get(count * JudgeCount + j));
				dev.getAvgquSz()
						.add(dataSet.get(i).getAvgquSz()
								.get(count * JudgeCount + j));
				dev.getAwait().add(
						dataSet.get(i).getAwait().get(count * JudgeCount + j));
				dev.getSvctm().add(
						dataSet.get(i).getSvctm().get(count * JudgeCount + j));
				dev.getUtil().add(
						dataSet.get(i).getUtil().get(count * JudgeCount + j));
			}// �����������list����ĳ��dev��JudgeCount����¼����������Indicator����򻯡�
			allDevInfoList.add(dev);
		}
		return allDevInfoList;
	}

	public static List<List<Histogram>> calHistogram(List<DevInfo> devInfoList) {
		int bins = 0;
		double binSize = 0.0;

		List<Double> tpsList = new ArrayList<Double>();
		List<Double> rdSecList = new ArrayList<Double>();
		List<Double> wrSecList = new ArrayList<Double>();
		List<Double> avgrqSzList = new ArrayList<Double>();
		List<Double> avgquSzList = new ArrayList<Double>();
		List<Double> awaitList = new ArrayList<Double>();
		List<Double> svctmList = new ArrayList<Double>();
		List<Double> utilList = new ArrayList<Double>();

		for (int i = 0; i < devInfoList.size(); i++) {// ������̨�����ϵ�ÿ��dev
			for (int j = 0; j < JudgeCount; j++) {// ÿ��dev������ָ�����
				tpsList.add(devInfoList.get(i).getTps().get(j));
				rdSecList.add(devInfoList.get(i).getRdSec().get(j));
				wrSecList.add(devInfoList.get(i).getWrSec().get(j));
				avgrqSzList.add(devInfoList.get(i).getAvgrqSz().get(j));
				avgquSzList.add(devInfoList.get(i).getAvgquSz().get(j));
				awaitList.add(devInfoList.get(i).getAwait().get(j));
				svctmList.add(devInfoList.get(i).getSvctm().get(j));
				utilList.add(devInfoList.get(i).getUtil().get(j));
			}
		}

		HashMap<String, HashMap<String, Object>> allFDRMap = new HashMap<String, HashMap<String, Object>>();

		// ��һ������÷�ʽ��һ�£����䣿��
		Double[] globalMinMax = getGlobalMinMax(tpsList);
		HashMap<String, Object> FDRMap = freedmanDiaconisRule(tpsList,
				globalMinMax[1], globalMinMax[0]);// tpsָ��ȫ�ֵ�bins binSize
		allFDRMap.put("tps", FDRMap);

		globalMinMax = getGlobalMinMax(rdSecList);
		FDRMap = freedmanDiaconisRule(rdSecList, globalMinMax[1],
				globalMinMax[0]);
		allFDRMap.put("rdSec", FDRMap);

		globalMinMax = getGlobalMinMax(wrSecList);
		FDRMap = freedmanDiaconisRule(wrSecList, globalMinMax[1],
				globalMinMax[0]);
		allFDRMap.put("wrSec", FDRMap);

		globalMinMax = getGlobalMinMax(avgrqSzList);
		FDRMap = freedmanDiaconisRule(avgrqSzList, globalMinMax[1],
				globalMinMax[0]);
		allFDRMap.put("avgrqSz", FDRMap);

		globalMinMax = getGlobalMinMax(avgquSzList);
		FDRMap = freedmanDiaconisRule(avgquSzList, globalMinMax[1],
				globalMinMax[0]);
		allFDRMap.put("avgquSz", FDRMap);

		globalMinMax = getGlobalMinMax(awaitList);
		FDRMap = freedmanDiaconisRule(awaitList, globalMinMax[1],
				globalMinMax[0]);
		allFDRMap.put("await", FDRMap);

		globalMinMax = getGlobalMinMax(svctmList);
		FDRMap = freedmanDiaconisRule(svctmList, globalMinMax[1],
				globalMinMax[0]);
		allFDRMap.put("svctm", FDRMap);

		globalMinMax = getGlobalMinMax(utilList);
		FDRMap = freedmanDiaconisRule(utilList, globalMinMax[1],
				globalMinMax[0]);
		allFDRMap.put("util", FDRMap);

		List<List<Histogram>> allDevAllHistList = new ArrayList<List<Histogram>>();// ����dev������ָ���list
																					// dev-indicator-histogram
		for (int k = 0; k < devInfoList.size(); k++) {// ������̨�����ϵ�ÿ��dev
			HashMap<String, Object> devInfoMap = new HashMap<String, Object>();
			devInfoMap.put("hostName", devInfoList.get(k).getHostName());
			devInfoMap.put("ip", devInfoList.get(k).getIp());
			devInfoMap.put("devName", devInfoList.get(k).getDevName());

			HashMap<String, List<Double>> indiDataListMap = new HashMap<String, List<Double>>();
			indiDataListMap.put("tps", devInfoList.get(k).getTps());
			indiDataListMap.put("rdSec", devInfoList.get(k).getRdSec());
			indiDataListMap.put("wrSec", devInfoList.get(k).getWrSec());
			indiDataListMap.put("avgrqSz", devInfoList.get(k).getAvgrqSz());
			indiDataListMap.put("avgquSz", devInfoList.get(k).getAvgquSz());
			indiDataListMap.put("await", devInfoList.get(k).getAwait());
			indiDataListMap.put("svctm", devInfoList.get(k).getSvctm());
			indiDataListMap.put("util", devInfoList.get(k).getUtil());

			List<Histogram> devHistogramList = new ArrayList<Histogram>();
			for (int l = 0; l < Indicator.length; l++) {
				FDRMap = allFDRMap.get(Indicator[l]);
				devInfoMap.put("indicator", Indicator[l]);
				List<Double> oneIndiDataList = indiDataListMap
						.get(Indicator[l]);
				Histogram histogram = createHistogram(oneIndiDataList,
						(int) FDRMap.get("bins"),
						(double) FDRMap.get("binSize"), devInfoMap);// һ��ָ���һ��ֱ��ͼ������JudgeCount����¼��
				devHistogramList.add(histogram);
			}// devHistogramList����ĳ��dev����ָ���ֱ��ͼ��һ��ֱ��ͼ��

			allDevAllHistList.add(devHistogramList);// ����dev�ĸ���ָ���ֱ��ͼ
		}

		return allDevAllHistList;
	}

	public static Double[] getGlobalMinMax(List<Double> dataList) {
		Double[] globalMinMax = new Double[2];
		if (dataList == null || dataList.size() == 0)
			return globalMinMax;

		Double min = dataList.get(0);
		Double max = dataList.get(0);
		for (int i = 1; i < dataList.size(); i++) {
			if (dataList.get(i) > max)
				max = dataList.get(i);
			else if (dataList.get(i) < min)
				min = dataList.get(i);
		}

		globalMinMax[0] = min;
		globalMinMax[1] = max;
		return globalMinMax;
	}

	public static Histogram createHistogram(List<Double> oneIndicatorList,
			int bins, double binSize, HashMap<String, Object> devInfoMap) {

		Histogram histogram = new Histogram(bins + 1);// ��1�Ǳ����ٽ磬��binsӲsetΪ30��binSize�������ģ�����ֵ�ᵼ��Խ��
		for (int i = 0; i < oneIndicatorList.size(); i++) {
			histogram.getHistInfo()[(int) (oneIndicatorList.get(i) / binSize)]++;
		}

		histogram.setHostName((String) devInfoMap.get("hostName"));
		histogram.setIp((String) devInfoMap.get("ip"));
		histogram.setDevName((String) devInfoMap.get("devName"));
		histogram.setIndicator((String) devInfoMap.get("indicator"));

		return histogram;

	}

	public static List<DevInfo> readDataSet() {
		String path = DataPath;// ��ĳĿ¼�������ļ�·����Ȼ�����ζ�
		File file = new File(path);
		File[] fileList = file.listFiles();
		List<DevInfo> devInfoList = new ArrayList<DevInfo>();
		List<List<DevInfo>> tempAllDevInfoList = new ArrayList<List<DevInfo>>();
		for (int i = 0; i < fileList.length; i++) {
			devInfoList = readFileData(path + "/" + fileList[i].getName());
			tempAllDevInfoList.add(devInfoList);
		}
		List<DevInfo> dataSet = new ArrayList<DevInfo>();
		for (int i = 0; i < tempAllDevInfoList.size(); i++) {
			for (int j = 0; j < tempAllDevInfoList.get(i).size(); j++) {
				dataSet.add(tempAllDevInfoList.get(i).get(j));
			}
		}

		return dataSet;

	}

	public static List<DevInfo> readFileData(String fileName) {
		List<DevInfo> devInfoList = new ArrayList<DevInfo>();
		File file = new File(fileName);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			String tempString = null;
			int line = 1;
			int periodCount = 0;
			while ((tempString = reader.readLine()) != null) {
				if (tempString.trim().equals("")) {
					periodCount++;
					// System.out.println(periodCount);
				} else {
					String[] tempLineArray = tempString.split("\\s+");
					addToDevInfoList(tempLineArray, devInfoList, fileName);

				}
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
				}
			}
		}
		return devInfoList;
	}

	public static void addToDevInfoList(String[] tempLineArray,
			List<DevInfo> devInfoList, String filename) {
		DevInfo dev = null;
		int index = tempLineArray.length;
		if (tempLineArray[index - 1].contains(".")) {
			dev = getCurrentDev(devInfoList, tempLineArray[index - 9]);
			if (dev == null) {
				dev = new DevInfo();
				String[] filenameArray = filename.split("/");
				String[] hnIp = filenameArray[filenameArray.length - 1]
						.split("_");
				dev.setHostName(hnIp[0]);
				dev.setIp(hnIp[1]);
				dev.setDevName(tempLineArray[index - 9]);
				dev.getTps().add(Double.valueOf(tempLineArray[index - 8]));// ����������Ϊʱ����һ����ʱ�����У����AM
				// PM�С��˴����Ż�
				dev.getRdSec().add(Double.valueOf(tempLineArray[index - 7]));
				dev.getWrSec().add(Double.valueOf(tempLineArray[index - 6]));
				dev.getAvgrqSz().add(Double.valueOf(tempLineArray[index - 5]));
				dev.getAvgquSz().add(Double.valueOf(tempLineArray[index - 4]));
				dev.getAwait().add(Double.valueOf(tempLineArray[index - 3]));
				dev.getSvctm().add(Double.valueOf(tempLineArray[index - 2]));
				dev.getUtil().add(Double.valueOf(tempLineArray[index - 1]));
				devInfoList.add(dev);
			} else {

				dev.getTps().add(Double.valueOf(tempLineArray[index - 8]));// ����������Ϊʱ����һ����ʱ�����У����AM
				// PM�С��˴����Ż�
				dev.getRdSec().add(Double.valueOf(tempLineArray[index - 7]));
				dev.getWrSec().add(Double.valueOf(tempLineArray[index - 6]));
				dev.getAvgrqSz().add(Double.valueOf(tempLineArray[index - 5]));
				dev.getAvgquSz().add(Double.valueOf(tempLineArray[index - 4]));
				dev.getAwait().add(Double.valueOf(tempLineArray[index - 3]));
				dev.getSvctm().add(Double.valueOf(tempLineArray[index - 2]));
				dev.getUtil().add(Double.valueOf(tempLineArray[index - 1]));
			}
		}

	}

	public static DevInfo getCurrentDev(List<DevInfo> devInfoList,
			String devName) {
		DevInfo dev = null;

		for (int i = 0; i < devInfoList.size(); i++) {
			if (devName.equals(devInfoList.get(i).getDevName())) {
				dev = devInfoList.get(i);
				break;
			}
		}
		return dev;
	}

	// Freedman�CDiaconis rule
	public static HashMap<String, Object> freedmanDiaconisRule(
			List<Double> values, double globalMax, double globalMin) {// ע�⣬��JudgeCount������ȫΪ0.00ʱ��bins��binSizeΪ0��0.0
		Integer bins = 0;
		Double binSize = 0.0;
		HashMap<String, Object> map = new HashMap<String, Object>();
		try {
			// Freedman-Diaconis rule
			double IQR = interQuartileRange(values);
			binSize = (2 * IQR * Math.pow(values.size(), -1 / 3));
			bins = (int) Math.ceil((globalMax - globalMin) / binSize);

			if (binSize == 0.0) {
				// System.out.println("binSize == 0.0");
				bins = JudgeCount;
				binSize = (globalMax - globalMin) / bins;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		map.put("bins", bins);
		map.put("binSize", binSize);

		// System.out.println("(" + globalMax + " -" + globalMin + ") / "
		// + binSize + "=" + bins);
		return map;
	}

	// IQR
	public static double interQuartileRange(List<Double> values)
			throws Exception {
		double[] quartiles = quartiles(values);
		return quartiles[2] - quartiles[0];
	}

	public static double[] quartiles(List<Double> values) throws Exception {
		if (values.size() < 3)
			throw new Exception(
					"This method is not designed to handle indicator lists with fewer than 3 elements.");

		double median = median(values);

		List<Double> lowerHalf = getValuesLessThan(values, median, true);
		List<Double> upperHalf = getValuesGreaterThan(values, median, true);

		return new double[] { median(lowerHalf), median, median(upperHalf) };
	}

	public static List<Double> getValuesGreaterThan(List<Double> values,
			double limit, boolean orEqualTo) {
		List<Double> modValues = new ArrayList<Double>();

		for (double value : values)
			if (value > limit || (value == limit && orEqualTo))
				modValues.add(value);

		return modValues;
	}

	public static List<Double> getValuesLessThan(List<Double> values,
			double limit, boolean orEqualTo) {
		List<Double> modValues = new ArrayList<Double>();

		for (double value : values)
			if (value < limit || (value == limit && orEqualTo))
				modValues.add(value);

		return modValues;
	}

	public static double median(List<Double> values) {
		Collections.sort(values);
		if (values.size() % 2 == 1)
			return (double) values.get((values.size() + 1) / 2 - 1);
		else {
			double lower = (double) values.get(values.size() / 2 - 1);
			double upper = (double) values.get(values.size() / 2);

			return (lower + upper) / 2.0;
		}
	}

}
