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
	public final static int JudegCount = 30;// �����������һ��ֱ��ͼ
	public final static int Threshold = 9999999;// ��ֵ��ѵ���õ�
	public final static int TotalCount = 2500;// ���������������������ȥ��
	public final static String DataPath = "C:/Users/Administrator/Desktop/log/";// ���ݼ��ļ���Ŀ¼
	public final static String[] Indicator = new String[] { "tps", "rdSec",// ����ָ������,��ʱû�õ����������������÷�����ƣ������Ͳ�����get��setʱ����ָ����
			"wrSec", "avgrqSz", "avgquSz", "await", "svctm", "util" };

	public static void main(String[] args) throws FileNotFoundException {
		// TODO Auto-generated method stub
		List<DevInfo> dataSet = new ArrayList<DevInfo>();
		dataSet = readDataSet();// ��ȡ�������ļ������ݣ�List--devInfo
		int count = 0;// �ڼ��������ݣ�����ģ��ÿ��T���ռ�����

		// ��ӡ���ݼ�
		/*System.out.println("dataSet structure");
		System.out.println("dataSet size:" + dataSet.size());
		for (int i = 0; i < 10; i++) {
			System.out.println("hostName:" + dataSet.get(i).getHostName());
			System.out.println("hevName:" + dataSet.get(i).getDevName());
			System.out.println("ip:" + dataSet.get(i).getIp());
			System.out.print("devInfo--tps:");
			for (int k = 0; k < 30; k++) {
				System.out.print(","+dataSet.get(i).getTps().get(k));
			}
			System.out.println();
			System.out.println();
		}*/

		List<DevInfo> allDevInfoList = new ArrayList<DevInfo>();
		allDevInfoList = pullData(dataSet, count);
		// calFDR(allDevInfoList.get(0));// ����ֻ�ǵ���һ̨�����ϵ�����dev��ֻ��allList�����һ��list

	}

	public static List<DevInfo> pullData(List<DevInfo> dataSet, int count) {
		List<DevInfo> allDevInfoList = new ArrayList<DevInfo>();
		DevInfo dev = new DevInfo();
		for (int i = 0; i < dataSet.size(); i++) {// ��������dev
			List<List<Histogram>> devAllHistogramList = new ArrayList<List<Histogram>>();// һ��dev�ϵ�histCount��ֱ��ͼList������ָ��histCount�Σ�
			List<Histogram> histogramList = new ArrayList<Histogram>();// һ��dev�İ�������ָ���ֱ��ͼList������ָ��һ�Σ�
			dev = dataSet.get(i);
			HashMap<String, String> map = new HashMap<String, String>();
			map.put("hostName", dev.getHostName());
			map.put("ip", dev.getIp());
			map.put("devName", dev.getDevName());

			List<Double> tpsList = new ArrayList<Double>();
			List<Double> rdSecList = new ArrayList<Double>();
			List<Double> wrSecList = new ArrayList<Double>();
			List<Double> avgrqSzList = new ArrayList<Double>();
			List<Double> avgquSzList = new ArrayList<Double>();
			List<Double> awaitList = new ArrayList<Double>();
			List<Double> svctmList = new ArrayList<Double>();
			List<Double> utilList = new ArrayList<Double>();

			for (int j = 0; j < JudegCount; j++) {// ����ĳ��dev�����ָ��ĸ�����¼��ȡJudegCount��

				tpsList.add(dev.getTps().get(count * JudegCount + j));// histCount*JudegCount+j��ÿ�ζ���JudegCount������
				rdSecList.add(dev.getRdSec().get(count * JudegCount + j));
				wrSecList.add(dev.getWrSec().get(count * JudegCount + j));
				avgrqSzList.add(dev.getAvgrqSz().get(count * JudegCount + j));
				avgquSzList.add(dev.getAvgquSz().get(count * JudegCount + j));
				awaitList.add(dev.getAwait().get(count * JudegCount + j));
				svctmList.add(dev.getSvctm().get(count * JudegCount + j));
				utilList.add(dev.getUtil().get(count * JudegCount + j));
			}// �����������list����ĳ��dev��JudegCount����¼����������Indicator����򻯡�

		}

		return allDevInfoList;
	}

	public static void calFDR(List<DevInfo> devInfoList) {
		int bins = 0;
		double binSize = 0.0;
		DevInfo dev = null;

		for (int i = 0; i < devInfoList.size(); i++) {// ������̨�����ϵ�ÿ��dev
			List<List<Histogram>> devAllHistogramList = new ArrayList<List<Histogram>>();// һ��dev�ϵ�histCount��ֱ��ͼList������ָ��histCount�Σ�
			for (int histCount = 0; histCount < TotalCount / JudegCount; histCount++) {// һ��dev����histCount������ָ���ֱ��ͼList
				List<Histogram> histogramList = new ArrayList<Histogram>();// һ��dev�İ�������ָ���ֱ��ͼList������ָ��һ�Σ�
				dev = devInfoList.get(i);
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("hostName", dev.getHostName());
				map.put("ip", dev.getIp());
				map.put("devName", dev.getDevName());

				List<Double> tpsList = new ArrayList<Double>();
				List<Double> rdSecList = new ArrayList<Double>();
				List<Double> wrSecList = new ArrayList<Double>();
				List<Double> avgrqSzList = new ArrayList<Double>();
				List<Double> avgquSzList = new ArrayList<Double>();
				List<Double> awaitList = new ArrayList<Double>();
				List<Double> svctmList = new ArrayList<Double>();
				List<Double> utilList = new ArrayList<Double>();

				for (int j = 0; j < JudegCount; j++) {// ����ĳ��dev�����ָ��ĸ�����¼��ȡJudegCount��

					tpsList.add(dev.getTps().get(histCount * JudegCount + j));// histCount*JudegCount+j��ÿ�ζ���JudegCount������
					rdSecList.add(dev.getRdSec()
							.get(histCount * JudegCount + j));
					wrSecList.add(dev.getWrSec()
							.get(histCount * JudegCount + j));
					avgrqSzList.add(dev.getAvgrqSz().get(
							histCount * JudegCount + j));
					avgquSzList.add(dev.getAvgquSz().get(
							histCount * JudegCount + j));
					awaitList.add(dev.getAwait()
							.get(histCount * JudegCount + j));
					svctmList.add(dev.getSvctm()
							.get(histCount * JudegCount + j));
					utilList.add(dev.getUtil().get(histCount * JudegCount + j));
				}// �����������list����ĳ��dev��JudegCount����¼����������Indicator����򻯡�

				HashMap<String, List<Double>> indiDataListMap = new HashMap<String, List<Double>>();
				indiDataListMap.put("tps", tpsList);
				indiDataListMap.put("rdSec", rdSecList);
				indiDataListMap.put("wrSec", wrSecList);
				indiDataListMap.put("avgrqSz", avgrqSzList);
				indiDataListMap.put("avgquSz", avgquSzList);
				indiDataListMap.put("await", awaitList);
				indiDataListMap.put("svctm", svctmList);
				indiDataListMap.put("util", utilList);

				for (int l = 0; l < Indicator.length; l++) {
					map.put("indicator", Indicator[l]);
					List<Double> oneIndiDataList = indiDataListMap
							.get(Indicator[l]);
					Histogram histogram = createHistogram(oneIndiDataList, map);// һ��ָ���һ��ֱ��ͼ������JudegCount����¼��
					histogramList.add(histogram);
				}// histogramList����ĳ��ͷdev����ָ���һ��ֱ��ͼ

				System.out.println(histogramList.size());
				devAllHistogramList.add(histogramList);
				return;
			}
		}
	}

	public static Histogram createHistogram(List<Double> oneIndicatorList,
			HashMap<String, String> map) {
		int bins = 0;
		double binSize = 0.0;
		HashMap<String, Object> FDRMap = freedmanDiaconisRule(oneIndicatorList,
				Collections.max(oneIndicatorList),
				Collections.min(oneIndicatorList));// ȫ�ֵ�max��min��ȡ���ˣ�����������
		bins = (int) FDRMap.get("bins");
		binSize = (double) FDRMap.get("binSize");
		Histogram histogram = new Histogram(bins);
		System.out.println(oneIndicatorList.size());
		for (int i = 0; i < oneIndicatorList.size(); i++) {
			histogram.getHistInfo()[(int) (oneIndicatorList.get(i) / binSize)]++;
		}

		histogram.setHostName(map.get("hostName"));
		histogram.setIp(map.get("ip"));
		histogram.setDevName(map.get("devName"));
		histogram.setIndicator(map.get("indicator"));

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

	public static HashMap<String, Object> freedmanDiaconisRule(
			List<Double> values, Double globalMax, Double globalMin) {// ע�⣬��JudgeCount������ȫΪ0.00ʱ��bins��binSizeΪ0��0.0
		Integer bins = 0;
		Double binSize = 0.0;
		HashMap<String, Object> map = new HashMap<String, Object>();
		try {
			// Freedman-Diaconis rule
			double IQR = interQuartileRange(values);
			binSize = (2 * IQR * Math.pow(values.size(), -1 / 3));
			bins = (int) Math.ceil((globalMax - globalMin) / binSize);
		} catch (Exception e) {
			e.printStackTrace();
		}
		map.put("bins", bins);
		map.put("binSize", binSize);
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

	// ��ӡ��־
	public static void printLog(String content) {
		String path = "C:/Users/Administrator/Desktop/dfl.txt";
		File dirFile = new File(path);

		if (!dirFile.exists()) {
			dirFile.mkdir();
		}
		try {
			// new FileWriter(path + "t.txt", true) �������true ���Բ�����ԭ��TXT�ļ����� ��д
			BufferedWriter bw1 = new BufferedWriter(new FileWriter(path
					+ "t.txt", true));
			bw1.write(content);
			bw1.flush();
			bw1.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
