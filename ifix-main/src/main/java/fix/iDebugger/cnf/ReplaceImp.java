package fix.iDebugger.cnf;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ReplaceImp extends LogicBase{
    public ReplaceImp(String input){
        super(input);
    }
    public Boolean run(){
        Boolean flag = false;
        for(int i = 0; i<this.myStack.size(); i++){
            String ans = this.replace_imp_inner(this.myStack.get(i));
            if(ans.length() == 0){
                continue;
            }
            this.myStack.set(i,ans);
            flag = true;
        }
        return flag;
    }
    private String replace_imp_inner(String source){
        Pattern r = Pattern.compile("^(.*?)\\s+imp\\s+(.*?)$");
        Matcher m = r.matcher(source);
        if(! m.find()){
            return "";
        }
        String a = m.group(1);
        String b = m.group(2);
        if(a.contains("neg ")){
            return a.replace("neg ", "") + " or " + b;
        }
        return "neg " + a + " or " + b;
    }
}