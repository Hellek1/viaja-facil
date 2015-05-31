package eu.hellek.viajafacil.android.map;

import android.content.Context;

import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

/*
 * overlay that displays a blue point at users position 
 */
public class MeinLocationOverlay extends MyLocationOverlay {

	public MeinLocationOverlay(Context context, MapView mapView) {
		super(context, mapView);
	}

}
