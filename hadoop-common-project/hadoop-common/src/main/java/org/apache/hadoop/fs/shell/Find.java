package org.apache.hadoop.fs.shell;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.hadoop.util.StringUtils;

import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.classification.InterfaceStability;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;

public class Find extends FsCommand {
	  
	  public static void registerCommands(CommandFactory factory) {
	    factory.addClass(Find.class, "-find");
	  }

	  public static final String NAME = "find";
	  public static final String USAGE = "[path...] [expression]";
	  public static final String DESCRIPTION = "Find command\n";

	  protected int maxRepl = 3, maxLen = 10, maxOwner = 0, maxGroup = 0;
	  protected String lineFormat;
	  protected boolean dirRecurse;
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
		CommandFormat cf = new CommandFormat(1, Integer.MAX_VALUE, "name", "type", "atime", "ctime", "mtime", "print", "depth", "owner", "group", "perm", "maxdepth", "size");
		cf.parseForFind(args);
		optionsForFind = cf.getOptionsForFind();
		if (args.isEmpty()) args.add(Path.CUR_DIR);
	  }

	  @Override
	  protected void processPathArgument(PathData item) throws IOException {
		String patten;
		ArrayList<PathData> tmpPathData = new ArrayList<PathData>();
		PathData[] items;

		if ( optionsForFind.containsKey("name") )
		{
			System.out.println("nameOption : " + optionsForFind.get("name"));
			items = PathData.expandAsGlob(optionsForFind.get("name"), getConf());
			for (PathData i : items) {
				System.out.println("Not Matches items : " + i.toString());
				if( i.toString().matches( "^" + item.toString() ) )
				{
					System.out.println("Matches items : " + i.toString());
					tmpPathData.add(i);
				}
			}
			/*
			// 경로가 디렉토리일 경우, 디렉토리 안에서 파일 검색을 한다.
			if( item.stat.isDirectory() )
			{
				patten = item.toString() + "/" + optionsForFind.get("name");
				System.out.println("patten : " + patten);
				items = PathData.expandAsGlob(patten, getConf());
			}
			// 경로가 디렉토리가 아닐 경우, 경로와 찾을려는 문자열과 비교한다.
			else if( item.toString().matches(optionsForFind.get("name")) )
				items[0] = item;
			// 경로가 디렉토리가 아니고, 디렉토리가 아니면서 찾으려는 문자열과 다르다면 찾지 못하였다.
			else
				return;
			*/
		}
		else
		{
			tmpPathData.add(item);
		}

		items = tmpPathData.toArray(new PathData[tmpPathData.size()]);

		if( items.length > 0 )
	    {
			for (PathData it : items) {
				System.out.println("items : " + it.toString());
			}
		}

		processPaths(null, items);
	  }

	  @Override
	  protected void processPaths(PathData parent, PathData ... items)
	  throws IOException {
		if (items.length != 0) {
		  out.println("Found " + items.length + " items");
		}
		adjustColumnWidths(items);

		if ( optionsForFind.containsKey("name") )
		{
			for (PathData item : items) {
				processPath(item);
			}
		}
		else
		{
			recursive = true;
			super.processPaths(parent, items);
		}
	  }
	  
	  @Override
	  protected void processPath(PathData item) throws IOException {
		FileStatus stat = item.stat;
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
		out.println(line);
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
