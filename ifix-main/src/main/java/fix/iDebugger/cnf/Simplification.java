package fix.iDebugger.cnf;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Simplification extends LogicBase{
    public Simplification(String input){
        super(input);
    }
    public Boolean run(){
        String old = this.getResult();
        for(int i = 0; i<this.myStack.size(); i++){
            this.myStack.set(i, this.reducingOr(this.myStack.get(i)));
        }
        String myFinal = this.myStack.get(this.myStack.size()-1);
        int theSize = this.myStack.size();
        this.myStack.set(theSize-1,this.reducingAnd(this.myStack.get(theSize-1)));
        return old.length() != this.getResult().length();
    }
    public String reducingAnd(String target){
        if(!target.contains("and")){
            return target;
        }
        Set<String> items = new HashSet<String>(Arrays.asList(target.split("\\s+and\\s+")));
        for(String item : items){
            if(items.contains("neg "+item)){
                return "";
            }
            Pattern r = Pattern.compile("\\d+$");
            Matcher m = r.matcher(item);
            if(!m.find()){
                continue;
            }
            String value = this.myStack.get(Integer.parseInt(item));
            if(Collections.frequency(this.myStack, value) > 1){
                this.myStack.set(Integer.parseInt(item),"");
            }
        }
        for(int i = 0; i<this.myStack.size()-1; i++){

            if(this.myStack.get(i).length() == 0){
                items.remove(Integer.toString(i));
            }
        }
        return this.myJoin(" and ", items.toArray(new String[items.size()]));
    }
    public String reducingOr(String target){
        if(!target.contains("or")){
            return target;
        }
        Set<String> items = new HashSet<String>(Arrays.asList(target.split("\\s+or\\s+")));
        for(String item : items){
            if(items.contains("neg "+item)){
                return "";
            }
        }
        return this.myJoin(" or ", items.toArray(new String[items.size()]));
    }
}