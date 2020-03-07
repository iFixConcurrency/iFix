package fix.iDebugger.cnf;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Ordering extends LogicBase{
    public Ordering(String input){
        super(input);
    }
    public boolean run(){
        boolean flag = false;
        for(int i = 0; i < this.myStack.size(); i++){
            String old = this.myStack.get(i);
            String newString = this.addBrackets(old);
            if(!old.equals(newString)){
                this.myStack.set(i, newString);
                flag = true;
            }
        }
        return flag;
    }
    private String addBrackets(String source){
        int count = 0;
        Pattern reg = Pattern.compile("\\s+(and|or|imp|iff)\\s+");
        Matcher m = reg.matcher(source);
        while(m.find()){ count+=1; }
        if(count < 2){
            return source;
        }
        Pattern regAnd = Pattern.compile("(neg\\s+)?\\S+\\s+and\\s+(neg\\s+)?\\S+");
        m = regAnd.matcher(source);
        if(m.find()){
            return m.replaceFirst("("+m.group(0)+")");
        }
        Pattern regOr = Pattern.compile("(neg\\s+)?\\S+\\s+or\\s+(neg\\s+)?\\S+");
        m = regOr.matcher(source);
        if(m.find()){
            return m.replaceFirst("("+m.group(0)+")");
        }
        Pattern regImp = Pattern.compile("(neg\\s+)?\\S+\\s+imp\\s+(neg\\s+)?\\S+");
        m = regImp.matcher(source);
        if(m.find()){
            return m.replaceFirst("("+m.group(0)+")");
        }
        Pattern regIff = Pattern.compile("(neg\\s+)?\\S+\\s+iff\\s+(neg\\s+)?\\S+");
        m = regIff.matcher(source);
        if(m.find()){
            return m.replaceFirst("("+m.group(0)+")");
        }
        return source;
    }
}