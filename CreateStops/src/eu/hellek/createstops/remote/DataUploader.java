package eu.hellek.createstops.remote;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URL;

import com.oreilly.servlet.HttpMessage;

import eu.hellek.createstops.data.TxtFilenameFilter;

public class DataUploader {

	private static final int startIndex = 0; // index of first file, i.e. 0 then 50 then 100
	private static final int maxFiles = 500;

	public static void main(String[] args) throws Exception {
		if(RemoteConfig.confirm("Upload data starting with index " + startIndex + " and a maximum of " + maxFiles + " entries")) {		
			
			int mode = 2;
			
			if(mode == 0) {
				task("tren");
			} else if(mode == 1) {
				task("subte");
			} else if(mode == 2) {
				task("bus");
			} else if(mode == 10) {
				task("tren");
				task("subte");
			} else if(mode == 99) {
				task("tren");
				task("subte");
				task("bus");
			} else {
				System.out.println("No valid mode selected.");
			}
			
		} else {
			System.out.println("Doing nothing since you did not confirm.");
		}
	}

	private static void task(String directory) throws Exception {
		URL url = new URL(RemoteConfig.getUrl());
		HttpMessage msg = new HttpMessage(url);

		File dir = new File("out/");
		int counter = 0;
		int handledCounter = 0;
		if(dir.isDirectory()) {
			File [] dirs = dir.listFiles();
			for(File d: dirs) {
				if(d.isDirectory() && d.getName().contains(directory)) {
					File [] files = d.listFiles(new TxtFilenameFilter());
					for(int i = 0; i < files.length && counter < maxFiles+startIndex; i++) {
						if(counter >= startIndex) {
							File f = files[i];
							System.out.println(counter + ": " + f.getName());
							String text = RemoteConfig.getCode() + "a";
							BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f))); 
							StringBuffer contentOfFile = new StringBuffer();
							String line; 
							while ((line = br.readLine()) != null) {
								contentOfFile.append(line);
							}
							String content = contentOfFile.toString();
							text += content;
//							System.out.println(text);
							msg.sendPostMessage(text);
							handledCounter++;
						}
						counter++;
					}
				}
			}
		}
		System.out.println("-----\nUploaded " + handledCounter + " files. The index of the last file was " + (counter-1));
	}
}

