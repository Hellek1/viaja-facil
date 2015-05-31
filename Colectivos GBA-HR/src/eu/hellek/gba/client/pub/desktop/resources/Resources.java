package eu.hellek.gba.client.pub.desktop.resources;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface Resources extends ClientBundle {
	
	@Source("red_MarkerA_circle.png")
	ImageResource MarkerACircle();
	
	@Source("red_MarkerB_circle.png")
	ImageResource MarkerBCircle();
	
	@Source("ar.png")
	ImageResource languageEsp();
	
	@Source("us.png")
	ImageResource languageEng();
	
	@Source("stern-weiss.png")
	ImageResource starOutline();
	
	@Source("stern-gelb.png")
	ImageResource starFull();
	
	@Source("pfeil-unten.png")
	ImageResource arrowDown();

}
