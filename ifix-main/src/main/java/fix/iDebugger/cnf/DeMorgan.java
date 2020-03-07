package fix.iDebugger.cnf;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class DeMorgan extends LogicBase{
    public DeMorgan(String input){
        super(input);
    }
    public Boolean run(){
        Boolean flag = false;
        String p = "neg\\s+(\\d+)";
        Pattern r = Pattern.compile(p);
        int myFinal = this.myStack.size() - 1;
        for(int i = 0; i<this.myStack.size(); i++){
            String target = this.myStack.get(i);
            Matcher m = r.matcher(target);
            if(! m.find()){
                continue;
            }
            String child = this.myStack.get(Integer.parseInt(m.group(1)));
            this.myStack.set(i,m.replaceFirst(Integer.toString(this.myStack.size())));
            this.myStack.add(this.doingDeMorgan(child));
            flag = true;
            break;
        }
        this.myStack.add(this.myStack.get(myFinal));
        return flag;
    }
    private String doingDeMorgan(String source){
        String[] items = source.split("\\s+");
        ArrayList<String> newItems = new ArrayList<String>();
        for(int i = 0;i<items.length;i++){
            if(items[i].contains("or")){
                newItems.add("and");
            }
            else if(items[i].contains("and")){
                newItems.add("or");
            }
            else if(items[i].contains("neg")){
                newItems.add("neg");
            }
            else if(items[i].trim().length() > 0){
                newItems.add("neg");
                newItems.add(items[i]);
            }
        }
        ArrayList<String> tmps = new ArrayList<String>();
        for(int i =0; i< newItems.size();i++){
            if(newItems.get(i).equals("neg")){
                if(newItems.get(i+1).equals("neg")){
                    newItems.set(i,"");
                    newItems.set(i+1,"");
                }
            }
            if(newItems.get(i).length() < 1){
                continue;
            }
            tmps.add(newItems.get(i));
        }
        String[] array = tmps.toArray(new String[tmps.size()]);
        return this.myJoin(" ",array);
    }
}