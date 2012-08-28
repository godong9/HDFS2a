package org.apache.hadoop.fs.shell;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.StringTokenizer;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.shell.PathExceptions.PathNotFoundException;
import org.apache.hadoop.util.StringUtils;

public class Find extends FsCommand {
	  
	  public static void registerCommands(CommandFactory factory) {
	    factory.addClass(Find.class, "-find");
	  }

	  public static final String NAME = "find";
	  public static final String USAGE = "[option] [path] [filename] [option2]";
	  public static final String DESCRIPTION = "Find command\n";

	  private boolean findflag = true;
	  private String flag;
	  private int argCnt;
	  private int preDepth=0;
	  private String[] expr;
	  protected int maxRepl = 3, maxLen = 10, maxOwner = 0, maxGroup = 0;
	  protected String lineFormat;
	  protected boolean dirRecurse;
	  protected boolean humanReadable = false;
	  protected static final SimpleDateFormat dateFormat = 
			    new SimpleDateFormat("yyyy-MM-dd HH:mm");
	  
	  protected String formatSize(long size) {
		    return humanReadable
		      ? StringUtils.humanReadableInt(size)
		      : String.valueOf(size);
	  }
	  
	  @Override
	  protected void processOptions(LinkedList<String> args) {
		 CommandFormat cf = new CommandFormat(1, Integer.MAX_VALUE, "name", "maxdepth", "size");
	    cf.parse(args); 
	    String[] opts = cf.getOpts().toArray(new String[0]);
	  
	    switch (opts.length) {
	      case 0:
	        throw new IllegalArgumentException("No find flag given");
	      case 1:
	    	 flag = opts[0];
	    	 dirRecurse = !cf.getOpt("d");	//Recurse Check
	    	 setRecursive(dirRecurse);
	    	 argCnt=args.size()-1;
	    	 
	    	 String argPath = args.get(0);
	    	 
	    	 StringTokenizer stk = new StringTokenizer(argPath,"/");
	    	 preDepth=stk.countTokens();	
	    	 
	    	 expr=new String[argCnt];
	    	 for(int i=0; i<(args.size()-1); i++){
	    		 expr[i] = args.get(i+1);
	    	 }
		     break;
	      default:
	        throw new IllegalArgumentException("Only one find flag is allowed");
	    }
	  }
	  
	  @Override
	  protected void processPathArgument(PathData item) throws IOException {
	    // implicitly recurse once for cmdline directories
	    if (dirRecurse && item.stat.isDirectory()) {	
	      recursePath(item);	//If recurse possible, recursePath set
	    } else {
	      super.processPathArgument(item);
	    }
	  }
	  
	  @Override
	  protected void processPaths(PathData parent, PathData ... items)
	  throws IOException {
	    if (!isRecursive() && items.length != 0) {
	      out.println("Found " + items.length + " items");
	    }
	    adjustColumnWidths(items);
	    super.processPaths(parent, items);
	  }
	  
	  @Override
	  protected void processPath(PathData item) throws IOException {
	   if(flag.equals("name")){
		   FileStatus stat = item.stat;
			   if(item.toString().contains(expr[0])){
			    	String line = String.format(lineFormat,
			    	        (stat.isDirectory() ? "d" : "-"),
			    	        stat.getPermission(),
			    	        (stat.isFile() ? stat.getReplication() : "-"),
			    	        stat.getOwner(),
			    	        stat.getGroup(),
			    	        formatSize(stat.getLen()),
			    	        dateFormat.format(new Date(stat.getModificationTime())),
			    	        item
			    	   );
			    	   System.out.println(line);
			    }
	       exitCode = 1;
	    }
	    else if(flag.equals("maxdepth")){
	    	FileStatus stat = item.stat;
	    	int setDepth=new Integer(expr[1]);
	    	String tmpPath = item.toString();
	    	
	    	//Count depth
	    	StringTokenizer stk = new StringTokenizer(tmpPath,"/");
	    	int tmpDepth=stk.countTokens();	

	    	//Add preDepth
	    	setDepth=setDepth+preDepth;	
	    	
	    	if(tmpDepth<=setDepth){	
			   if(item.toString().contains(expr[0])){
				   //System.out.println("item: "+item);
			    	String line = String.format(lineFormat,
			    	        (stat.isDirectory() ? "d" : "-"),
			    	        stat.getPermission(),
			    	        (stat.isFile() ? stat.getReplication() : "-"),
			    	        stat.getOwner(),
			    	        stat.getGroup(),
			    	        formatSize(stat.getLen()),
			    	        dateFormat.format(new Date(stat.getModificationTime())),
			    	        item
			    	   );
			    	   System.out.println(line);
			    }
	    	}
	       exitCode = 1;
	    }
	    else if(flag.equals("size")){
	        System.out.println(String.format("Find size"));
	        exitCode = 1;
	    }
	    else{
	    	System.out.println(String.format("Flag Input Error!"));
	    	findflag=false;
	    }
	    if (!findflag) exitCode = 1;
	  }

	  @Override
	  protected void processNonexistentPath(PathData item) throws IOException {
		 if(!findflag){
			 displayError(new PathNotFoundException(item.toString()));
			 exitCode = 1;
		 }
	  }
	  
	  private void adjustColumnWidths(PathData items[]) {
		    for (PathData item : items) {
		      FileStatus stat = item.stat;
		      maxRepl  = maxLength(maxRepl, stat.getReplication());
		      maxLen   = maxLength(maxLen, stat.getLen());
		      maxOwner = maxLength(maxOwner, stat.getOwner());
		      maxGroup = maxLength(maxGroup, stat.getGroup());
		    }

		    StringBuilder fmt = new StringBuilder();
		    fmt.append("%s%s "); // permission string
		    fmt.append("%"  + maxRepl  + "s ");
		    // Do not use '%-0s' as a formatting conversion, since it will throw a
		    // a MissingFormatWidthException if it is used in String.format().
		    // http://docs.oracle.com/javase/1.5.0/docs/api/java/util/Formatter.html#intFlags
		    fmt.append((maxOwner > 0) ? "%-" + maxOwner + "s " : "%s");
		    fmt.append((maxGroup > 0) ? "%-" + maxGroup + "s " : "%s");
		    fmt.append("%"  + maxLen   + "s ");
		    fmt.append("%s %s"); // mod time & path
		    lineFormat = fmt.toString();
	  }
	  private int maxLength(int n, Object value) {
		    return Math.max(n, (value != null) ? String.valueOf(value).length() : 0);
	  }
	
}