package fix.iDebugger.cnf;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Replaceiff extends LogicBase{
    public Replaceiff(String input){
        super(input);
    }
    public Boolean run(){
        int myFinal = this.myStack.size() -1;
        Boolean flag = this.replace_all_iff();
        this.myStack.add(this.myStack.get(myFinal));
        return flag;
    }
    private Boolean replace_all_iff(){
        Boolean flag = false;
        for(int i = 0; i<this.myStack.size(); i++){
            TripleString ans = this.replaceIffInner(this.myStack.get(i),this.myStack.size());
            if(!ans.flag){
                continue;
            }
            this.myStack.set(i, ans.one);
            this.myStack.add(ans.two);
            this.myStack.add(ans.three);
            flag = true;
        }
        return flag;
    }
    private TripleString replaceIffInner(String source, int id){
        Pattern r = Pattern.compile("^(.*?)\\s+iff\\s+(.*?)$");
        Matcher m = r.matcher(source);
        if(! m.find()){
            TripleString tmp = new TripleString();
            tmp.flag = false;
            return tmp;
        }
        String a = m.group(1);
        String b = m.group(2);
        TripleString tmp = new TripleString();
        tmp.flag = true;
        tmp.one = Integer.toString(id) + " and " + Integer.toString(id+1);
        tmp.two = a + " imp " + b;
        tmp.three = b + " imp " + a;
        return tmp;
    }
}
