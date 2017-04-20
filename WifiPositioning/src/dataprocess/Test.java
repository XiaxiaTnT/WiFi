package dataprocess;

public class Test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		double rs1_dev=0.0;
		double rs1_var=0.0;
		double rs2_dev=0.0;
		double rs2_var=0.0;
		for(int i=0;i<100;i++) {
			TestPositioning tp=new TestPositioning();
			tp.test(1,64);
			rs1_dev+=tp.results1.getaverageDeviation();
			rs1_var+=tp.results1.getvariance();
			rs2_dev+=tp.results2.getaverageDeviation();
			rs2_var+=tp.results2.getvariance();
		}
		rs1_dev=rs1_dev/100.0;
		rs1_var=rs1_var/100.0;
		rs2_dev=rs2_dev/100.0;
		rs2_var=rs2_var/100.0;
		System.out.println("knn method : averageDeviation:"+rs1_dev+"variance: "+rs1_var);
		System.out.println("wknn method: averageDeviation:"+rs2_dev+"variance: "+rs2_var);
	}

}
