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
	 * 每一个点每一次采集每一个AP 存储所有数据 改变算法时 在这上面读取 产生新的avgRssList
	*/
	public static List<List<Map<String, Double>>> allRss=new ArrayList<>();//二者只初始化一次
	List <Integer[]> apVectors=new ArrayList<>();/**0-n向量组成的数组记录AP是否出现若干次*/

	List<Map<String, Double>> penaltyList=new ArrayList<>();/**和平均值相对于 记录每个点每个AP的可信度(1-P(miss)) */
	public static List<Map<String, Double>> avgRssList=new ArrayList<>();/**平均值*/
	
	/**每个点每个AP出现的RSS以及对应的次数 每一层大小：130*density-26-n 
	 * 使用TreeMap保证RSS是升序的 TreeMap的list按AP顺序添加*/
	List <List<TreeMap<Double, Integer>>> rssVectors=new ArrayList<>();
	
	public Point[] points;
	
	public static List<Double> XArr=new ArrayList<>();
	public static List<Double> YArr=new ArrayList<>();
	public static List<String> posxlist=new ArrayList<>();
	public static List<String> posylist=new ArrayList<>();

	
	private int neglect_frequency=0;//KNN设成?偏差最小  22
	private double posDensity;//采集点密度
	private double timeDensity;//采集次数密度
	private static double defaultRSS=-95.0;//默认missed AP为-100
	private static double availableRSS=-1000;//是每次的可用值还是平均值可用？
	
	/**
	 * 设置线下数据的参数
	 */
	public static class Options{
		double pDensity=1.0;//采集点密度
		double tDensity=1.0;//采集次数密度
		
		int neglect_frequency=0;//将此次数下的AP RSS置为default
		double defaultRSS=-95.0;//默认missed AP的RSS
		double availableRSS=-1000.0;//仅使用大于该值的RSS 默设为极小认为RSS都可用 该值和以上两个互斥
		
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
		buildRssVectorList();//build的是全部的情况
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
			List<Map<String, Double>> eachPosRss = null;//大小110（次）
			Integer[] apVector=new Integer[aplist.size()];//0-n向量
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
		for(List<Map<String, Double>> onePosRss:allRss){//130次
			List<TreeMap<Double, Integer>> apRssList=new ArrayList<>(aplist.size());//相对于ap的list，表示每一个ap的出现的值和次数
			for(String ap:aplist){//27次
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
	 * 直接对apVector处理 通过计算missed AP的概率计算每一个点每一个ap的可信度 用此方法 定位精度最高
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
