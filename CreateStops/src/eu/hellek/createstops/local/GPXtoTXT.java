package eu.hellek.createstops.local;

import java.io.File;
import java.util.regex.Pattern;

import eu.hellek.createstops.helpers.GpxParser;

public class GPXtoTXT {

	public static void main(String[] args) {
		String basePath = "res/"; 
		String[] types = {"bus" , "subte", "tren"};
//		int mode = 2; // 0 = bus, 1 = subte, 2 = tren
		for(int mode = 0; mode <= 2; mode++) {
			String path = basePath + types[mode] + "/";
			new GPXtoTXT(path, types[mode]);
		}
	}
	
	public GPXtoTXT(String path, String type) {
		File dir = new File(path);
		if(dir.isDirectory()) {
			File [] files = dir.listFiles();
			System.out.println("Found " + files.length + " files.");
			for(File f : files) {
				if(!f.isDirectory()) {
					System.out.println(f.getName());
					String[] parts = f.getName().split(Pattern.quote("."));
					GpxParser gp = new GpxParser(parts[0],type);
					gp.read(f.toString());
					System.out.println("Wrote " + gp.getTotalPoints() + " points.");
				}
			}
		}
	}

}
