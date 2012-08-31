package org.apache.hadoop.fs.shell;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.lang.Boolean;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.shell.PathExceptions.PathNotFoundException;
import org.apache.hadoop.util.StringUtils;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.GlobFilter;

public class Find extends FsCommand {
	  
	  public static void registerCommands(CommandFactory factory) {
	    factory.addClass(Find.class, "-find");
	  }

	  public static final String NAME = "find";
	  public static final String USAGE = "[path...] [expression]";
	  public static final String DESCRIPTION = "Find command\n";

	  private String flag;
	  private int preDepth=0;
  
	  protected int maxRepl = 3, maxLen = 10, maxOwner = 0, maxGroup = 0;
	  protected String lineFormat;
	  protected boolean humanReadable = false;
	  protected Map<String, String> optionsForFind = new HashMap<String, String>();
	  
	  protected static final SimpleDateFormat dateFormat = 
			    new SimpleDateFormat("yyyy-MM-dd HH:mm");  
	  protected String formatSize(long size) {
		    return humanReadable
		      ? StringUtils.humanReadableInt(size)
		      : String.valueOf(size);
	  }
	  
	  @Override
	  protected void processOptions(LinkedList<String> args) {
		CommandFormat cf = new CommandFormat(1, Integer.MAX_VALUE, "name", "type", "atime", "ctime", "mtime", 
										"print", "depth", "owner", "group", "perm", "maxdepth", "size");
		cf.parseForFind(args);
		optionsForFind = cf.getOptionsForFind();
		
		if (args.isEmpty()) 
			args.add(Path.CUR_DIR);
		
		String argPath = args.toString();
		StringTokenizer stk = new StringTokenizer(argPath,"/");
		preDepth=stk.countTokens()-1;
	
		setRecursive(true);
	  }
	  
	  @Override
	  protected void processPathArgument(PathData item) throws IOException {
	    // implicitly recurse once for cmdline directories
	    if (item.stat.isDirectory()) {	
	      recursePath(item);	//If recurse possible, recursePath set
	    } else {
	      super.processPathArgument(item);
	    }
	  }
	  
	  @Override
	  protected void processPaths(PathData parent, PathData ... items)
	  throws IOException {
	    adjustColumnWidths(items);
	    super.processPaths(parent, items);
	  }
	    
	  @Override
	  protected void processPath(PathData item) throws IOException {
		
		boolean findFlag = true;
		FileStatus stat = item.stat;
		boolean isDirectory = false;
		boolean isFile = false;
		String fileType;
		String filePermission = stat.getPermission().toString();
		String fileOwner = stat.getOwner();
		String fileGroup = stat.getGroup();
		long mTime = stat.getModificationTime();

		if( stat.isDirectory() )
		{
			fileType = "d";
			isDirectory = true;
		}
		else if( stat.isFile() )
		{
			fileType = "f";
			isFile = true;
		}
		else
			fileType = "-";

		if(findFlag && optionsForFind.containsKey("name")){
			String optString = optionsForFind.get("name");
			// HDFS 에서 제공하는 패턴 체크 함수로 비교
			GlobFilter fp = new GlobFilter(optString);
			if( fp.accept(item.path) )
				findFlag = findFlag && true;
			else
				findFlag = false;
		}

		if(findFlag && optionsForFind.containsKey("type")){
			String optString = optionsForFind.get("type");
			if( optString.equals(fileType) )
				findFlag = findFlag && true;
			else
				findFlag = false;
		}

		if(findFlag && optionsForFind.containsKey("atime")){
			exitCode = 0;
		}

		if(findFlag && optionsForFind.containsKey("owner")){
			String optString = optionsForFind.get("owner");
			if( optString.equals(fileOwner) )
				findFlag = findFlag && true;
			else
				findFlag = false;
		}

		if(findFlag && optionsForFind.containsKey("group")){
			String optString = optionsForFind.get("group");
			if( optString.equals(fileGroup) )
				findFlag = findFlag && true;
			else
				findFlag = false;
		}

		if(findFlag && optionsForFind.containsKey("perm")){
			String optString = optionsForFind.get("perm");
			if( filePermission.indexOf(optString) != -1 )
				findFlag = findFlag && true;
			else
				findFlag = false;
		}

		if(findFlag && optionsForFind.containsKey("maxdepth")){
	    	int setDepth=new Integer(optionsForFind.get("maxdepth"));
	    	//System.out.println("setDepth: "+setDepth);
	    	String tmpPath = item.toString();
	    	
	    	//Count depth
	    	StringTokenizer stk = new StringTokenizer(tmpPath,"/");
	    	int tmpDepth=stk.countTokens();	

	    	//Add tmpDepth
	    	tmpDepth=tmpDepth+preDepth-1;	
	    	//System.out.println("tmpDepth: "+tmpDepth);
	    	
	    	String tmpString = item.toString(); 	       
		    int tmpNum = tmpString.lastIndexOf("/");
		    tmpString = tmpString.substring(tmpNum+1);       
		    //Java Regular Expression Matching

		    //Pattern pt = Pattern.compile(optString);
		    //Matcher m = pt.matcher(tmpString);
	    	
	    	if(tmpDepth<=setDepth)
				findFlag = findFlag && true;
			else
				findFlag = false;
	    }

		if(findFlag && optionsForFind.containsKey("size")){
			System.out.println(String.format("Find size"));
			exitCode = 0;
		}
		
		// 사용자가 작성한 옵션이 없을때
		if(optionsForFind.isEmpty())
			findFlag = true;

		if( findFlag )
		{
			String line = String.format(lineFormat,
				(isDirectory ? "d" : "-"),
				filePermission,
				(isFile ? stat.getReplication() : "-"),
				fileOwner,
				fileGroup,
				formatSize(stat.getLen()),
				dateFormat.format(new Date(mTime)),
				item
			);
			System.out.println(line);
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
