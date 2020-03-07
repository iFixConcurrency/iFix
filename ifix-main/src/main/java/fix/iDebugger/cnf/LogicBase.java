package fix.iDebugger.cnf;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class LogicBase{
    public String source;
    public ArrayList<String> myStack;
    private DoubleString removeBrackets(String source, int id){
        String reg = "\\(([^\\(]*?)\\)";
        Pattern r = Pattern.compile(reg);
        Matcher m = r.matcher(source);
        if(! m.find()){
            DoubleString tmp = new DoubleString();
            tmp.flag = false;
            return tmp;
        }
        DoubleString tmp = new DoubleString();
        tmp.flag = true;
        tmp.one = m.replaceFirst(Integer.toString(id));
        tmp.two = m.group(1);
        return tmp;
    }
    public String myJoin(String one, String[] array){
        String result = "";
        for(int i = 0; i < array.length-1;i++){
            result = result + array[i] + one;
        }
        return result + array[array.length-1];
    }
    public LogicBase(String input){
        this.myStack = new ArrayList<>();
        String myFinal = input;
        this.source = input;
        while(true){
            DoubleString tmp = this.removeBrackets(input, this.myStack.size());
            if(! tmp.flag){
                break;
            }
            input = tmp.one;
            myFinal = input;
            this.myStack.add(tmp.two);
        }
        this.myStack.add(myFinal);
    }
    public String getResult(){
        String root = this.myStack.get(this.myStack.size()-1);
        String p0 = "^\\s*([0-9]+)\\s*$";
        Pattern r0 = Pattern.compile(p0);
        Matcher m0 = r0.matcher(root);
        if(m0.find()){
            root = this.myStack.get(Integer.parseInt(m0.group(1)));
        }
        while(true){
            String p = "(\\d+)";
            Pattern r = Pattern.compile(p);
            Matcher m = r.matcher(root);
            if(! m.find()){
                break;
            }
            String newString = "("+ this.myStack.get(Integer.parseInt(m.group(1))) + ")";
            root = m.replaceFirst(newString);
        }
        return root;
    }

    public void mergeItems(String logic){
        String p0 = "(\\d+)";
        String p1 = "neg\\s+(\\d+)";
        Pattern r0 = Pattern.compile(p0);
        Pattern r1 = Pattern.compile(p1);
        boolean flag = false;
        for(int i = 0; i < this.myStack.size(); i++){
            String target = this.myStack.get(i);
            if(! target.contains(logic)){
                continue;
            }
            Matcher m1 = r1.matcher(target);
            if(m1.find()){
                continue;
            }
            Matcher m0 = r0.matcher(target);
            while(m0.find()){
                String j = m0.group(1);
                String child = this.myStack.get(Integer.parseInt(j));
                if(! child.contains(logic)){
                    continue;
                }
                Pattern newR = Pattern.compile("(^|\\s)" + j + "(\\s|$)");
                Matcher newM = newR.matcher(this.myStack.get(i));
                this.myStack.set(i, newM.replaceFirst(" "+child+" ").trim());
                flag = true;
            }
        }
        if(flag){
            this.mergeItems(logic);
        }
    }
}