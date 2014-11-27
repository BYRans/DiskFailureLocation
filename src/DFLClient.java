import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class DFLClient {
	public final static int JudgeCount = 30;// 几个样本组成一个直方图
	public final static int JudgeWindows = 5;// 几个widows异常则判定为fault
	public final static int Threshold = 9999999;// 阙值，训练得到
	public final static int TotalCount = 2500;// 总数据条数，后期这个会去掉
	public final static String DataPath = "C:/Users/Administrator/Desktop/log/";// 数据集文件夹目录
	public final static String[] Indicator = new String[] { "tps", "rdSec",// 监测的指标数组,暂时没用到，初步设想是利用反射机制，这样就不用在get、set时罗列指标了
			"wrSec", "avgrqSz", "avgquSz", "await", "svctm", "util" };

	public static void main(String[] args) throws FileNotFoundException {
		// TODO Auto-generated method stub
		List<DevInfo> dataSet = new ArrayList<DevInfo>();
		dataSet = readDataSet();// 获取到所有文件的数据：List--devInfo
		// int count = 0;// 第几次拉数据，用来模拟每个T内收集数据

		List<Accumulator> devAccumulatorList = getDevList(dataSet);
		
		
		
		
		
		for (int i = 0; i < TotalCount / JudgeCount; i++) {
			// **********once start**********
			List<DevInfo> allDevInfoList = new ArrayList<DevInfo>();
			allDevInfoList = pullData(dataSet, i);
			List<List<Histogram>> allDevAllHistList = calHistogram(allDevInfoList);
			// printHistogramSet(allDevAllHistList);
			List<Integer> abnormalWin = anomalyDetection(allDevAllHistList);
		}

		// **********once end**********

	}
	public static List<Accumulator> getDevList(List<DevInfo> dataSet){
		List<Accumulator> devAccumulatorList = new ArrayList<Accumulator>();
		Accumulator accumulator = new Accumulator();
		for(int i=0;i<dataSet.size();i++){
			accumulator.setDevName(dataSet.get(i).getDevName());
			accumulator.setHostName(dataSet.get(i).getHostName());
			accumulator.setIp(dataSet.get(i).getIp());
			accumulator.setAccumulator(0);
			devAccumulatorList.add(accumulator);
		}
		return devAccumulatorList;
	}

	public static List<DevInfo> pullData(List<DevInfo> dataSet,
			int count) {
		List<DevInfo> allDevInfoList = new ArrayList<DevInfo>();
		DevInfo dev = new DevInfo();
		for (int i = 0; i < dataSet.size(); i++) {// 迭代所有dev
			dev = new DevInfo();
			dev.setHostName(dataSet.get(i).getHostName());
			dev.setDevName(dataSet.get(i).getDevName());
			dev.setIp(dataSet.get(i).getIp());
			for (int j = 0; j < JudgeCount; j++) {// 迭代某个dev里各个指标的各条记录，取JudgeCount条
				dev.getTps().add(
						dataSet.get(i).getTps().get(count * JudgeCount + j));// histCount*JudgeCount+j是每次读下JudgeCount条数据
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
			}// 现在上面各个list里有某个dev的JudgeCount条记录，后期利用Indicator数组简化。
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

		for (int i = 0; i < devInfoList.size(); i++) {// 迭代该台机器上的每个dev
			for (int j = 0; j < JudgeCount; j++) {// 每个dev上下列指标汇总
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

		// 这一段想个好方式改一下，反射？？
		Double[] globalMinMax = getGlobalMinMax(tpsList);
		HashMap<String, Object> FDRMap = freedmanDiaconisRule(tpsList,
				globalMinMax[1], globalMinMax[0]);// tps指标全局的bins binSize
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

		List<List<Histogram>> allDevAllHistList = new ArrayList<List<Histogram>>();// 所有dev的所有指标的list
																					// dev-indicator-histogram
		for (int k = 0; k < devInfoList.size(); k++) {// 迭代该台机器上的每个dev
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
						(double) FDRMap.get("binSize"), devInfoMap);// 一个指标的一个直方图（包含JudgeCount条记录）
				devHistogramList.add(histogram);
			}// devHistogramList包含某个dev所有指标的直方图（一个直方图）

			allDevAllHistList.add(devHistogramList);// 所有dev的各个指标的直方图
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

		Histogram histogram = new Histogram(bins + 1);// 加1是避免临界，例bins硬set为30，binSize除出来的，最大的值会导致越界
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
		String path = DataPath;// 读某目录下所有文件路径，然后依次读
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
				dev.getTps().add(Double.valueOf(tempLineArray[index - 8]));// 倒着拿是因为时间那一列有时是两列，多出AM
				// PM列。此处待优化
				dev.getRdSec().add(Double.valueOf(tempLineArray[index - 7]));
				dev.getWrSec().add(Double.valueOf(tempLineArray[index - 6]));
				dev.getAvgrqSz().add(Double.valueOf(tempLineArray[index - 5]));
				dev.getAvgquSz().add(Double.valueOf(tempLineArray[index - 4]));
				dev.getAwait().add(Double.valueOf(tempLineArray[index - 3]));
				dev.getSvctm().add(Double.valueOf(tempLineArray[index - 2]));
				dev.getUtil().add(Double.valueOf(tempLineArray[index - 1]));
				devInfoList.add(dev);
			} else {

				dev.getTps().add(Double.valueOf(tempLineArray[index - 8]));// 倒着拿是因为时间那一列有时是两列，多出AM
				// PM列。此处待优化
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

	public static HashMap<String, Object> freedmanDiaconisRule(
			List<Double> values, double globalMax, double globalMin) {// 注意，当JudgeCount条数据全为0.00时，bins、binSize为0、0.0
		Integer bins = 0;
		Double binSize = 0.0;
		HashMap<String, Object> map = new HashMap<String, Object>();
		try {
			// Freedman-Diaconis rule
			double IQR = interQuartileRange(values);
			binSize = (2 * IQR * Math.pow(values.size(), -1 / 3));
			bins = (int) Math.ceil((globalMax - globalMin) / binSize);

			if (binSize == 0.0) {
				System.out.println("binSize == 0.0");
				bins = JudgeCount;
				binSize = (globalMax - globalMin) / bins;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		map.put("bins", bins);
		map.put("binSize", binSize);

		System.out.println("(" + globalMax + " -" + globalMin + ") / "
				+ binSize + "=" + bins);
		return map;
	}

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

	// 打印数据集
	public static void printDataSet(List<DevInfo> dataSet) {
		System.out.println("dataSet structure");
		System.out.println("dataSet size:" + dataSet.size());
		for (int i = 0; i < 10; i++) {
			System.out.println("hostName:" + dataSet.get(i).getHostName());
			System.out.println("hevName:" + dataSet.get(i).getDevName());
			System.out.println("ip:" + dataSet.get(i).getIp());
			System.out.print("devInfo--tps:");
			for (int k = 0; k < 30; k++) {
				System.out.print("," + dataSet.get(i).getTps().get(k));
			}
			System.out.println();
			System.out.println();
		}

	}

	// 打印直方图
	public static void printHistogramSet(List<List<Histogram>> dataSet) {
		System.out.println("*******Histogram Structure***********");
		System.out.println("List size:" + dataSet.size());
		System.out.println("indicator number:" + dataSet.get(0).size());
		for (int i = 0; i < dataSet.size(); i++) {
			System.out.print("dev--Histogram:");
			for (int k = 0; k < 1; k++) {
				System.out.println("devHostName:"
						+ dataSet.get(i).get(k).getHostName());
				System.out.println("devName:"
						+ dataSet.get(i).get(k).getDevName());
				System.out.println("Histogram indicator → :"
						+ dataSet.get(i).get(k).getIndicator());
				for (int l = 0; l < dataSet.get(i).get(k).getHistInfo().length; l++)
					System.out.print(","
							+ dataSet.get(i).get(k).getHistInfo()[l]);
				System.out.println();
				System.out.println();
			}
		}

	}

}
