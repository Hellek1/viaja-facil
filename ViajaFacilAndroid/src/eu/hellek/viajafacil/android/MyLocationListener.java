package eu.hellek.viajafacil.android;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

/*
 * callbacks for the GPS and network-location services. Stores the best location so that the app can access it later
 */
public class MyLocationListener implements LocationListener {
	
//	private Context context;
	private Location bestLocation;
	
	public MyLocationListener(Context context) {
//		this.context = context;
	}

	@Override
	public void onLocationChanged(Location location) {
/*		int lat = (int) (location.getLatitude());
		int lng = (int) (location.getLongitude());
		System.out.println("Lat: " + lat + " Lon: " + lng);*/
		if(bestLocation == null || location.hasAccuracy() && ((location.getAccuracy() < bestLocation.getAccuracy() || !bestLocation.hasAccuracy()) || location.getAccuracy() < 80) ) {
			bestLocation = location;
		}
	}

	@Override
	public void onProviderDisabled(String provider) {
//		Toast.makeText(context, "Disenabled provider " + provider, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onProviderEnabled(String provider) {
//		Toast.makeText(context, "Enabled new provider " + provider, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
//		Toast.makeText(context, "Status changed: " + provider + ": status " + status, Toast.LENGTH_SHORT).show();
	}

	public Location getBestLocation() {
		return bestLocation;
	}

}
