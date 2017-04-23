package dataprocess;

import java.util.ArrayList;
import java.util.List;

public class Result {
	public List<Point> points=new ArrayList<>();
	public List<Double> deviations=new ArrayList<>();
	public double averageDeviation;
	public double variance;
	
	void addResult(Point point, double deviation){
		points.add(point);
		deviations.add(deviation);
	}
	
	/**计算总体的误差和方差*/
	void calculateOverAllDeviationAndVariance(){
		double deviationsum=0.0;
		for(double d: deviations){
			deviationsum+=d;
		}
		double avgdeviation=deviationsum/points.size();
		
		double variancesum=0.0;
		for(double d:deviations){
			variancesum+=(d-avgdeviation)*(d-avgdeviation);
		}
		double variance=Math.sqrt(variancesum);
		this.averageDeviation=avgdeviation;
		this.variance=variance;
		System.out.println("average deviation:"+this.averageDeviation+"  variance:"+this.variance);
	}
	public double getaverageDeviation() {
		return this.averageDeviation;
	}
	public double getvariance() {
		return this.variance;
	}
	public Result(){
		
	}
}
