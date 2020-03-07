package fix.iDebugger.cnf;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Distributive extends LogicBase{
    public Distributive(String input){
        super(input);
    }
    public boolean run(){
        boolean flag = false;
        Pattern r = Pattern.compile("(\\d+)");
        int myFinal = this.myStack.size() -1;
        for(int i = 0; i<this.myStack.size(); i++){
            String target = this.myStack.get(i);
            if(! target.contains("or")){
                continue;
            }
            Matcher m = r.matcher(target);
            while(m.find()){
                String j = m.group(1);
                String child = this.myStack.get(Integer.parseInt(j));
                if(!child.contains("and")){
                    continue;
                }
                Pattern newR = Pattern.compile("(^|\\s)" + j + "(\\s|$)");
                String[] items = child.split("\\s+and\\s+");
                String[] tmpList = new String[items.length];
                for(int k = 0 ; k < items.length; k++){
                    tmpList[k] = Integer.toString(this.myStack.size());
                    Matcher new_m = newR.matcher(target);
                    this.myStack.add(new_m.replaceAll(" "+items[k]+" ").trim());
                }
                this.myStack.set(i,this.myJoin(" and ",tmpList));
                flag = true;
            }
            if(flag){
                break;
            }
        }
        this.myStack.add(this.myStack.get(myFinal));
        return flag;
    }
}