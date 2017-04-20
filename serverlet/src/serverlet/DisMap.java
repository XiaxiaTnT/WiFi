package serverlet;

import java.util.ArrayList;
import java.util.List;

public class DisMap {
	public List<OneItem> MapList;
	public DisMap() {
		this.MapList=new ArrayList<>();
	}
	public void Insert(double dist,int pos) {
		OneItem oi=new OneItem(dist,pos);
		if(MapList.size()==0) {
			MapList.add(oi);
		}
		else {
			int k=0;
			for(int i=0;i<MapList.size();i++) {
				if(oi.distance>MapList.get(i).distance) {
					k=i;break;
				}
			}
			MapList.add(k+1, oi);
		}
	}
	public void show() {
		for(OneItem o:MapList) {
			System.out.print(o.distance+" ");
		}
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		DisMap dismap=new DisMap();
		dismap.Insert(1.0, 1);
		dismap.Insert(2.0, 2);
		dismap.Insert(1.5, 3);
		dismap.Insert(1.25, 4);
		dismap.show();
	}
	class OneItem{
		public double distance;
		public int index;
		public OneItem(double dis,int i) {
			distance=dis;
			index=i;
		}
	}

}
