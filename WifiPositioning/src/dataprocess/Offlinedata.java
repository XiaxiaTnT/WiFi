package dataprocess;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dataprocess.Offlinedata.Options;
import processdataset.Constant;

import processdataset.Tools;



public class Offlinedata {
	public static String ap_regex = "00:\\w\\w:\\w\\w:\\w\\w:\\w\\w:\\w\\w";
	public static String rss_regex = "00:(.*?)=(.*?),";
	public static String pos_regex = "pos=(.*?),0.0;";
	public static String newline_regex = "t=11";
	public static String starttime_regex = "started";
	public static String endtime_regex = "finished";

	public static String fixedid = "id=00:02:2D:21:0F:33;";

	public static Pattern newline_pattern = Pattern.compile(Offlinedata.newline_regex);
	public static Pattern starttime_pattern = Pattern.compile(Offlinedata.starttime_regex);
	public static Pattern endtime_pattern = Pattern.compile(Offlinedata.endtime_regex);
	public static Pattern rss_pattern = Pattern.compile(Offlinedata.rss_regex);
	public static Pattern ap_pattern = Pattern.compile(Offlinedata.ap_regex);
	public static Pattern pos_pattern = Pattern.compile(Offlinedata.pos_regex);

	public static String OFF_PATH = "1.5meters.offline.trace.txt";
	
	public static List<List<Map<String, Double>>> allRss=new ArrayList<>();
	
	
    public static List<Integer[]> apVectors=new ArrayList<>();
    
	public static ArrayList <String> posXlist=new ArrayList<>();
	public static ArrayList <String> posYlist=new ArrayList<>();
	
	
	public static List <String> aplist=new ArrayList<>();
	
	public static List <List<TreeMap<Double, Integer>>> rssVectors=new ArrayList<>();
	
	public static List<Map<String, Double>> penaltyList=new ArrayList<>();/**和平均值相对于 记录每个点每个AP的可信度(1-P(miss)) */
	public static List<Map<String, Double>> avgRssList=new ArrayList<>();/**平均值*/
	
	public static Point[] points;
	
	private int neglect_frequency=0;//KNN设成?偏差最小  22
	private double posDensity;//采集点密度
	private double timeDensity;//采集次数密度
	private static double defaultRSS=-95.0;//默认missed AP为-100
	private static double availableRSS=-1000;//是每次的可用值还是平均值可用？
	
	
	public static final double []posXlist_1={-23.5,-22.0,-20.5,-19.0,-17.5,-16.0,-14.5,-13.0,-11.5,-10.0,-8.5,-7.0,-5.5,-4.0,-2.5,-1.0,//1
											 -23.5,-22.0,-20.5,-19.0,-17.5,-16.0,-14.5,-13.0,-11.5,-10.0,-8.5,-7.0,-5.5,-4.0,-2.5,-1.0,//2
											   2.0,  2.0,  2.0,  2.0,  2.0,  2.0,  2.0,  2.0,  2.0,  2.0, 2.0, 2.0, //3
											   0.5,  0.5,  0.5,  0.5,  0.5,  0.5,  0.5,  0.5,  0.5,  0.5, 0.5, 0.5,//4
											   0.5,  0.5,  0.5,  0.5,  0.5,  0.5,  0.5,  0.5,  0.5,//5
											   2.0,  2.0,  2.0,  2.0,  2.0,  2.0,  2.0,  2.0,  2.0,//6
											   3.5,  5.0,  6.5,  8.0,  9.5, 11.0, 12.5, 14.0, 15.5, 17.0,18.5,20.0,21.5,23.0,24.5,26.0,27.5,29.0,30.5,32.0,//7 circle
											  32.0,	32.0,
											  32.0,	30.5, 29.0,	27.5, 26.0,	24.5, 23.0,	21.5, 20.0,	18.5,17.0,15.5,14.0,12.5,11.0, 9.5,	8.0, 6.5, 5.0, 3.5,
											   9.5,  9.5, 11.0,	12.5, 12.5,	11.0,//8
											  21.5,	21.5, 23.0,	23.0, 24.5,	26.0, 26.0,	24.5,};//9
	public static final double []posYlist_1={-10.75,-10.75,-10.75,-10.75,-10.75,-10.75,-10.75,-10.75,-10.75,-10.75,-10.75,-10.75,-10.75,-10.75,-10.75,-10.75,//1
											  -9.25, -9.25,	-9.25, -9.25, -9.25, -9.25,	-9.25, -9.25, -9.25, -9.25,	-9.25, -9.25, -9.25, -9.25,	-9.25, -9.25,//2
											 -18.25,-16.75,-15.25,-13.75,-12.25,-10.75,	-9.25, -7.75, -6.25, -4.75,	-3.25, -1.75,//3
											 -18.25,-16.75,-15.25,-13.75,-12.25,-10.75,	-9.25, -7.75, -6.25, -4.75,	-3.25, -1.75,//4
											   0.5,   2.0,   3.5,	5.0,   6.5,   8.0,   9.5,  11.0,  12.5,//5
											  12.5,  11.0,   9.5,   8.0,   6.5,   5.0,   3.5,   2.0,   0.5,//6
											   3.5,   3.5,   3.5,   3.5,   3.5,   3.5,   3.5,   3.5,   3.5,   3.5,   3.5,	3.5,   3.5,   3.5,   3.5,   3.5,3.5,3.5,3.5,3.5,//7 circle
											   5.0,	  6.5,
											   8.0,   8.0,   8.0,   8.0,   8.0,   8.0,   8.0,   8.0,   8.0,   8.0,   8.0,   8.0,   8.0,   8.0,   8.0,   8.0,8.0,8.0,8.0,8.0,
											   6.5,   5.0,   5.0,   5.0,   6.5,   6.5,//8
											   6.5,   5.0,   5.0,   6.5,   6.5,   6.5,   5.0,   5.0,};//9
	
