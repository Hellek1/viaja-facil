package eu.hellek.createstops.remote;

import java.net.URL;

import com.oreilly.servlet.HttpMessage;

public class DeleteEverything {


	public static void main(String[] args) throws Exception {
        if(RemoteConfig.confirm("Delete everything")) {
			URL url = new URL(RemoteConfig.getUrl());
			HttpMessage msg = new HttpMessage(url);
			String text = RemoteConfig.getCode() + "d";
			msg.sendPostMessage(text);
			System.out.println("Done sending request.");
        } else {
        	System.out.println("Doing nothing since you did not confirm.");
        }
	}

}
