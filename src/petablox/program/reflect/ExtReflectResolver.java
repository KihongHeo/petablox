package petablox.program.reflect;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import petablox.project.Config;
import petablox.project.Messages;
import petablox.project.OutDirUtils;
import petablox.util.Utils;
import soot.options.Options;


/**
 * Dynamic reflection resolution using soot/tamiflex tools.
 *
 */
public class ExtReflectResolver {
	private static final String STARTING_RUN = "INFO: Reflection resolution using SOOT: Starting Run ID %s.";
    private static final String FINISHED_RUN = "INFO: Reflection resolution using SOOT: Finished Run ID %s.";
    private static final String PLAY_OUT_JAR = "poa-2.0.1.jar";
    private static final String BOOSTER_JAR  = "booster-trunk.jar";
    private static final String REFL_DIRNAME_PREFIX = "classesRefl";
    private static final String REFL_DATA_FILENAME = "out/refl.log";
    private static final String REFL_DATA_FILTERED = "out/refl_filt.log";
    private static final String PO_OUT = "out";
    private static final String PO_SCRATCH = "scratch";
    private static final String SUPPORTED_REFL ="\"^Class\\.forName\\|^Class\\.newInstance\\|^Constructor\\.newInstance\"";
    private static final String CONFIG_FILE_DIR = ".tamiflex";
    private static final String CONFIG_FILE_NAME = "poa.properties";
    
	private String[] runIDs = Config.runIDs.split(Utils.LIST_SEPARATOR);
	
    public void run() {
    	dumpConfig();
    	for (String runID : runIDs) {
    		if (Config.verbose >= 1) Messages.log(STARTING_RUN, runID);
    		List<String> playOutCmd = getPlayOutCmd();
    		String args = System.getProperty("chord.args." + runID, "");
            List<String> fullPoCmd = new ArrayList<String>(playOutCmd);
            fullPoCmd.addAll(Utils.tokenize(args));  
            deleteDirIfExists(PO_OUT);
            execCmd(fullPoCmd);
            
            File reflFile = new File(REFL_DATA_FILENAME);
            if (reflFile.length() != 0) {
	            String dstDirName = basename(Config.outDirName) + File.separator + getReflDstDirname(runID);
	            movePlayOutData();
	            createDstDirForRefl(dstDirName);
	            createFilteredReflFile();
	            List<String> boosterCmd = getBoosterCmd(dstDirName);
	            execCmd(boosterCmd);
	            Config.userClassPathName = Config.workDirName + File.separator + basename(Config.outDirName) +
		                   File.separator + getReflDstDirname(runIDs[0]);
	            Options.v().set_whole_program(false);
            } else {
            	if (Config.verbose >- 1) Messages.log("Empty reflection data - hence not running booster");
            	deleteDirIfExists(PO_OUT);
            }
            if (Config.verbose >= 1) Messages.log(FINISHED_RUN, runID);
    	}
        return;
    }
    
    private List<String> getPlayOutCmd() {
        String mainClassName = Config.mainClassName;
        assert (mainClassName != null);
        String classPathName = Config.userClassPathName;
        assert (classPathName != null);
        List<String> basecmd = new ArrayList<String>();
        basecmd.add("java");
        String jvmArgs = Config.runtimeJvmargs;
        basecmd.addAll(Utils.tokenize(jvmArgs));
        basecmd.add("-javaagent:" + Config.mainDirName + File.separator +"lib" + File.separator + PLAY_OUT_JAR);
        basecmd.add("-cp");
        basecmd.add(classPathName);
        basecmd.add(mainClassName);
        return basecmd;
    }
    
    private List<String> getBoosterCmd(String dstDirName) {
        String mainClassName = Config.mainClassName;
        assert (mainClassName != null);
        String classPathName = Config.userClassPathName;
        assert (classPathName != null);
        List<String> basecmd = new ArrayList<String>();
        basecmd.add("java");
        String jvmArgs = Config.runtimeJvmargs;
        basecmd.addAll(Utils.tokenize(jvmArgs));
        
        basecmd.add("-jar"); 
        basecmd.add(Config.mainDirName +File.separator + "lib" + File.separator + BOOSTER_JAR);
        basecmd.add("-p");
        basecmd.add("cg");
        basecmd.add("reflection-log:" + basename(Config.outDirName) + File.separator + REFL_DATA_FILTERED);
        basecmd.add("-cp");
        String stdlibClPath = System.getProperty("sun.boot.class.path");
        basecmd.add(stdlibClPath + File.pathSeparator + 
	             Config.toolClassPathName + File.pathSeparator + classPathName);
        basecmd.add("-d");
        basecmd.add(dstDirName);    
        basecmd.add(mainClassName);
        return basecmd;
    }
    