	public static final double []posXlist_2= { -23.5,-20.5,-17.5, -14.5,-11.5 ,-8.5 ,-5.5,-2.5,//1		     							    
			     							     2.0,  2.0,  2.0, 2.0,  2.0,  2.0,//3
			     							    2.0,  2.0,  2.0, 2.0,  2.0,//6
			     							    5.0,8.0,11.0,14.0,17.0,20.0,23.0,26.0,29.0,32.0,32.0,//7
			     							    11.0,//8
			     							    23.0,26.0,//9
	};
	public static final double []posYlist_2= {-10.75,-10.75,-10.75,-10.75,-10.75,-10.75,-10.75,-10.75,//1								  
											   -18.25,-15.25,-12.25,-9.25,-6.25,-3.25,//3
											    12.5,9.5,6.5,3.5,0.5,//6
											    3.5,   3.5,   3.5,   3.5,   3.5,   3.5,   3.5,   3.5,   3.5,   3.5,6.5,//7
											    6.5,//8
											    6.5,6.5,//9										   
	};
	public static final double []posXlist_3= {-23.5,-19.0,-14.5,-10.0,-5.5,-1.0,            2.0, 2.0,2.0,2.0,             2.0,2.0,2.0,    3.5,8.0,12.5,17.0,21.5,26.0,30.5,30.5,26.0,21.5,17.0,12.5,8.0,3.5,};
	public static final double []posYlist_3= {-10.75,-10.75,-10.75,-10.75,-10.75,-10.75,    -18.25,-13.75,-9.25,-4.75,    9.5,5.0,0.5,     3.5,3.5,3.5,3.5,3.5,3.5,3.5,8.0,8.0,8.0,8.0,8.0,8.0,8.0, };
	
	public static final double []posXlist_4= {-23.5,-17.5,-11.5,-5.5,         2.0,2.0,2.0,            2.0,2.0,2.0,     3.5,9.5,15.5,21.5,27.5,}; 
	public static final double []posYlist_4= {-10.75,-10.75,-10.75,-10.75,    -18.25,-12.25,-6.25,    12.5,6.5,0.5,    3.5,3.5,3.5,3.5,3.5,};
	
	public static final double []posXlist_5= {-23.5,-16.0,-8.5,-1.0 ,          2.0,2.0,2.0,           2.0,2.0,     3.5,11.0,18.5,26.0,};
	public static final double []posYlist_5= {-10.75,-10.75,-10.75,-10.75,     -18.25,-10.75,-3.25,   12.5,5.0,    3.5,3.5,3.5,3.5,};
	
	public static final double []posXlist_6= {-23.5,-14.5,-5.5,        2.0,2.0,        2.0,2.0,   3.5,12.5,21.5,30.5,};
	public static final double []posYlist_6= {-10.75,-10.75,-10.75,   -18.25,-9.25,    9.5,0.5,   3.5,3.5,3.5,3.5,};
	public static class Options{
//		double pDensity=1.0;//采集点密度
//		double tDensity=1.0;//采集次数密度
		
		int neglect_frequency=0;//将此次数下的AP RSS置为default
		double defaultRSS=-95.0;//默认missed AP的RSS
		double availableRSS=-1000.0;//仅使用大于该值的RSS 默设为极小认为RSS都可用 该值和以上两个互斥
		
