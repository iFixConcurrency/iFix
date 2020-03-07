package fix.iDebugger.terminal;

import java.util.ArrayList;
import java.util.Arrays;

// 因为Windows 和linux命令有点差别 所以 2333
public class CmdString {
    public static String[] get(String[] cmd){
        ArrayList<String> cmds = new ArrayList<>();
        if(System.getProperty("os.name").contains("Windows")){
            cmds.add("cmd");
            cmds.add("/c");
        }
        else{
            cmds.add("/bin/sh");
            cmds.add("-c");
        }
        cmds.addAll(Arrays.asList(cmd));
        return cmds.toArray(new String[cmds.size()]);
    }
}