    private void execCmd(List<String> cmdList) {
    	 String cmdstr = "";
         for (String s : cmdList)
             cmdstr += s + " ";
    	if (Config.verbose >= 1) System.out.println(cmdstr);
    	
        int timeout = getTimeout();
        boolean haltOnErr = haltOnErr();   
        try {
            if (haltOnErr)
            	OutDirUtils.executeWithFailOnError(cmdList);
            else
            	OutDirUtils.executeWithWarnOnError(cmdList, timeout);
        } catch(Throwable t) { //just log exceptions
            t.printStackTrace();
        }       
        return;
    }
    
    private boolean haltOnErr() {
        return Config.dynamicHaltOnErr;
    }

    private int getTimeout() {
        return Config.dynamicTimeout;
    }

    private String getReflDstDirname(String runId) {
    	String dstDirName = REFL_DIRNAME_PREFIX + runId;
    	return dstDirName;
    }
    
    private void createDstDirForRefl(String dstDirName) {
    	File dstDir;
        dstDir = new File(Config.workDirName, dstDirName);
        if (!dstDir.exists())
            dstDir.mkdir(); 
        return;
    }
    
    private void movePlayOutData() {
    	File poOut;
    	poOut = new File(Config.workDirName + File.separator + PO_OUT);
    	File poOutMoved;
    	poOutMoved = new File(Config.workDirName + File.separator + basename(Config.outDirName) + File.separator + PO_OUT);
    	poOut.renameTo(poOutMoved);
    	deleteDirIfExists(PO_SCRATCH);
    	return;
    }
    
    private void createFilteredReflFile() {
    	List<String> basecmd = new ArrayList<String>();
    	basecmd.add("/bin/bash");
    	basecmd.add("-c");
    	basecmd.add("grep " + SUPPORTED_REFL + " " + basename(Config.outDirName) + File.separator + REFL_DATA_FILENAME +
    			    " > " + basename(Config.outDirName) + File.separator + REFL_DATA_FILTERED);
        execCmd(basecmd);
        return;
    }
    
    private void deleteDirIfExists(String dirName) {
    	File dir = new File(dirName);
    	if (dir.exists()) 
    		deleteDir(dir);
    }
    
    private void deleteDir(File file) {
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                deleteDir(f);
            }
        }
        file.delete();
    }
     
    private void dumpConfig() {
    	String cfgDir = System.getenv("HOME") + File.separator + CONFIG_FILE_DIR;
    	if (Utils.mkdirs(cfgDir)) { 
	    	PrintWriter pw = null;
	    	try {
	    		pw = new PrintWriter(new File(cfgDir, CONFIG_FILE_NAME));
	        } catch (FileNotFoundException ex) {
	            throw new RuntimeException(ex);
	        } 
	        pw.println("dontDumpClasses = false");
	    	pw.println("dontNormalize = true");
	    	pw.println("count = false");
	    	pw.println("useDeclaredTypes = false");
	    	pw.println("verbose = false");
	    	pw.println("transformations =\\");
	    	pw.println("de.bodden.tamiflex.playout.transformation.array.ArrayNewInstanceTransformation \\");
	    	pw.println("de.bodden.tamiflex.playout.transformation.clazz.ClassForNameTransformation \\");
	    	pw.println("de.bodden.tamiflex.playout.transformation.clazz.ClassNewInstanceTransformation \\");
	    	pw.println("de.bodden.tamiflex.playout.transformation.constructor.ConstructorNewInstanceTransformation \\");
	    	pw.close();
    	}
    }
    
    private String basename(String fname) {
    	String[] parts = fname.split(File.separator);
    	return parts[parts.length - 1];
    }
}
