package dataprocess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Constant {
	
	/**
	 * 解析数据集的正则表达式
	 */
	public static String ap_regex="00:\\w\\w:\\w\\w:"
      							+ "\\w\\w:\\w\\w:\\w\\w";
	public static String rss_regex="00:(.*?)=(.*?),";
	public static String pos_regex="pos=(.*?),0.0;";
	public static String newline_regex="t=11";
	public static String starttime_regex="started";
	public static String endtime_regex="finished";
	
	public static String fixedid="id=00:02:2D:21:0F:33;";
	
	public static Pattern newline_pattern=Pattern.compile(Constant.newline_regex); 
	public static Pattern starttime_pattern=Pattern.compile(Constant.starttime_regex);
	public static Pattern endtime_pattern=Pattern.compile(Constant.endtime_regex);
	public static Pattern rss_pattern=Pattern.compile(Constant.rss_regex);
	public static Pattern ap_pattern=Pattern.compile(Constant.ap_regex);
	public static Pattern pos_pattern=Pattern.compile(Constant.pos_regex);
	public static String ON_PATH="1.5meters.online.trace.txt";
	
	//固定为offline的ap 27个
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
			"00:E0:63:82:8B:A9",};
	public static final String []ON_POS_ARR={
			 "-23.9,-9.65,0.0",
			 "-23.0,-10.55,0.0",
			 "-19.65,-10.7,0.0",
			 "-17.35,-11.2,0.0",
			 "-15.5,-9.6,0.0",
			 "-12.9,-10.5,0.0",
			 "-10.55,-9.9,0.0",
		     "-9.2,-10.75,0.0",
			 "-6.35,-10.2,0.0",
			 "-4.1,-9.9,0.0",
			 "-2.05,-11.1,0.0",
			 "-0.95,-9.95,0.0",
			 "0.85,-18.55,0.0",
			 "1.15,-15.15,0.0",
			 "0.75,-13.05,0.0",
			 "1.55,-11.95,0.0",
			 "1.2,-9.05,0.0",
			 "1.65,-6.3,0.0",
			 "1.2,-3.7,0.0",
			 "1.85,-2.25,0.0",
			 "1.71,1.81,0.0",
			 "2.02,7.45,0.0",
			 "2.49,7.6,0.0",
			 "1.52,9.32,0.0",
			 "0.78,10.94,0.0",
			 "1.39,6.61,0.0",
			 "4.51,7.63,0.0",
			 "6.0,7.88,0.0",
			 "9.08,7.24,0.0",
			 "10.62,3.87,0.0",
			 "10.99,7.19,0.0",
			 "11.76,7.76,0.0",
			 "11.39,5.0,0.0",
			 "12.95,5.25,0.0",
			 "13.46,7.85,0.0",
			 "14.98,7.55,0.0",
			 "21.45,6.62,0.0",
			 "21.84,5.16,0.0",
			 "22.38,3.94,0.0",
			 "22.3,6.36,0.0",
			 "28.12,7.57,0.0",
			 "31.78,7.62,0.0",
			 "32.16,7.08,0.0",
			 "32.68,3.48,0.0",
			 "23.5,4.22,0.0",
			 "25.23,7.78,0.0"
	};

	public static final double []ON_X_ARR={
			-23.9,
			-23.0,
			-19.65,
			-17.35,
			-15.5,
			-12.9,
			-10.55,
			-9.2,
			-6.35,
			-4.1,
			-2.05,
			-0.95,
			0.85,
			1.15,
			0.75,
			1.55,
			1.2,
			1.65,
			1.2,
			1.85,
			1.71,
			2.02,
			2.49,
			1.52,
			0.78,
			1.39,
			4.51,
			6.0,
			9.08,
			10.62,
			10.99,
			11.76,
			11.39,
			12.95,
			13.46,
			14.98,
			21.45,
			21.84,
			22.38,
			22.3,
			28.12,
			31.78,
			32.16,
			32.68,
			23.5,
			25.23,
	};

	public static final double []ON_Y_ARR={
			-9.65,
			-10.55,
			-10.7,
			-11.2,
			-9.6,
			-10.5,
			-9.9,
			-10.75,
			-10.2,
			-9.9,
			-11.1,
			-9.95,
			-18.55,
			-15.15,
			-13.05,
			-11.95,
			-9.05,
			-6.3,
			-3.7,
			-2.25,
			1.81,
			7.45,
			7.6,
			9.32,
			10.94,
			6.61,
			7.63,
			7.88,
			7.24,
			3.87,
			7.19,
			7.76,
			5.0,
			5.25,
			7.85,
			7.55,
			6.62,
			5.16,
			3.94,
			6.36,
			7.57,
			7.62,
			7.08,
			3.48,
			4.22,
			7.78,
	};
	
	
}
