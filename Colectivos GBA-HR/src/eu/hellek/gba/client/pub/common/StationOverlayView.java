package eu.hellek.gba.client.pub.common;

import com.google.gwt.maps.client.base.HasLatLng;
import com.google.gwt.maps.client.base.HasPoint;
import com.google.gwt.maps.client.overlay.OverlayView;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Label;

public class StationOverlayView extends OverlayView {
	
	private HasLatLng latlng;
	private Label div;
	private int minimumZoom;
	
	public StationOverlayView(String text, HasLatLng pos, String color, int minimumZoom) {
		super();
		this.latlng = pos;
		div = new Label("° " + text);
		div.setStyleName("stationLabel");
		div.getElement().setAttribute("style", "color:" + color + ";");
		div.setVisible(false);
		this.minimumZoom = minimumZoom;
	}

	@Override
	public void draw() {
		if(this.getMap().getZoom() >= minimumZoom) {
			HasPoint p = this.getProjection().fromLatLngToDivPixel(latlng);
			int x = (int)Math.round(p.getX()) - 2;
			int y = (int)Math.round(p.getY()) - 2;
			//GWT.log("should display station label " + title + " at " + x + ", " + y);
	    	DOM.setStyleAttribute(div.getElement(), "left", x+"px");
	    	DOM.setStyleAttribute(div.getElement(), "top", y+"px");
	    	div.setVisible(true);
	    } else {
	    	div.setVisible(false);
	    }
	}

	@Override
	public void onAdd() {
		this.getPanes().getOverlayImage().appendChild(div.getElement());
	}

	@Override
	public void onRemove() {
		div.setVisible(false);
//		this.getPanes().getOverlayImage().removeChild(div.getElement());
		div.getElement().removeFromParent();
	}

}
