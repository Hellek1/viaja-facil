package eu.hellek.viajafacil.android;

import java.util.List;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.Overlay;

import eu.hellek.gba.shared.SearchResultProxy;

/*
 * class to store everything to later restore it when ViajaFacilActivity gets destroyed and re-created on flipping the display
 */
public class StoredData {

	private GeoPoint marker1;
	private GeoPoint marker2;
	private GeoPoint mapcenter;
	private int zoomlevel;
	private List<SearchResultProxy> searchResults;
	private List<Overlay> resultOverlays;
	private boolean useSubte;
	private boolean useTrains;
	private int searchmode;
	private List<String> mlkSet1;
	private List<String> mlkSet2;
	private boolean resultsEnabled;
	private CharSequence btnSearchText;
	private String[] lastGeocodedAddr;
	
	public StoredData(GeoPoint marker1, GeoPoint marker2, GeoPoint mapcenter, int zoomlevel, List<SearchResultProxy> searchResults,
			List<Overlay> resultOverlays, boolean useSubte, boolean useTrains, int searchmode, List<String> mlkSet1,
			List<String> mlkSet2, boolean resultsEnabled, CharSequence btnSearchText, String[] lastGeocodedAddr) {
		this.marker1 = marker1;
		this.marker2 = marker2;
		this.mapcenter = mapcenter;
		this.zoomlevel = zoomlevel;
		this.searchResults = searchResults;
		this.resultOverlays = resultOverlays;
		this.useSubte = useSubte;
		this.useTrains = useTrains;
		this.searchmode = searchmode;
		this.mlkSet1 = mlkSet1;
		this.mlkSet2 = mlkSet2;
		this.resultsEnabled = resultsEnabled;
		this.btnSearchText = btnSearchText;
		this.lastGeocodedAddr = lastGeocodedAddr;
	}

	public GeoPoint getMarker1() {
		return marker1;
	}

	public GeoPoint getMarker2() {
		return marker2;
	}

	public GeoPoint getMapcenter() {
		return mapcenter;
	}

	public int getZoomlevel() {
		return zoomlevel;
	}

	public List<Overlay> getResultOverlays() {
		return resultOverlays;
	}

	public boolean isUseSubte() {
		return useSubte;
	}

	public boolean isUseTrains() {
		return useTrains;
	}

	public int getSearchmode() {
		return searchmode;
	}

	public List<String> getMlkSet1() {
		return mlkSet1;
	}

	public List<String> getMlkSet2() {
		return mlkSet2;
	}

	public List<SearchResultProxy> getSearchResults() {
		return searchResults;
	}

	public boolean isResultsEnabled() {
		return resultsEnabled;
	}

	public CharSequence getBtnSearchText() {
		return btnSearchText;
	}

	public String[] getLastGeocodedAddr() {
		return lastGeocodedAddr;
	}
	
}
