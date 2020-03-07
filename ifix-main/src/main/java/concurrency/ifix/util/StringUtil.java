package concurrency.ifix.util;

public class StringUtil {
    public static boolean compare(String[] s1, String[] s2){
        if(s1 == null || s2 == null || s1.length != s2.length){
            return false;
        }
        for(int i = 0; i < s1.length; i ++){
            if(! s1[i].equals(s2[i])){
                return false;
            }
        }
        return true;
    }
}
