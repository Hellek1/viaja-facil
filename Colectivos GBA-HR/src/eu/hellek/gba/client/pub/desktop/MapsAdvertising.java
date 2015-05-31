package eu.hellek.gba.client.pub.desktop;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.maps.client.HasJso;

public class MapsAdvertising implements HasJso {
	
	JavaScriptObject jso;

	public MapsAdvertising(JavaScriptObject mapJso) {
		jso = addAdvertising(mapJso);
	}
	
	@Override
	public JavaScriptObject getJso() {
		return jso;
	}
	
	private native JavaScriptObject addAdvertising(JavaScriptObject mapJso) /*-{
		var adUnitOptions = {
    		format: $wnd.google.maps.adsense.AdFormat.LEADERBOARD,
    		position: $wnd.google.maps.ControlPosition.BOTTOM,
    		map: mapJso,
    		visible: true,
    		publisherId: 'replace me'
  		}

	  var adUnitDiv = $doc.createElement('div');
	  return new $wnd.google.maps.adsense.AdUnit(adUnitDiv, adUnitOptions);
	}-*/;
	
}
