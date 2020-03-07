package fix.iDebugger.cnf;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CnfUtil {

	private static HashMap<String, String> map = new HashMap<>();
	
    static Boolean merging(LogicBase source){
        String old = source.getResult();
        source.mergeItems("or");
        source.mergeItems("and");
        return !old.equals(source.getResult());
    }
    
    private static String preHandle(String line){
    	String currentString = "A";
    	map.clear();
    	Pattern reg = Pattern.compile("\\d+");
        Matcher m = reg.matcher(line);
        while(m.find()){
            String s = m.group(0);
            if(! map.containsKey(s)){
                String t = currentString;
                map.put(s, t);
                currentString = getNextString(currentString);
            }
        }
        for(String s : map.keySet()){
            line = line.replaceAll(s, map.get(s));
        }
        line = line.replaceAll("\\|", "or");
        line = line.replaceAll("-", "neg ");
        line = line.replaceAll("&", "and");
    	return line;
    }
    
    private static String postHandle(String line) {
    	for(String s : map.keySet()){
    		line = line.replaceAll("neg ", "-");
    		line = line.replaceAll("or ", "");
    		line = line.replaceAll(map.get(s), s);
        }
        return line;
	}

    public static String toCnf(String line){
    	line = preHandle(line);
        Ordering ordering = new Ordering(line);
        while(ordering.run()){
            ordering = new Ordering(ordering.getResult());
        }
        merging(ordering);

        Distributive distributive = new Distributive(ordering.getResult());
        while(distributive.run()){
            distributive = new Distributive(distributive.getResult());
        }
        merging(distributive);

        Simplification simplification = new Simplification(distributive.getResult());
        simplification.run();
        String reString = simplification.getResult();
        
        return postHandle(reString);
    }

    private static String getNextString(String currentString){
        char[] strArr = currentString.toCharArray();
        int t = currentString.length() - 1;
        boolean flag = true;
        while(flag){
            if(strArr[t] != 'Z'){
                strArr[t] += 1;
                flag = false;
                currentString = new String(strArr);
            }
            else if (t == 0){
                currentString = new String(new char[currentString.length() + 1]).replace("\0", "A");
                flag = false;
            }
            else{
                t --;
            }
        }
        return currentString;
    }

}