		public Options() {}
		public Options(String what, double value) {
			switch(what){
//			case "pos":
//				pDensity=value;
//				break;
//			case "time":
//				tDensity=value;
//				break;
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
		public Options(int nf,double deRss, double avRss) {
//			pDensity=p;
//			tDensity=t;
			neglect_frequency=nf;
			defaultRSS=deRss;
			availableRSS=avRss;
		}
	}
	
	
	public void Offlinedatas(Options options,int width,List<Integer> ltimes){
		defaultRSS=options.defaultRSS;
		availableRSS=options.availableRSS;
		neglect_frequency=options.neglect_frequency;
		allRss=new ArrayList<>();
		apVectors=new ArrayList<>();
		aplist=new ArrayList<>();
		penaltyList=new ArrayList<>();
		avgRssList=new ArrayList<>();
		initRssData(width,ltimes);
		initPoints();
		buildRssVectorList();
		buildPenaltyList(ltimes.size());
		generateAvgRss(allRss);
	}
	

	public  Offlinedata(Options options, int width, List<Integer> ltimes) {
		// TODO Auto-generated constructor stub
		defaultRSS=options.defaultRSS;
		availableRSS=options.availableRSS;
		neglect_frequency=options.neglect_frequency;
		allRss=new ArrayList<>();
		apVectors=new ArrayList<>();
		aplist=new ArrayList<>();
		penaltyList=new ArrayList<>();
		avgRssList=new ArrayList<>();
		rssVectors=new ArrayList<>();
		initRssData(width,ltimes);
		initPoints();
		buildRssVectorList();
		buildPenaltyList(ltimes.size());
		generateAvgRss(allRss);
	}


	public static final String[]AP_ARR= {  
			"00:14:BF:B1:7C:54",
			"00:16:B6:B7:5D:8F",
			"00:14:BF:B1:7C:57",
			"00:14:BF:B1:97:8D",
			"00:16:B6:B7:5D:9B",
			"00:14:BF:3B:C7:C6",
			"00:14:BF:B1:97:8A",
			"00:14:BF:B1:97:81",
			"00:16:B6:B7:5D:8C",
			"00:11:88:28:5E:E0",
			"00:11:88:28:CB:70",
			"00:11:88:5A:31:50",
			"00:14:BF:B1:97:90",
			"00:11:88:5A:1B:10",
			"00:11:88:5A:B9:60",
			"00:11:88:5A:B9:00",
			"00:16:B6:B7:5D:89",
			"00:04:0E:5C:23:FC",
			"00:14:6C:A5:C2:F8",
			"00:11:88:5A:C4:40",
			"00:14:6C:62:CA:82",
			"00:03:C9:7A:29:8E",
			"00:14:6C:6F:2F:FC",
			"00:18:4D:B7:E2:4A",
			"00:15:0C:19:F9:9C",
			"00:0D:88:C4:1C:35",
			"00:E0:63:82:8B:A9",
			};
	
	
	public void initPoints(){
		points=new Point[posXlist.size()];
		for(int i=0;i<posXlist.size();i++){
			points[i]=new Point(Double.parseDouble(posXlist.get(i)),Double.parseDouble(posYlist.get(i)));
		}
	}
	
	
	public void initRssData(int width,List<Integer> Ltimes){
		aplist=Arrays.asList(AP_ARR);
		//生成每个位置取数据的随机数
		posXlist=new ArrayList<>();
		posYlist=new ArrayList<>();
		
		if(width==1) {
			for(int i=0;i<posXlist_1.length;i++) {
				posXlist.add(Double.toString(posXlist_1[i]));
				posYlist.add(Double.toString(posYlist_1[i]));
			}
		}
		else if(width==2) {
			for(int i=0;i<posXlist_2.length;i++) {
				posXlist.add(Double.toString(posXlist_2[i]));
				posYlist.add(Double.toString(posYlist_2[i]));
			}
		}
		else if(width==3) {
			for(int i=0;i<posXlist_3.length;i++) {
				posXlist.add(Double.toString(posXlist_3[i]));
				posYlist.add(Double.toString(posYlist_3[i]));
			}
		}
		else if(width==4) {
			for(int i=0;i<posXlist_4.length;i++) {
				posXlist.add(Double.toString(posXlist_4[i]));
				posYlist.add(Double.toString(posYlist_4[i]));
			}
		}
		else if(width==5) {
			for(int i=0;i<posXlist_5.length;i++) {
				posXlist.add(Double.toString(posXlist_5[i]));
				posYlist.add(Double.toString(posYlist_5[i]));
			}
		}
		else if(width==6) {
			for(int i=0;i<posXlist_6.length;i++) {
				posXlist.add(Double.toString(posXlist_6[i]));
				posYlist.add(Double.toString(posYlist_6[i]));
			}
		}
		try{
			File file=new File(OFF_PATH);
			FileInputStream fis = new FileInputStream(file);
			InputStreamReader isr=new InputStreamReader(fis);
			BufferedReader br=new BufferedReader(isr);
			String line;
			Map<String,Double> eachTimeRss = null;
			List<Map<String, Double>> eachPosRss = null;//大小110（次）
			Integer[] apVector=new Integer[aplist.size()];//0-n向量
			int Nwidth=0;
			int Ntimes=0;
			boolean isstart=true;
			boolean flag=true;
			while((line=br.readLine())!=null){
				Matcher newline_matcher=Offlinedata.newline_pattern.matcher(line);
				if(newline_matcher.find()&&line.contains(Offlinedata.fixedid)){
					if(isstart) {
						Pattern p=Pattern.compile("pos=(.*?),(.*?),0.0");
						Matcher m=p.matcher(line);
						if(m.find()) {
							String x=m.group(1);//m1 mac地址
							String y=m.group(2);
							if(posXlist.get(Nwidth).equals(x)&&posYlist.get(Nwidth).equals(y)) {
								flag=true;
								Nwidth++;
							}
							else {
								flag=false;
							}
						}						
						isstart=false;
					}
					if(flag==true&&Ltimes.contains(Ntimes)){
						eachTimeRss=new HashMap<>();
						String target=line.replace(Offlinedata.fixedid, "");
						Matcher rm=Offlinedata.rss_pattern.matcher(target);
						while(rm.find()){
							String each_ap="00:"+rm.group(1);//m1 mac地址 m2：rss
							double rss=Double.valueOf(rm.group(2));
						
							eachTimeRss.put(each_ap, rss);
							apVector[aplist.indexOf(each_ap)]++;
						}
						eachPosRss.add(eachTimeRss);
					}
					Ntimes++;
					//System.out.print(Ntimes);
				}
				Matcher starttime_matcher=Offlinedata.starttime_pattern.matcher(line);
				if(starttime_matcher.find()){
					//System.out.println();
					Ntimes=0;
					isstart=true;
					flag=false;
					eachPosRss=new ArrayList<>();
					apVector=new Integer[aplist.size()];
					for(int i=0;i<apVector.length;i++){
						apVector[i]=0;
					}
				}
				Matcher endtime_matcher=Offlinedata.endtime_pattern.matcher(line);
				if(endtime_matcher.find()){
					if(flag==true){
						allRss.add(eachPosRss);
						apVectors.add(apVector);
					}
				}	
				
			}
			//System.out.println(allRss.get(0).get(0));
			//System.out.println(allRss.size());
		}catch(Exception e){
			e.getMessage();
		}
		
	}
	public void buildRssVectorList(){
		for(List<Map<String, Double>> onePosRss:allRss){//130次
			List<TreeMap<Double, Integer>> apRssList=new ArrayList<>(27);//相对于ap的list，表示每一个ap的出现的值和次数
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
	
	public void buildPenaltyList(int times){
		for(Integer[] apVector:apVectors){
			Map<String,Double> map=new HashMap<>();
			for(int i=0;i<apVector.length;i++){
				double prob=apVector[i]/(times*1.0);
				map.put(aplist.get(i), prob);
			}
			penaltyList.add(map);
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
//					if(availableRSS<=-100){//使用了availableRSS 频率一般不会太少
						eachavgrss.put(aplist.get(j), count[j]<neglect_frequency ? defaultRSS : avg);
//					} 
//					else {//使用availableRSSb 如果平均值-81 实际79 误差就会很大
//						if(avg<-availableRSS) continue;
//						else eachavgrss.put(aplist.get(j), avg);
//					}
				}
			}
			Tools.cleanArr(count, sum);
		}
//		System.out.println(avgRssList.size());
//		for(int i=0;i<avgRssList.size();i++){
//			System.out.println(avgRssList.get(i));
//		}
		
	}
	
	
	
//	public static void main(String []args){
//		Options op=new Options();
//		op.availableRSS=-100;
//		Offlinedata offline=new Offlinedata(op,4,100);
//		//Tools.displayAllRSS(allRss, aplist);
////		System.out.println(posXlist);
////		System.out.println(posYlist);
//		System.out.println(posXlist.size());
//		System.out.println(allRss.size());
//		for(int i=0;i<points.length;i++) {
//			System.out.print(points[i]);
//		}
//	}
}
