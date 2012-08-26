package org.apache.hadoop.fs.shell;

import java.io.IOException;
import java.util.LinkedList;

import org.apache.hadoop.fs.shell.PathExceptions.PathNotFoundException;

public class Find extends FsCommand {
	  
	  public static void registerCommands(CommandFactory factory) {
	    factory.addClass(Find.class, "-find");
	  }

	  public static final String NAME = "find";
	  public static final String USAGE = "[option] [path] [expression]";
	  public static final String DESCRIPTION = "Find command\n";

	  private boolean find = true;
	  private String flag;
	  private String[] expr;
	  
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
	    	 expr=new String[args.size()];
	    	 for(int i=0; i<(args.size()-1); i++){
	    		 expr[i] = args.get(i+1);
	    	 }
		     break;
	      default:
	        throw new IllegalArgumentException("Only one find flag is allowed");
	    }
	  }

	  @Override
	  protected void processPath(PathData item) throws IOException {
	    

	   if(flag.equals("name")){
	        System.out.println(item);
	        System.out.println(expr[0]);
	        System.out.println(expr[1]);
	        System.out.println(String.format("Find name"));
	        exitCode = 1;
	    }
	    else if(flag.equals("maxdepth")){
	        System.out.println(item);
	        System.out.println(String.format("Find maxdepth"));
	        exitCode = 1;
	    }
	    else if(flag.equals("size")){
	        System.out.println(String.format("Find size"));
	        exitCode = 1;
	    }
	    else{
	    	find=false;
	    }
	    if (!find) exitCode = 1;
	  }
	  
	  @Override
	  protected void processNonexistentPath(PathData item) throws IOException {
		 if(!find){
			 displayError(new PathNotFoundException(item.toString()));
			 exitCode = 1;
		 }
	  }
	
}
