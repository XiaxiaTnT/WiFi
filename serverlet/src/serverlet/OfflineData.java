package serverlet;


import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class OfflineData {
	public static List<String> aplist=new ArrayList<>();
	
	public static String rss_regex="(.*?)=(.*?);";
	public static Pattern rss_pattern=Pattern.compile(rss_regex);
	/**
	 * ÿһ����ÿһ�βɼ�ÿһ��AP �洢�������� �ı��㷨ʱ ���������ȡ �����µ�avgRssList
	*/
	public static List<List<Map<String, Double>>> allRss=new ArrayList<>();//����ֻ��ʼ��һ��
	List <Integer[]> apVectors=new ArrayList<>();/**0-n������ɵ������¼AP�Ƿ�������ɴ�*/

	List<Map<String, Double>> penaltyList=new ArrayList<>();/**��ƽ��ֵ����� ��¼ÿ����ÿ��AP�Ŀ��Ŷ�(1-P(miss)) */
	public static List<Map<String, Double>> avgRssList=new ArrayList<>();/**ƽ��ֵ*/
	
	/**ÿ����ÿ��AP���ֵ�RSS�Լ���Ӧ�Ĵ��� ÿһ���С��130*density-26-n 
	 * ʹ��TreeMap��֤RSS������� TreeMap��list��AP˳�����*/
	List <List<TreeMap<Double, Integer>>> rssVectors=new ArrayList<>();
	
	public Point[] points;
	
	public static List<Double> XArr=new ArrayList<>();
	public static List<Double> YArr=new ArrayList<>();
	public static List<String> posxlist=new ArrayList<>();
	public static List<String> posylist=new ArrayList<>();

	
	private int neglect_frequency=0;//KNN���?ƫ����С  22
	private double posDensity;//�ɼ����ܶ�
	private double timeDensity;//�ɼ������ܶ�
	private static double defaultRSS=-95.0;//Ĭ��missed APΪ-100
	private static double availableRSS=-1000;//��ÿ�εĿ���ֵ����ƽ��ֵ���ã�
	
	/**
	 * �����������ݵĲ���
	 */
	public static class Options{
		double pDensity=1.0;//�ɼ����ܶ�
		double tDensity=1.0;//�ɼ������ܶ�
		
		int neglect_frequency=0;//���˴����µ�AP RSS��Ϊdefault
		double defaultRSS=-95.0;//Ĭ��missed AP��RSS
		double availableRSS=-1000.0;//��ʹ�ô��ڸ�ֵ��RSS Ĭ��Ϊ��С��ΪRSS������ ��ֵ��������������
		
		public Options() {}
		public Options(String what, double value) {
			switch(what){
			case "pos":
				pDensity=value;
				break;
			case "time":
				tDensity=value;
				break;
			case "neglect":
				neglect_frequency=(int)value;
				break;
			case "default":
				defaultRSS=value;
				break;
			case "available":
				availableRSS=value;
				break;
			default:
				System.out.println("wrong");
			}
		}
		public Options(double p, double t, int nf,double deRss, double avRss) {
			pDensity=p;
			tDensity=t;
			neglect_frequency=nf;
			defaultRSS=deRss;
			availableRSS=avRss;
		}
	} 
	
	public static void main(String[] args) {
		Options ops=new Options();
		ops.availableRSS=-100;
		OfflineData offline=new OfflineData(ops);
		System.out.println(aplist.size());
		System.out.println(posxlist.size());
		System.out.println(XArr.size());
		System.out.println(allRss.size());
		System.out.println(allRss.get(0).get(0));
		System.out.println(avgRssList.get(0));
	}
	public OfflineData(Options options) {
		posDensity=options.pDensity;
		timeDensity=options.tDensity;
		defaultRSS=options.defaultRSS;
		availableRSS=options.availableRSS;
		neglect_frequency=options.neglect_frequency;
		initAps();
		initPos();
		initPoints();
		initRSSData();
		buildRssVectorList();//build����ȫ�������
		buildPenaltyList();
		generateAvgRss(allRss);
	}
	public void initAps() {
		try {
			Statement stmt=new ConnectDB().Connect();
			String findaps="select * from aps;";
			ResultSet rs=stmt.executeQuery(findaps);
			while(rs.next()) {
				aplist.add(rs.getString("ap"));
			}
			//System.out.println(aplist);
			rs.close();
			stmt.close();
		}catch(Exception e) {
			e.getMessage();
		}
	}
	public void initPos() {
		try {
			Statement stmt=new ConnectDB().Connect();
			String findpos="select * from postion;";
			ResultSet rs=stmt.executeQuery(findpos);
			while(rs.next()) {
				String x=rs.getString("posx");
				String y=rs.getString("posy");
				
				posxlist.add(x);
				posylist.add(y);
				XArr.add(Double.parseDouble(x));
				YArr.add(Double.parseDouble(y));
			}
			//System.out.println(XArr);
			//System.out.println(YArr);
		}catch(Exception e) {
			e.getMessage();
		}
	}
	public void initPoints(){
		points=new Point[XArr.size()];
		for(int i=0;i<XArr.size();i++){
			points[i]=new Point(XArr.get(i), YArr.get(i));
		}
	}
	
	public void initRSSData(){
		try {
			String line;
			Map<String,Double> eachTimeRss = null;
			List<Map<String, Double>> eachPosRss = null;//��С110���Σ�
			Integer[] apVector=new Integer[aplist.size()];//0-n����
			Tools.cleanArr(apVector);
			for(int j=0;j<posxlist.size();j++) {
				eachPosRss=new ArrayList<>(64);
				Statement stmt=new ConnectDB().Connect();
				String findrss="select * from rss where posx='"+posxlist.get(j)+"' and posy='"+posylist.get(j)+"';";
				ResultSet rs=stmt.executeQuery(findrss);
				while(rs.next()) {
					eachTimeRss=new HashMap<>();
					line=rs.getString("rssvalue");
					//System.out.println(line);
					Matcher matcher=rss_pattern.matcher(line);
					while(matcher.find()) {
						//System.out.println("find");
						String each_ap=matcher.group(1);
						double rss=Double.valueOf(matcher.group(2));
						//System.out.println(each_ap+rss);
						eachTimeRss.put(each_ap, rss);
						apVector[aplist.indexOf(each_ap)]++;
					}
					eachPosRss.add(eachTimeRss);
				}
				allRss.add(eachPosRss);
				apVectors.add(apVector);
				Tools.cleanArr(apVector);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void generateAvgRss(List<List<Map<String, Double>>> rssList){
		int []count=new int [aplist.size()];
		double []sum=new double[aplist.size()];
		for(List<Map<String, Double>> eachpos : rssList){
			Map<String,Double> eachavgrss=new HashMap<>();
			avgRssList.add(eachavgrss);
			for(Map<String,Double> eachTimeRss : eachpos){
				for(int i=0;i<aplist.size();i++){
					String ap=aplist.get(i);
					Double rss=eachTimeRss.get(ap);
					if(rss!=null && rss>availableRSS){
						sum[i]+=eachTimeRss.get(ap);
						count[i]++;
					}
				}
			}	
			for(int j=0;j<aplist.size();j++){
				if(count[j]!=0){
					double avg = sum[j]/count[j];
						eachavgrss.put(aplist.get(j), count[j]<neglect_frequency ? defaultRSS : avg);

				}
			}
			Tools.cleanArr(count, sum);
		}
	}
	
	public void buildRssVectorList(){
		for(List<Map<String, Double>> onePosRss:allRss){//130��
			List<TreeMap<Double, Integer>> apRssList=new ArrayList<>(aplist.size());//�����ap��list����ʾÿһ��ap�ĳ��ֵ�ֵ�ʹ���
			for(String ap:aplist){//27��
				TreeMap<Double, Integer> oneApRss=new TreeMap<>();
				for(Map<String,Double> oneTimeRss:onePosRss){
					
					if(oneTimeRss.get(ap)!=null){
						double rss=oneTimeRss.get(ap);
						if(oneApRss.containsKey(rss)){
							int times=oneApRss.get(rss);
							oneApRss.put(rss, ++times);
						}else
							oneApRss.put(rss, 1);
					}
					
				}
				apRssList.add(oneApRss);
			}
			rssVectors.add(apRssList);
		}
	}
	
	/**
	 * ֱ�Ӷ�apVector���� ͨ������missed AP�ĸ��ʼ���ÿһ����ÿһ��ap�Ŀ��Ŷ� �ô˷��� ��λ�������
	 */
	public void buildPenaltyList(){
		for(Integer[] apVector:apVectors){
			Map<String,Double> map=new HashMap<>();
			for(int i=0;i<apVector.length;i++){
				double prob=apVector[i]/110.0;
				map.put(aplist.get(i), prob);
			}
			penaltyList.add(map);
		}
	}	
}
