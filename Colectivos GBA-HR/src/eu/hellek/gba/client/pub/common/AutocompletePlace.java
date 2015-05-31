package eu.hellek.gba.client.pub.common;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.maps.client.HasJso;
import com.google.gwt.maps.client.base.LatLng;

public class AutocompletePlace implements HasJso {
	
	JavaScriptObject jso;

	public AutocompletePlace(String textboxId) {
		jso = addAutoComplete(textboxId);
	}
	
	@Override
	public JavaScriptObject getJso() {
		return jso;
	}
	
	public LatLng getLatLng() {
		return new LatLng(getPlaceLat(this.jso), getPlaceLon(this.jso));
	}
	
	private native JavaScriptObject addAutoComplete(String textboxId) /*-{
	   var defaultBounds = new $wnd.google.maps.LatLngBounds(
			new $wnd.google.maps.LatLng(-35.1, -59.25),
			new $wnd.google.maps.LatLng(-34.05, -57.8));

	  var input = $doc.getElementById(textboxId);
	  var options = {
		bounds: defaultBounds
	  };
	  return new $wnd.google.maps.places.Autocomplete(input, options);
	}-*/;

	private native double getPlaceLat(JavaScriptObject jso) /*-{
	  var place = jso.getPlace();
	  return place.geometry.location.lat();
	}-*/;
	
	private native double getPlaceLon(JavaScriptObject jso) /*-{
	  var place = jso.getPlace();
	  return place.geometry.location.lng();
	}-*/;
	
}
