package concurrency.ifix.util;

import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;
import concurrency.ifix.env.Env;

import java.io.*;
import java.util.*;

/**
 * Util to operate on file
 *
 * @author ann
 */
public class FileUtil {
    private static Logger logger = (Logger) LoggerFactory.getLogger(FileUtil.class);

    static {
        logger.setLevel(Env.LOG_LEVEL);
    }

    public static char[] getFileContents(File file) {
        // char array to store the file contents in
        char[] contents = null;
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while ((line = br.readLine()) != null) {
                // Additional content and missing new lines.
                sb.append(line + "\n");
            }
            contents = new char[sb.length()];
            sb.getChars(0, sb.length() - 1, contents, 0);

            assert (contents.length > 0);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return contents;
    }

    /**
     * Get all files located in the folder provided with the specific suffix.
     * @param folder target folder path, use absolute path
     * @param suffix allowed suffix, files with illegal suffix will be ignored
     * @return list of files
     */
    public static List<File> getFiles(File folder, String[] suffix){
        List<File> res = new ArrayList<>();
        if(folder.isDirectory()){
            // ignore test folder
            if("test".equals(folder.getName()) || "withlock".equals(folder.getName()) || folder.getName().endsWith("test")){
                return new ArrayList<>();
            }
            File[] files = folder.listFiles();
            if(files != null){
                for(File file : files){
                    if(file.exists()){
                        if(file.isDirectory()){
                            res.addAll(getFiles(file, suffix));
                        }
                        else if(isSuffixValid(file.getName(), suffix)){
                            res.add(file);
                        }
                    }
                }
            }
        }
        else if(isSuffixValid(folder.getName(), suffix)){
            res.add(folder);
        }
        return res;
    }

    public static String getSuffix(String s){
        final String separator = ".";
        if(s.contains(separator)){
            return s.substring(s.lastIndexOf(separator) + 1);
        }
        else{
            return "";
        }
    }

    public static boolean isSuffixValid(String s, String[] suffixArr){
        String suffix = getSuffix(s);
        Set<String> suffixSet = new HashSet<>(Arrays.asList(suffixArr));
        return suffixSet.contains(suffix);
    }

    public static String removeSuffix(String s){
        final String separator = ".";
        if(s.contains(separator)){
            return s.substring(0, s.lastIndexOf(separator));
        }
        else{
            return s;
        }
    }

    public static void writeToFile(String path, String content){
        File file = new File(path);
        writeToFile(file, content);
    }

    public static void writeToFile(File file, String content){
        if(! file.getParentFile().exists()){
            file.getParentFile().mkdirs();
        }
        if(file.exists()){
            file.delete();
        }
        try{
            FileWriter fw = new FileWriter(file);
            fw.write(content);
            fw.close();
        }
        catch(IOException e){
            logger.debug("write result to file error");
            e.printStackTrace();
        }
    }


}
