package eu.hellek.gba.client.pub.common;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.maps.client.HasMap;
import com.google.gwt.maps.client.HasMapOptions;
import com.google.gwt.maps.client.base.HasLatLng;
import com.google.gwt.maps.client.base.HasLatLngBounds;
import com.google.gwt.maps.client.overlay.HasProjection;
import com.google.gwt.user.client.Element;

public class NullMap implements HasMap {
	
	private static NullMap instance;

	private NullMap() { }
	
	public static NullMap getInstance() {
		if(instance == null) {
			instance = new NullMap();
		}
		return instance;
	}
	
	@Override
	public JavaScriptObject getJso() {
		return getNullJso();
	}
	
	private native JavaScriptObject getNullJso() /*-{
		return null;
	}-*/;

	@Override
	public void fitBounds(HasLatLngBounds bounds) {
		// do nothing
	}

	@Override
	public HasLatLngBounds getBounds() {
		// do nothing
		return null;
	}

	@Override
	public HasLatLng getCenter() {
		// do nothing
		return null;
	}

	@Override
	public Element getDiv() {
		// do nothing
		return null;
	}

	@Override
	public String getMapTypeId() {
		// do nothing
		return null;
	}

	@Override
	public HasProjection getProjection() {
		// do nothing
		return null;
	}

	@Override
	public int getZoom() {
		// do nothing
		return 0;
	}

	@Override
	public void panBy(int x, int y) {
		// do nothing
	}

	@Override
	public void panTo(HasLatLng latLng) {
		// do nothing
	}

	@Override
	public void panToBounds(HasLatLngBounds bounds) {
		// do nothing
	}

	@Override
	public void setCenter(HasLatLng latLng) {
		// do nothing
	}

	@Override
	public void setMapTypeId(String mapTypeId) {
		// do nothing
	}

	@Override
	public void setOptions(HasMapOptions options) {
		// do nothing
	}

	@Override
	public void setZoom(int zoom) {
		// do nothing
	}

}
