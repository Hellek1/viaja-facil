package eu.hellek.createstops.data;

import java.io.File;
import java.io.FilenameFilter;

public class TxtFilenameFilter implements FilenameFilter { 
	  @Override public boolean accept( File f, String s ) { 
	    return new File(f, s).isFile() && 
	           s.toLowerCase().endsWith( ".txt" ); 
	  } 
	}