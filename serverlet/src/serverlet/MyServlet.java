package serverlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import serverlet.ConnectDB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import serverlet.OfflineData;

public class MyServlet extends HttpServlet{
	private static final long serialVersionUID = -9122125709896866661L;
	static OfflineData offline;
	public static String rssregex="(.*?)=(.*?);";
	public static Pattern rsspattern=Pattern.compile(rssregex);
	protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
		try {
			BufferedReader b=req.getReader();
			String line;
			//StringBuilder s=new StringBuilder();
			Statement stmt=new ConnectDB().Connect();
			while((line=b.readLine())!=null) {
				//s.append(line);
				System.out.println(line);
				stmt.executeUpdate(line);
				
			}
			resp.getWriter().write(1);
			stmt.close();
		}catch(Exception e) {
			e.getMessage();
		}
	}
	protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
		String Request=req.getParameter("request");
		System.out.println(Request);
		OfflineData.Options options = new OfflineData.Options(1.0,1.0,27, -99, -100);
		offline = new OfflineData(options);
		Map<String, Double> onrss=new HashMap<>();
		Matcher mt=rsspattern.matcher(Request);
		while(mt.find()) {
			String apnames=mt.group(1);
			double rssvalue=Double.valueOf(mt.group(2));
			onrss.put(apnames, rssvalue);
		}
		Point res=KNN(onrss,4,true);
		System.out.println(res.toString());
		resp.getWriter().write(res.toString());
	}
	protected void doDelete(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
		
	}
	protected void doPut(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
		
	}
	/**
	 * 用K-nearest neighbor进行定位
	 * @param onrss 一个线上点的RSS
	 * @param k
	 * @param usePenalty 是否使用penalty
	 * @param index 表示第几个线上点
	 */
	public static Point KNN(Map<String, Double> onrss, int k, boolean usePenalty) {
		Map<Double, Integer> distanceMap = new TreeMap<>();// 位置（0-129）-距离
		int p = 0;
		//System.out.println(offline.avgRssList);
		//System.out.println(offline.penaltyList);
		for (int i = 0; i < offline.XArr.size(); i++) {
			Map<String, Double> offrss = offline.avgRssList.get(i);
			//System.out.println(offline.avgRssList);
			Map<String, Double> penaltyMap = offline.penaltyList.get(i);
			double distance, sum = 0;
			for (int j = 0; j < offline.aplist.size(); j++) {
				double penalty = penaltyMap.get(offline.aplist.get(j));
				double off, on;
				if (offrss.get(offline.aplist.get(j)) != null)
					off = offrss.get(offline.aplist.get(j));
				else
					off = -100.0;

				if (onrss.get(offline.aplist.get(j)) != null)
					on = onrss.get(offline.aplist.get(j));
				else
					on = -100.0;
				sum += usePenalty ? penalty * (off - on) * (off - on) : (off - on) * (off - on);
			}
			distance = Math.sqrt(sum);
			distanceMap.put(distance, i);
			//System.out.println(distanceMap.size());
			//System.out.println(distance);
			// System.out.println(Constant.OFF_POS_ARR[p++]+" distance "+": "+distance);//输出off与on的距离
		}
		double xsum = 0.0, ysum = 0.0;
		int kk = 0;
		for (Double d : distanceMap.keySet()) {
			if (kk++ == k)
				break;
			int pos = distanceMap.get(d);
			xsum += offline.points[pos].x;
			ysum += offline.points[pos].y;
			//System.out.println(offline.points[pos] + " d=" + d);
		}
		Point result = new Point(xsum / k, ysum / k);
		return result;
	}
}
