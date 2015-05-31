package eu.hellek.createstops.local;

import java.io.File;

public class ListLinesInHtml {

	public static void main(String[] args) {
		String basePath = "out/"; 
		String[] types = {"bus" , "subte", "tren"};
//		int mode = 2; // 0 = bus, 1 = subte, 2 = tren
		for(int mode = 0; mode <= 2; mode++) {
			String path = basePath + types[mode] + "/";
			new ListLinesInHtml(path, types[mode]);
		}
	}
	
	public ListLinesInHtml(String path, String type) {
		File dir = new File(path);
		if(dir.isDirectory()) {
			File [] files = dir.listFiles();
//			System.out.println("Found " + files.length + " files.");
			for(File f : files) {
				if(!f.isDirectory()) {
					String name = f.getName().substring(0, f.getName().length() - 4);
					if(name.contains("XshortX ")) {
						name =name.replace("XshortX ", "");
					}
					System.out.println(name + "<br/>");
				}
			}
		}
	}

}
