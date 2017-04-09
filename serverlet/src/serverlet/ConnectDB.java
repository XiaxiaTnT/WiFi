package serverlet;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class ConnectDB {
	public Statement stmt;
	public Statement Connect(){
		try{
			Class.forName("com.mysql.jdbc.Driver");
			String str1="jdbc:mysql://localhost:3306/wifi?useUnicode=true&characterEncodeing=utf-8&useSSL=false";
			Connection connect =DriverManager.getConnection(str1,"root","123456");
			stmt=connect.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
			//System.out.println("connected");
		}catch(Exception e){
			e.printStackTrace();
		}
		return stmt;
	}
}