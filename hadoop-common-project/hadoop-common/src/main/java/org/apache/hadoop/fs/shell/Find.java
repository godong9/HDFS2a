package org.apache.hadoop.fs.shell;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
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
		PathData[] items = PathData.expandAsGlob(item.toString(), getConf());
		processPaths(null, items);
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
