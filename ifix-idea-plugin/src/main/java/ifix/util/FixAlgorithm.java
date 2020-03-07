package ifix.util;

import java.util.List;

public class FixAlgorithm {
	public static int gcd(int a, int b){
	    if(a < b){
	    	int tmp = a;
	    	a = b;
	    	b = tmp;
	    }
	    if(b == 0){
	        return a;
	    }
	    else{
	        return gcd(b, a % b);
	    }
	}
	
	
	public static int lcm(int a, int b){
		return a * b / gcd(a, b);
	}
	
	public static int nGcd(List<Integer> list){
		if(list.isEmpty()){
			return -1;
		}
		if(list.size() == 1){
			return list.get(0);
		}
		int res = list.get(0);
		for(int i = 1; i < list.size(); i ++){
			res = gcd(res, list.get(i));
		}
		return res;
	}
	
	public static int nLcm(List<Integer> list){
		if(list.isEmpty()){
			return -1;
		}
		if(list.size() == 1){
			return list.get(0);
		}
		int res = list.get(0);
		for(int i = 1; i < list.size(); i ++){
			res = lcm(res, list.get(i));
		}
		return res;
	}
}
