package eu.hellek.viajafacil.android;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.protobuf.CodedOutputStream;

import eu.hellek.gba.proto.Helpers;
import eu.hellek.gba.proto.LoginInfoProtos.LoginInfo;
import eu.hellek.gba.proto.LoginInfoProtos.LoginInfo.UserFavouritePositionProxy;
import eu.hellek.gba.proto.RequestsProtos.DirectSearchRequest;
import eu.hellek.gba.proto.RequestsProtos.IndirectSearchRequest;
import eu.hellek.gba.shared.ConnectionProxy;
import eu.hellek.gba.shared.ConnectionProxyComparator;
import eu.hellek.gba.shared.LineProxy;
import eu.hellek.gba.shared.SearchResultProxy;
import eu.hellek.viajafacil.android.map.LineOverlay;
import eu.hellek.viajafacil.android.map.MapGestureDetectorOverlay;
import eu.hellek.viajafacil.android.map.MarkerCircleOverlay;
import eu.hellek.viajafacil.android.map.MarkersOverlay;
import eu.hellek.viajafacil.android.map.MeinLocationOverlay;
import eu.hellek.viajafacil.android.map.MyOnGestureListener;
import eu.hellek.viajafacil.android.map.StationOverlay;

public class ViajaFacilActivity extends MapActivity {
	
	static final int DIALOG_SELECTFROM = 0;
	static final int DIALOG_SELECTTO = 1;
	public static final int DIALOG_FROMMAP = 2;
	static final int DIALOG_CHOOSEADDR = 3;
	static final int DIALOG_AB_NEEDED = 4;
	static final int DIALOG_NO_LOCATION_YET = 5;
	static final int DIALOG_ADDRESS_GEOCODING_FAILED = 6;
	static final int DIALOG_EXIT_APP = 7;
	
	public static final int FROM = 0;
	public static final int TO = 1;
	
	public static final String[] colors = { "#ff0000", "#0000ff", "#009900", "#ff00ff", "#00ffff" };
	public static final int NUM_COLORS = colors.length;
	
	public static final int GET_RESULT_TO_DISPLAY = 0;
	public static final int GET_ADDR_FROM_CONTACTS = 1;
	public static final int GET_FAVORITE = 2;
	public static final int GET_ACCOUNTS_THEN_SHOW_FAVS = 3;
	public static final int ACCOUNT_MANAGER_INTENT = 4;
	
	private static final String appurl = "www.viaja-facil.com"; // URL used for direct-search. might also use the SSL url below if desired 
	public static final String appurlssl = "viaja-facil.appspot.com"; // URL used to communicate in SSL-mode for login and adding/deleting favorites, also for indirect searches since they take so long that a few ms more due to overhead really don't matter
	/*private static final String appurl = "10.0.2.2:8888";
	private static final String appurlssl = appurl;*/
	
	/*
	 * stuff related to the login-cookie
	 */
	private static final String PREFS_COOKIE_PREFS = "cookieprefs";
	private static final String PREFS_COOKIE_NAME = "cookiename";
	private static final String PREFS_COOKIE_VALUE = "cookievalue";
	private static final String PREFS_COOKIE_TIME = "cookietime";
	
	public static final String PREFS_ACCOUNT_PREFS = "accountprefs";
	public static final String PREFS_ACCOUNT_NAME = "accountname";
	
	private LocationManager locationManager;
	private MyLocationListener locationListener; // location listener to detect users position
	private MeinLocationOverlay mlo; // overlay to display position
	private MapView mapView;
	private Menu menu;
	private MarkersOverlay markers; // overlay to display start/end points (A/B)
	private List<Overlay> resultOverlays; // overlay that contains the results (displayed connection), mainly used to be able to remove it before drawing a new result
	public static Cookie appEngineCookie;
	
	private int mapLongTouchX;
	private int mapLongTouchY;
	
	private boolean useSubte = true; // whether the user desires to use subtes
	private boolean useTrains = true; // same for trains
	
	private int searchmode; // direct or indirect search
	private List<String> mlkSet1 = null; // keys of lines in results of direct search. they are ignored in indirect search
	private List<String> mlkSet2 = null;
	
	private String [] lastGeocodedAddr = { "", "" };
	private List<Address> addressesToChooseFrom;
	private int chooseAddressFromOrTo;
	private int chooseContactFromOrTo;
	private int chooseFavoriteFromOrTo;
	public static boolean downloadedFavs = false;
	public static int getFavsReRuns = 0; // how many retries where done to fetch the favs. needs to be limited to avoid endless loop
	protected boolean doSearchAfterwards;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        TOS.show(this);
        
        mapView = (MapView) findViewById(R.id.mapWidget);
        mapView.setBuiltInZoomControls(true);
        mapView.setLongClickable(true);
        mapView.setStreetView(false);
        
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationListener = new MyLocationListener(this);
		updateLocation();
		
		MapController mapController = mapView.getController();
		mapController.setZoom(11);
		mapController.setCenter(new GeoPoint(((int)(-34.604389 * 1E6)), ((int)(-58.410873 * 1E6))));
        List<Overlay> mapOverlays = mapView.getOverlays();
        mapOverlays.clear();
        mapOverlays.add(new MapGestureDetectorOverlay(new MyOnGestureListener(this)));
        markers = new MarkersOverlay(this);
        mapOverlays.add(markers);

        mlo = new MeinLocationOverlay(this, mapView);
        mapOverlays.add(mlo);
        //mlo.enableCompass();
		mlo.enableMyLocation();
		
		resultOverlays = new ArrayList<Overlay>();
		
		OnClickListener btnListener = new ButtonListener();
		ImageButton btnFrom = (ImageButton) findViewById(R.id.btnFrom);
		btnFrom.setOnClickListener(btnListener);
		ImageButton btnTo = (ImageButton) findViewById(R.id.btnTo);
		btnTo.setOnClickListener(btnListener);
		Button btnSearch = (Button) findViewById(R.id.btnSearch);
		btnSearch.setOnClickListener(btnListener);
		Button btnResults = (Button) findViewById(R.id.btnResults);
		btnResults.setOnClickListener(btnListener);
		
		TextView txtFrom = (TextView)findViewById(R.id.txtFrom);
		TextView txtTo = (TextView)findViewById(R.id.txtTo);
		TextViewListener listenerFrom = new TextViewListener(FROM);
		TextViewListener listenerTo = new TextViewListener(TO);
		txtFrom.setOnEditorActionListener(listenerFrom);
		txtFrom.setOnFocusChangeListener(listenerFrom);
		txtFrom.setOnKeyListener(listenerFrom);
		txtTo.setOnEditorActionListener(listenerTo);
		txtTo.setOnFocusChangeListener(listenerTo);
		txtTo.setOnKeyListener(listenerTo);
		
		Intent intent = getIntent();
        if(intent != null) {
        	/*
        	 * handling of Android intents, i.e. when Viaja Facil was opened through "share" on a location in Google Maps, etc.
        	 */
        	double lat = 0.0;
        	double lon = 0.0;
        	String addr = null;
        	if(intent.getData() != null) {
        		/*System.err.println("host: " + intent.getData().getHost());
            	System.err.println("data: " + intent.getDataString());*/
	        	if(intent.getScheme().equals("geo")) {
	        		String [] split1 = intent.getDataString().split(":");
	        		String [] split2 = split1[1].split("?");
	        		String [] reslatlon = split2[0].split(",");
	        		lat = Double.parseDouble(reslatlon[0]);
	        		lon = Double.parseDouble(reslatlon[1]);
	        		if(lat == 0 && lon == 0) {
	        			addr = intent.getData().getQueryParameter("q");
	        		}
	        	} else if(intent.getScheme().equals("google.navigation")) {
	//        		URLEncodedUtils.parse(intent.getData(), URLEncodedUtils.CONTENT_TYPE);
	        		String [] split1 = intent.getDataString().split("ll=");
	        		String res = split1[1];
	        		String [] split2 = res.split("&");
	        		if(split2.length > 1) {
	        			res = split2[0];
	        		}
	//        		System.err.println("Split string: " + res);
	        		String [] reslatlon = res.split(",");
	        		lat = Double.parseDouble(reslatlon[0]);
	        		lon = Double.parseDouble(reslatlon[1]);
	        	}
        	} else if(intent.getExtras() != null) {
        		/*System.err.println("other intent received");
        		Set<String> extrakeys = intent.getExtras().keySet();
        		for(String key : extrakeys) {
        			System.err.println("Extra: (" + key + ")" + intent.getExtras().get(key));
        		}*/
        		String extra = intent.getExtras().getString("android.intent.extra.TEXT");
        		if(extra != null) {
        			String split [] = extra.split("\n");
        		//	System.err.println("Split: " + split[0]);
        			addr = split[0];
        		}
        	}
        	if(lat != 0.0 && lon != 0.0) {
        		GeoPoint p = new GeoPoint((int)(lat * 1E6),(int)(lon * 1E6));
        		new PointToAddressGeocoderTask(TO).execute(p);
        		markers.updateMarkerPosition(TO, p);
				mapView.invalidate();
				resetSearch();
        	} else if(addr != null) {
        		new AddressToPointGeocoderTask(TO).execute(addr);
        		txtTo.setText(addr);
        	}
        }
        
        loadSavedInstance();
        
        final SharedPreferences preferences1 = getSharedPreferences("favs", Activity.MODE_PRIVATE);
        long lastSavedFavs = preferences1.getLong("lastsaved", 0);
        final int MAX_FAV_AGE = 180000; // 3 minutes
        if(lastSavedFavs + MAX_FAV_AGE < System.currentTimeMillis()) {
	        Account storedAccount = getStoredAccount();
	        //System.err.println("StoredAccount: " + storedAccount);
	        if(storedAccount != null) {
	        	AccountManager accountManager = AccountManager.get(getApplicationContext());
	        	final SharedPreferences preferences = getSharedPreferences(PREFS_COOKIE_PREFS, Activity.MODE_PRIVATE);
	    		String cookiename = preferences.getString(PREFS_COOKIE_NAME, null);
	    		String cookievalue = preferences.getString(PREFS_COOKIE_VALUE, null);
	    		long cookietime = preferences.getLong(PREFS_COOKIE_TIME, 0);
	    		//System.err.println("Cookie values: name: " + cookiename + ", expiry: " + cookietime + ", value: " + cookievalue);
	    		if(cookiename != null && cookievalue != null && cookietime != 0) {
	    			BasicClientCookie cookie = new BasicClientCookie(cookiename, cookievalue);
	    			cookie.setDomain("viaja-facil.appspot.com");
	    			cookie.setPath("/");
	    			cookie.setExpiryDate(new Date(cookietime));
	    			if(cookiename.charAt(0) == 'S') {
	    				cookie.setSecure(true);
	    			}
	    			if(!cookie.isExpired(new Date())) {
	    				appEngineCookie = cookie;
	    				new GetFavoritesTask(false).execute();
	    			} else {
	    				accountManager.getAuthToken(storedAccount, "ah", false, new GetAuthTokenCallback(false), null);
	    			}
	    		} else {
	    			accountManager.getAuthToken(storedAccount, "ah", false, new GetAuthTokenCallback(false), null);
	    		}
	        }
        }
    }
    
    private void loadSavedInstance() {
    	final Object data = getLastNonConfigurationInstance();
        if(data != null) {
        	StoredData stored = (StoredData) data;
        	markers.updateMarkerPosition(FROM, stored.getMarker1());
        	markers.updateMarkerPosition(TO, stored.getMarker2());
        	mapView.getController().setCenter(stored.getMapcenter());
        	mapView.getController().setZoom(stored.getZoomlevel());
        	SearchResultHolder.getInstance().setResultList(stored.getSearchResults());
        	resultOverlays = stored.getResultOverlays();
        	mapView.getOverlays().addAll(resultOverlays);
        	useSubte = stored.isUseSubte();
        	useTrains = stored.isUseTrains();
        	searchmode = stored.getSearchmode();
        	mlkSet1 = stored.getMlkSet1();
        	mlkSet2 = stored.getMlkSet2();
        	Button btnResults = (Button) findViewById(R.id.btnResults);
        	btnResults.setEnabled(stored.isResultsEnabled());
    		Button btnSearch = (Button) findViewById(R.id.btnSearch);
    		btnSearch.setText(stored.getBtnSearchText());
    		lastGeocodedAddr = stored.getLastGeocodedAddr();
        }
    }

	@Override
	protected boolean isRouteDisplayed() {
		return resultOverlays.size() > 0;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		updateLocation();
		if(mlo != null) {
			//mlo.enableCompass();
			mlo.enableMyLocation();
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		locationManager.removeUpdates(locationListener);
		if(mlo != null) {
			//mlo.disableCompass();
			mlo.disableMyLocation();
		}
	}
	
	@Override
	public Object onRetainNonConfigurationInstance() {
		Button btnResults = (Button) findViewById(R.id.btnResults);
		Button btnSearch = (Button) findViewById(R.id.btnSearch);
	    final StoredData toStore = new StoredData(markers.getFromPos(), markers.getToPos(), mapView.getMapCenter(), mapView.getZoomLevel(), SearchResultHolder.getInstance().getResultList(), resultOverlays, useSubte, useTrains, searchmode, mlkSet1, mlkSet2, btnResults.isEnabled(), btnSearch.getText(), lastGeocodedAddr);
	    return toStore;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.menu, menu);
	    this.menu = menu;
	    return true;
	}
	
	@Override
	/*
	 * what to do when an item in an (Options)Menu had been selected
	 */
	public boolean onOptionsItemSelected(MenuItem item) {
//		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		switch (item.getItemId()) {
		case R.id.switchmapnormal: // switch map to certain mode
			mapView.setSatellite(false);
			mapView.setTraffic(false);
			item.setChecked(true);
			return true;
		case R.id.switchmapsat:
			mapView.setTraffic(false);
			mapView.setSatellite(true);
			item.setChecked(true);
			return true;
		case R.id.switchmaptraffic:
			mapView.setSatellite(false);
			mapView.setTraffic(true);
			item.setChecked(true);
			return true;
		case R.id.invert: // invert start/dest
			switchFromTo();
			return true;
		case R.id.subte: // (de)select subte as allowed means of transport
			if(item.isChecked())  {
				item.setChecked(false);
				useSubte = false;
			} else {
				item.setChecked(true);
				useSubte = true;
			}
			performKey(); // to bring the menu back so that the user can also (de)select trains
			return false;
		case R.id.tren:
			if(item.isChecked()) { 
				item.setChecked(false);
				useTrains = false;
			} else {
				item.setChecked(true);
				useTrains = true;
			}
			performKey();
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}
	
	/*
	 * manage creation of Dialogs
	 */
	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		switch(id) {
		case DIALOG_SELECTFROM: // select Start from my Position or from contacts or from favorites (triggered by button on the right of the start-text-box
			builder.setTitle(R.string.selectStartPos);
			CharSequence [] items1 = { this.getResources().getString(R.string.myPos), this.getResources().getString(R.string.contacts),
					this.getResources().getString(R.string.favs)};
			builder.setItems(items1, new DialogListener(id));
			dialog = builder.create();
			break;
		case DIALOG_SELECTTO:
			builder.setTitle(R.string.selectDestPos);
			CharSequence [] items2 = { this.getResources().getString(R.string.myPos), this.getResources().getString(R.string.contacts), 
					this.getResources().getString(R.string.favs)};
			builder.setItems(items2, new DialogListener(id));
			dialog = builder.create();
			break;
		case DIALOG_FROMMAP: // select start or dest from map
			CharSequence [] items3 = { this.getResources().getString(R.string.from) + " " + this.getResources().getString(R.string.here), 
				this.getResources().getString(R.string.to) + " " + this.getResources().getString(R.string.here) };
			builder.setTitle(R.string.fromMap);
			builder.setItems(items3, new DialogInterface.OnClickListener() {			
				@Override
				public void onClick(DialogInterface dialog, int which) {
					GeoPoint p = mapView.getProjection().fromPixels(mapLongTouchX, mapLongTouchY);
					new PointToAddressGeocoderTask(which).execute(p); // which = 0 for from and 1 for to
					markers.updateMarkerPosition(which, p);
					mapView.invalidate();
					resetSearch();
				}
			});
			dialog = builder.create();
			break;
		case DIALOG_CHOOSEADDR: // dialog to choose correct address if the geocoder gave several results
			CharSequence [] items4;
			if(addressesToChooseFrom.size() >= 5) {
				items4 = new CharSequence[5];
			} else {
				items4 = new CharSequence[addressesToChooseFrom.size()];
			}
			for(int i = 0; i < 5 && i < addressesToChooseFrom.size(); i++) {
				//items4[i] = addressesToChooseFrom.get(i).getAddressLine(0) + ", " + addressesToChooseFrom.get(i).getLocality();
				String locality = addressesToChooseFrom.get(i).getLocality();
				if(locality == null) {
					locality = addressesToChooseFrom.get(i).getSubLocality();
				}
				if(locality == null) {
					locality = addressesToChooseFrom.get(i).getSubAdminArea();
				}
				if(locality == null) {
					locality = addressesToChooseFrom.get(i).getPremises();
				}
				items4[i] = addressesToChooseFrom.get(i).getAddressLine(0) + ", " + locality;
			}
			builder.setItems(items4, new DialogInterface.OnClickListener() {			
				@Override
				public void onClick(DialogInterface dialog, int which) {
					GeoPoint p = new GeoPoint((int)(addressesToChooseFrom.get(which).getLatitude() * 1E6),(int)(addressesToChooseFrom.get(which).getLongitude() * 1E6));
					markers.updateMarkerPosition(chooseAddressFromOrTo, p);
					mapView.getController().setCenter(p);
					mapView.getController().setZoom(15);
					resetSearch();
					removeDialog(DIALOG_CHOOSEADDR);
					if(doSearchAfterwards) { // is true if the geocoder was triggered by the search function and tells the activity to try searching again after user selected an address
						doSearchAfterwards = false;
						doSearch();
					}
				}
			});
			dialog = builder.create();
			break;
		case DIALOG_AB_NEEDED: // tell user that start or destination are not set yet
			builder.setMessage(R.string.ab_needed);
			builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			});
			builder.setCancelable(true);
			dialog = builder.create();
			break;
		case DIALOG_NO_LOCATION_YET: // tell the user that his location (GPS/network) is not available yet
			builder.setMessage(R.string.noLocationYet);
			builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			});
			builder.setCancelable(true);
			dialog = builder.create();
			break;
		case DIALOG_ADDRESS_GEOCODING_FAILED: // dialog to inform that geocoding failed
			builder.setMessage(R.string.noaddr);
			builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			});
			builder.setCancelable(true);
			dialog = builder.create();
			break;
		case DIALOG_EXIT_APP: // ask user if he really wants to quit
			builder.setTitle(R.string.question_exit);
			builder.setCancelable(true);
			builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					getContext().finish();
				}
			});
			builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.dismiss();
				}
			});
			dialog = builder.create();
			break;
		default:
			dialog = null;
			Log.e("onCreateDialog", "went to default dialog, that's bad!!!");
		}
		return dialog;
	}

	@Override
	public void onBackPressed() {
		showDialog(DIALOG_EXIT_APP);
	}

	/*
	 * presses the key "Q" which is set to bring up the menu that lets the user decide whether he wants to allow trains/subtes
	 * there seems to be no better way to bring up such a menu
	 */
	private void performKey() {
		new Handler().postDelayed(new Runnable() {
	        public void run() {
	        	menu.performShortcut(KeyEvent.KEYCODE_Q, new KeyEvent(0, KeyEvent.KEYCODE_Q), 0);
	        }
	    }, 200);
	}
	
	/*
	 * start the location service and register the listener
	 */
	private void updateLocation() {
		locationManager.removeUpdates(locationListener);
		try {
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
		} catch (Exception e) {
			Log.e("updateLocation", "Location-Provider NETWORK did not work.");
			e.printStackTrace();
		}
		try {
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
		} catch (Exception e) {
			Log.e("updateLocation", "Location-Provider GPS did not work.");
			e.printStackTrace();
		}
	}

	private void switchFromTo() {
		TextView from = (TextView)findViewById(R.id.txtFrom);
		TextView to = (TextView)findViewById(R.id.txtTo);
		CharSequence tempS = from.getText();
		from.setText(to.getText());
		to.setText(tempS);
		String tempG = lastGeocodedAddr[FROM];
		lastGeocodedAddr[FROM] = lastGeocodedAddr[TO];
		lastGeocodedAddr[TO] = tempG;
		GeoPoint tempM = markers.getFromPos();
		markers.updateMarkerPosition(0, markers.getToPos());
		markers.updateMarkerPosition(1, tempM);
		mapView.invalidate();
		resetSearch();
	}
	
	private void resetSearch() {
		searchmode = 0;
		Button btnSearch = (Button)findViewById(R.id.btnSearch);
		btnSearch.setEnabled(true);
		btnSearch.setText(R.string.search);
		mlkSet1 = null;
		mlkSet2 = null;
		mapView.getOverlays().removeAll(resultOverlays);
		resultOverlays.clear();
	}
	
	private static GeoPoint latLonToGeoPoint(double lat, double lon) {
		return new GeoPoint((int)(lat * 1E6), (int)(lon * 1E6));
	}
	
	public void setMapLongTouchXY(int x, int y) {
		mapLongTouchX = x;
		mapLongTouchY = y;
	}
	
	private void doSearch() {
		if(markers.getFromPos().getLatitudeE6() != 0 && markers.getToPos().getLatitudeE6() != 0) {
			Button btnSearch = (Button)findViewById(R.id.btnSearch);
			btnSearch.setEnabled(false);
			btnSearch.setText(R.string.searching);
			centerMapOnMarkers();
			if(searchmode == 0) {
				SearchResultHolder.getInstance().clear();
				new DirectSearchTask().execute();
			} else {
				new IndirectSearchTask().execute();
			}
		} else {
			showDialog(DIALOG_AB_NEEDED);
		}
	}
	
	private void centerMapOnMarkers() {
		mapView.getController().zoomToSpan((int)(markers.getLatSpanE6() * 1.2), (int)(markers.getLonSpanE6() * 1.2));
		int middlelat = (markers.getFromPos().getLatitudeE6() + markers.getToPos().getLatitudeE6()) / 2;
		int middlelon = (markers.getFromPos().getLongitudeE6() + markers.getToPos().getLongitudeE6()) / 2;
		mapView.getController().setCenter(new GeoPoint(middlelat, middlelon));
	}
	
	protected synchronized void addSearchResult(eu.hellek.gba.shared.SearchResultProxy res) {
		SearchResultHolder.getInstance().add(res);
	}
	
	/*
	 * When the Activity called another activity, i.e. to list user accounts, to list favorites, to show search-results
	 * or anything like that, on returning to the main activity, this function gets called and handles the result that was passed
	 * 
	 * (non-Javadoc)
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK && requestCode == GET_RESULT_TO_DISPLAY) { // show a result on the map
			if (data.hasExtra("resultid")) {
				//Toast.makeText(this, data.getExtras().getInt("resultid"), Toast.LENGTH_SHORT).show();
				drawConnection(data.getExtras().getInt("resultid"));
			}
			if(data.hasExtra("geopoint")) { // zoom to a position if one had been passed
				int [] pos = data.getExtras().getIntArray("geopoint");
				GeoPoint gp = new GeoPoint(pos[0], pos[1]);
				MarkerCircleOverlay mco = new MarkerCircleOverlay(gp);
				resultOverlays.add(mco);
				mapView.getOverlays().add(mco);
				mapView.getController().setCenter(gp);
				mapView.getController().setZoom(16);
			}
		}
		if(resultCode == RESULT_OK && requestCode == GET_ADDR_FROM_CONTACTS) { // if address was selected from contact list
			Uri contactData = data.getData();
			Cursor c =  managedQuery(contactData, null, null, null, null);
			if (c.moveToFirst()) {
			    String address = c.getString(c.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS));
			    TextView tv;
			    if(chooseContactFromOrTo == 0) {
			    	tv = (TextView)findViewById(R.id.txtFrom);
			    } else {
			    	tv = (TextView)findViewById(R.id.txtTo);
			    }
			    tv.setText(address);
			    new AddressToPointGeocoderTask(chooseContactFromOrTo).execute(address);
			}
		}
		if(resultCode == RESULT_OK && requestCode == GET_FAVORITE) { // if a favorite had been selected
			String address = data.getExtras().getString("addr");
			float lat = data.getExtras().getFloat("lat");
			float lon = data.getExtras().getFloat("lon");
			GeoPoint gp = new GeoPoint((int)(lat * 1E6), (int)(lon * 1E6));
			TextView tv;
			if(chooseFavoriteFromOrTo == 0) {
				tv = (TextView)findViewById(R.id.txtFrom);
			} else {
				tv = (TextView)findViewById(R.id.txtTo);
			}
			tv.setText(address);
			lastGeocodedAddr[chooseFavoriteFromOrTo] = address;
			markers.updateMarkerPosition(chooseFavoriteFromOrTo, gp);
			mapView.invalidate();
			resetSearch();
		}
		if(resultCode == RESULT_OK && requestCode == GET_ACCOUNTS_THEN_SHOW_FAVS) { // user selected an account
			Account acc = (Account)data.getExtras().getParcelable("account");
			//System.err.println("Account: " + acc.toString());
			AccountManager accountManager = AccountManager.get(getApplicationContext());
			accountManager.getAuthToken(acc, "ah", false, new GetAuthTokenCallback(true), null);
		}
		if(requestCode == ACCOUNT_MANAGER_INTENT) { // app is returining after the user authorized the accountmanager
			Log.w("onActivitiyResult", "Accountmanager intent returned.");
			AccountManager accountManager = AccountManager.get(getApplicationContext());
			accountManager.getAuthToken(getStoredAccount(), "ah", false, new GetAuthTokenCallback(true), null);
		}
	}
	
	/*
	 * draw a connection on the map (lines, stations, etc.)
	 */
	private void drawConnection(int result) {
		List<Overlay> mapOverlays = mapView.getOverlays();
		ConnectionProxy conn = SearchResultHolder.getInstance().getAtIndex(result);
		mapOverlays.removeAll(resultOverlays);
		resultOverlays.clear();
		int col = 0;
		for(int i = 0; i < conn.getLines().size(); i++) {
			LineProxy lp = conn.getLines().get(i);
			if(lp.getType() != 0 || (i != 0 && i != conn.getLines().size()-1)) {
				if(col >= NUM_COLORS) {
					col = col % NUM_COLORS;
				}
				if(lp.getType() != 0) {
					List<GeoPoint> pathFull = new ArrayList<GeoPoint>(lp.getAllPoints().size());
					for(int j = 0; j < lp.getAllPoints().size(); j = j+2) {
						GeoPoint latlng = new GeoPoint((int)(lp.getAllPoints().get(j) * 1E6), (int)(lp.getAllPoints().get(j+1) * 1E6));
						pathFull.add(latlng);
						if(lp.getStations() != null && lp.getStations().size() > 0) {
							StationOverlay ov;
							if(lp.getType() == 3) {
								ov = new StationOverlay(lp.getStations().get(j/2), latlng, colors[col], 14);
							} else {
								ov = new StationOverlay(lp.getStations().get(j/2), latlng, colors[col], 15);
							}
							resultOverlays.add(ov);
							mapOverlays.add(ov);
						}
					}
					LineOverlay lo1 = new LineOverlay(pathFull, colors[col], 0);
					resultOverlays.add(lo1);
					mapOverlays.add(lo1);
				}
				List<GeoPoint> pathReal = new ArrayList<GeoPoint>(lp.getRelevantPoints().size());
				for(int j = 0; j < lp.getRelevantPoints().size(); j = j+2) {
					GeoPoint latlng = new GeoPoint((int)(lp.getRelevantPoints().get(j) * 1E6), (int)(lp.getRelevantPoints().get(j+1) * 1E6));
					pathReal.add(latlng);
				}
				LineOverlay lo2;
				if(lp.getType() == 0) {
					lo2 = new LineOverlay(pathReal, "#000000", 1);
				} else {
					lo2 = new LineOverlay(pathReal, colors[col], 1);
					col++;
				}
				resultOverlays.add(lo2);
				mapOverlays.add(lo2);
			}
		}
		centerMapOnMarkers();
	}
	
	/*
	 * returns the stored account that the user selected for log-in or null if none is found
	 */
	public Account getStoredAccount() {
		final SharedPreferences preferences = getSharedPreferences(PREFS_ACCOUNT_PREFS, Activity.MODE_PRIVATE);
		String accountname = preferences.getString(PREFS_ACCOUNT_NAME, null);
		AccountManager accountManager = AccountManager.get(getApplicationContext());
		Account account = null;
		if(accountname != null) {
			Account[] accounts = accountManager.getAccountsByType("com.google");
			for(Account a : accounts) {
				if(a.toString().equalsIgnoreCase(accountname)) {
					account = a;
				}
			}
		}
		return account;
	}
	
	public ViajaFacilActivity getContext() {
		return this;
	}
	
	class ButtonListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			if(v.getId() == R.id.btnFrom) {
				showDialog(DIALOG_SELECTFROM);
			} else if(v.getId() == R.id.btnTo) {
				showDialog(DIALOG_SELECTTO);
			} else if(v.getId() == R.id.btnSearch) {
				TextView from = (TextView)findViewById(R.id.txtFrom);
				TextView to = (TextView)findViewById(R.id.txtTo);
				if(lastGeocodedAddr[FROM].equalsIgnoreCase(from.getText().toString()) && lastGeocodedAddr[TO].equalsIgnoreCase(to.getText().toString())) {
					doSearch();
				} else if(!lastGeocodedAddr[FROM].equalsIgnoreCase(from.getText().toString())) {
					doSearchAfterwards = true;
					new AddressToPointGeocoderTask(FROM).execute(from.getText().toString());
				} else if(!lastGeocodedAddr[TO].equalsIgnoreCase(to.getText().toString())) {
					doSearchAfterwards = true;
					new AddressToPointGeocoderTask(TO).execute(to.getText().toString());
				} else {
					Log.e("ButtonListener", "This should not happen");
				}
			} else if(v.getId() == R.id.btnResults) {
				Intent intent = new Intent(v.getContext(), ResultsActivity.class);
				startActivityForResult(intent, GET_RESULT_TO_DISPLAY);
			} else {
				Log.e("ButtonListener", "Did not match any buttons, this should not happen");
			}
		}
	}
	
	/*
	 * for the dialog that is opened by the button next to the from/to textfields
	 */
	class DialogListener implements DialogInterface.OnClickListener {
		
		int callingDialog;
		
		public DialogListener(int i) {
			this.callingDialog = i;
		}

		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch(which) {
			case 0: // set start or dest to the user's current location
				Location location = locationListener.getBestLocation();
				if(location != null) {
					GeoPoint p = latLonToGeoPoint(location.getLatitude(), location.getLongitude());
					new PointToAddressGeocoderTask(callingDialog).execute(p);
					markers.updateMarkerPosition(callingDialog, p);
					mapView.invalidate();
					mapView.getController().animateTo(latLonToGeoPoint(location.getLatitude(), location.getLongitude()));
					resetSearch();
				} else {
					showDialog(DIALOG_NO_LOCATION_YET);
					// Toast.makeText(getBaseContext(), getBaseContext().getResources().getString(R.string.noLocationYet), Toast.LENGTH_SHORT).show();
				}
				break;
			case 1: // start activity to pick from contacts
				Intent intent = new Intent(Intent.ACTION_PICK);
				intent.setType(ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_TYPE);
				chooseContactFromOrTo = callingDialog;
				startActivityForResult(intent, GET_ADDR_FROM_CONTACTS);
				break;
			case 2: // start activities to choose from favorites. log-in first if necessary
				chooseFavoriteFromOrTo = callingDialog;
				if(getStoredAccount() != null) {
					if(appEngineCookie != null && !appEngineCookie.isExpired(new Date())) {
						Intent intent2 = new Intent(getBaseContext(), FavoritesActivity.class);
						startActivityForResult(intent2, GET_FAVORITE);
					} else {
						AccountManager accountManager = AccountManager.get(getApplicationContext());
						accountManager.getAuthToken(getStoredAccount(), "ah", false, new GetAuthTokenCallback(true), null);
					}
				} else {
					Intent intent2 = new Intent(getBaseContext(), AccountListActivity.class);
					startActivityForResult(intent2, GET_ACCOUNTS_THEN_SHOW_FAVS);
				}
				break;
			}
		}
	}
	
	/*
	 * listens to actions related to the from/to textviews
	 */
	class TextViewListener implements OnEditorActionListener, OnKeyListener, OnFocusChangeListener {
		
		int fromOrTo;
		
		public TextViewListener(int fromOrTo) {
			this.fromOrTo = fromOrTo;
		}

		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			if(v.getText().length() > 5) {
				new AddressToPointGeocoderTask(fromOrTo).execute(v.getText().toString());
			}
			return true;
		}

		@Override
		public boolean onKey(View v, int actionId, KeyEvent event) {
			if((event.getAction() == KeyEvent.ACTION_DOWN) && (actionId == KeyEvent.KEYCODE_ENTER))  {
				TextView tv = (TextView) v;
				return onEditorAction(tv, actionId, event);
			} else {	
				return false;
			}
		}

		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			TextView tv = (TextView)v;
			if(!hasFocus) {
				if(tv.getText().length() > 5 && !lastGeocodedAddr[fromOrTo].equalsIgnoreCase(tv.getText().toString())) {
					new AddressToPointGeocoderTask(fromOrTo).execute(tv.getText().toString());
				}
			}
		}
	}
	
	/*
	 * Geocode a lat/lon to an Adress
	 */
	private class PointToAddressGeocoderTask extends AsyncTask<GeoPoint, Void, String> {

		private int fromOrTo;
		
		public PointToAddressGeocoderTask(int fromOrTo) {
			this.fromOrTo = fromOrTo;
		}

		protected String doInBackground(GeoPoint... p) {
			Geocoder geoCoder = new Geocoder(getBaseContext(), Locale.getDefault());
			try {
				List<Address> addresses = geoCoder.getFromLocation(p[0].getLatitudeE6() / 1E6, p[0].getLongitudeE6() / 1E6, 1);
				if(addresses.size() > 0) {
					String addr = addresses.get(0).getAddressLine(0);
					return addr;
				}
				return null;
			} catch (IOException e) {
				//e.printStackTrace();
				Log.e("PointToAddressGeocoderTask", "Geocoder failed", e);
				return null;
			}
		}

		protected void onPostExecute(String addr) {
			if(addr != null) {
				lastGeocodedAddr[fromOrTo] = addr;
			}
			TextView tv;
			if(fromOrTo == FROM) {
				tv = (TextView)findViewById(R.id.txtFrom);
			} else {
				tv = (TextView)findViewById(R.id.txtTo);
			}
			tv.setText(addr);
		}
	}
	
	/*
	 * geocode an address to a lat/lon Point. Might bring several results, in which case the user has to select the correct one
	 */
	private class AddressToPointGeocoderTask extends AsyncTask<String, Void, List<Address>> {

		private int fromOrTo;
		
		public AddressToPointGeocoderTask(int fromOrTo) {
			this.fromOrTo = fromOrTo;
		}

		protected List<Address> doInBackground(String... s) {
			lastGeocodedAddr[fromOrTo] = s[0];
			Geocoder geoCoder = new Geocoder(getBaseContext(), Locale.getDefault());
			try {
				Log.d("AddressGeoCoder", "Addr: " + s[0]);
				//List<Address> addresses = geoCoder.getFromLocationName(s[0], 5, -35.321848, -59.295959, -34.04697, -57.77298);
				List<Address> addresses = geoCoder.getFromLocationName(s[0], 5);
				return addresses;
			} catch (IOException e) {
//				e.printStackTrace();
				Log.e("ADdressToPointGeocodertask", "Geocoder failed", e);
				return null;
				/*Address temp1 = new Address(Locale.getDefault());
				temp1.setAddressLine(0, "test1 " + new Random().nextLong());
				Address temp2 = new Address(Locale.getDefault());
				temp2.setAddressLine(0, "test2 " + new Random().nextLong());
				List<Address> list = new ArrayList<Address>(2);
				list.add(temp1);
				list.add(temp2);
				return list;*/
			}
		}

		protected void onPostExecute(List<Address> addresses) {
			GeoPoint p = null;
			if(addresses == null || addresses.size() == 0) {
				showDialog(DIALOG_ADDRESS_GEOCODING_FAILED);
				if(doSearchAfterwards) {
					doSearchAfterwards = false;
				}
				// Toast.makeText(getBaseContext(), getBaseContext().getResources().getString(R.string.noaddr), Toast.LENGTH_LONG).show();
			} else if(addresses.size() == 1) {
				Address addr = addresses.get(0);
				p = new GeoPoint((int)(addr.getLatitude() * 1E6),(int)(addr.getLongitude() * 1E6));
			} else {
				addressesToChooseFrom = addresses;
				chooseAddressFromOrTo = fromOrTo;
				showDialog(DIALOG_CHOOSEADDR);
			}
			
			if(p != null) {
				markers.updateMarkerPosition(fromOrTo, p);
				mapView.getController().setCenter(p);
				mapView.getController().setZoom(15);
				resetSearch();
				if(doSearchAfterwards) {
					doSearchAfterwards = false;
					doSearch();
				}
			}
		}
	}
	
	/*
	 * runs a direct search (that uses max. one bus line)
	 * the result contains two sets (mlk1 and mlk2) which are keys that need to be stored to be passed to the indirect search
	 */
	private class DirectSearchTask extends AsyncTask<Void, Void, eu.hellek.gba.proto.SearchResultProtos.SearchResultProxy> {
		
		private ProgressDialog searching;
		private DirectSearchRequest req;
		
		@Override
		protected void onPreExecute() {
			searching = ProgressDialog.show(getContext(), "", getContext().getResources().getString(R.string.searching), true);
			DirectSearchRequest.Builder builder = DirectSearchRequest.newBuilder();
			builder.setIgnoreSubte(!useSubte); // whether to use trains/subte. needs to be inverted since the server reads whether subte shall NOT be used
			builder.setIgnoreTrains(!useTrains);
			builder.setLat1((float)(markers.getFromPos().getLatitudeE6() / 1E6));
			builder.setLon1((float)(markers.getFromPos().getLongitudeE6() / 1E6));
			builder.setLat2((float)(markers.getToPos().getLatitudeE6() / 1E6));
			builder.setLon2((float)(markers.getToPos().getLongitudeE6() / 1E6));
			DirectSearchRequest req = builder.build();
			this.req = req;
		}
		
		@Override
		protected eu.hellek.gba.proto.SearchResultProtos.SearchResultProxy doInBackground(Void... params) {
			try {
				URL url = new URL("http://"+appurl+"/rm/DirectSearchServlet");
				HttpURLConnection conn = (HttpURLConnection)url.openConnection();
				conn.setDoOutput(true);
				conn.setRequestMethod("POST");
				conn.setRequestProperty("Content-Type", "application/x-protobuf");
				conn.setRequestProperty("Content-Length", ""+req.getSerializedSize());
				OutputStream out = conn.getOutputStream();
				CodedOutputStream cout = CodedOutputStream.newInstance(out);
				req.writeTo(cout);
				cout.flush();
				out.close();
				eu.hellek.gba.proto.SearchResultProtos.SearchResultProxy srp = eu.hellek.gba.proto.SearchResultProtos.SearchResultProxy.parseFrom(conn.getInputStream());
				return srp;
			} catch(Exception e) {
				Log.e("DirectSearchTask", "Error in search", e);
				e.printStackTrace();
				return null;
			}
		}

		@Override
		protected void onPostExecute(eu.hellek.gba.proto.SearchResultProtos.SearchResultProxy srp) {
			Button btnSearch = (Button)findViewById(R.id.btnSearch);
			btnSearch.setEnabled(true);
			try {
				searching.dismiss();
			} catch (Exception e) {
				Log.w("DirectSearchTask", "Dismissing the dialog threw an error: " + e);
			}
			if(srp != null) {
				mlkSet1 = srp.getMlkSet1StringList(); // store the important mlkSet1/mlkSet2 for a possible subsequent indirect search
				mlkSet2 = srp.getMlkSet2StringList();
				SearchResultProxy resfinal = Helpers.copyFromProto(srp);
				searchmode = 1;
				if(resfinal.getError() == null || resfinal.getError().length() == 0) {
					Collections.sort(resfinal.getConnections(), new ConnectionProxyComparator());
					/*System.err.println(srp.getConnectionsList().get(0).getLinesList().get(0).getLinenum());
					for(ConnectionProxy cp : resfinal.getConnections()) {
						if(cp.getLines() != null) {
							System.err.println("Connection:");
							for(LineProxy lp : cp.getLines()) {
								System.err.println(lp + " " + lp.getTime());
							}
						}
					}*/
					getContext().addSearchResult(resfinal);
					btnSearch.setText(R.string.searchindirect);
					Button btnResults = (Button) findViewById(R.id.btnResults);
					btnResults.setEnabled(true);

					Intent intent = new Intent(getContext(), ResultsActivity.class);
					startActivityForResult(intent, GET_RESULT_TO_DISPLAY);
				} else if(resfinal.getError().equals(SearchResultProxy.noResults)) {
					doSearch();
				} else if(resfinal.getError().equals(SearchResultProxy.tooManyReqests)) {
					Toast.makeText(getContext(), getContext().getResources().getString(R.string.errRequests), Toast.LENGTH_LONG).show();
				} else if(resfinal.getError().equals(SearchResultProxy.distanceTooLittle)) {
					Toast.makeText(getContext(), getContext().getResources().getString(R.string.errDistance), Toast.LENGTH_LONG).show();
				} else {
					resetSearch();
					Toast.makeText(getContext(), "Error: " + resfinal.getError(), Toast.LENGTH_LONG).show();
				}
			} else {
				resetSearch();
				Toast.makeText(getContext(), "Connection error", Toast.LENGTH_LONG).show();
			}
		}

		@Override
		protected void onCancelled() {
			searching.dismiss();
			resetSearch();
		}
	}
	
	/*
	 * indirect search (that might use more than one bus line or a combination of buses and trains/subtes)
	 * can ONLY be run if a direct search had been run before since the direct search gathers some data that is needed for 
	 * the indirect search (contained in mlkSet1/mlkSet2) which need to be passed here
	 * depending on the density of colectivos at the start point, it might separate them into parallel tasks
	 * that are collected at the end.
	 */
	class IndirectSearchTask extends AsyncTask<Void, Integer, Void> {
		
		private ProgressDialog searching;
		private ArrayList<IndirectSearchRequest> reqs;
		private SearchResultProxy results;
		private int numThreads;
		private int finishedThreads;
		
		private final static int partitionMaxSize = 40;

		@Override
		protected void onPreExecute() {
			reqs = new ArrayList<IndirectSearchRequest>();
			results = new SearchResultProxy(new ArrayList<ConnectionProxy>(), null, null);
			searching = new ProgressDialog(getContext());
			searching.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			searching.setMessage(getContext().getResources().getString(R.string.searching));
			searching.setCancelable(false);
			searching.show();
			int partitions = 0;
			finishedThreads = 0;
			if(mlkSet1 != null && mlkSet1.size() != 0) { // create partitions for parallel tasks (at least one)
				partitions = (mlkSet1.size() / partitionMaxSize)+1;
			}
			for(int i = 0; i < partitions; i++) { // start task for each partition
				List<String> mlkSet1PartitionString = new ArrayList<String>();
				if(i < partitions - 1) {
					mlkSet1PartitionString = mlkSet1.subList((mlkSet1.size()/partitions) * i, (mlkSet1.size()/partitions) * (i+1));
				} else {
					mlkSet1PartitionString = mlkSet1.subList((mlkSet1.size()/partitions) * i, mlkSet1.size());
				}
				List<String> mlkSet1Part = new ArrayList<String>();
				for(String s : mlkSet1PartitionString) {
					mlkSet1Part.add(s);
				}
				IndirectSearchRequest.Builder builder = IndirectSearchRequest.newBuilder();
				builder.setIgnoreSubte(!useSubte);
				builder.setIgnoreTrains(!useTrains);
				builder.setLat1((float)(markers.getFromPos().getLatitudeE6() / 1E6));
				builder.setLon1((float)(markers.getFromPos().getLongitudeE6() / 1E6));
				builder.setLat2((float)(markers.getToPos().getLatitudeE6() / 1E6));
				builder.setLon2((float)(markers.getToPos().getLongitudeE6() / 1E6));
				builder.addAllMlkSet1String(mlkSet1Part);
				builder.addAllMlkSet2String(mlkSet2);
				IndirectSearchRequest req = builder.build();
				reqs.add(req);
			}
		}
		
		@Override
		/*
		 * wait for threads to finish
		 */
		protected Void doInBackground(Void... params) {
			List<Thread> threads = new ArrayList<Thread>();
			for(IndirectSearchRequest req : reqs) {
				Thread t = new Thread(new IndirectSearchThread(this, req));
				threads.add(t);
			}
			numThreads = threads.size();
			for(Thread t : threads) {
				t.start();
			}
			for(Thread t : threads) {
				try {
					t.join();
				} catch (InterruptedException e) {
					Log.w("IndirectSearchTask", "Interrupted", e);
					e.printStackTrace();
				}
			}
			return null;
		}

		@Override
		/*
		 * open results activity to show results
		 */
		protected void onPostExecute(Void result) {
			try {
				searching.dismiss();
			} catch (Exception e) {
				Log.w("IndirectSearchTask", "Dismissing the dialog threw an error: " + e);
			}
			resetSearch();
			
			if(results.getConnections().size() > 0) {
				Collections.sort(results.getConnections(), new ConnectionProxyComparator());
				getContext().addSearchResult(results);
				Collections.reverse(SearchResultHolder.getInstance().getResultList());
				Button btnResults = (Button) findViewById(R.id.btnResults);
				btnResults.setEnabled(true);
				/*
				System.err.println("Result:");
				for(ConnectionProxy cp : results.getConnections()) {
					if(cp.getLines() != null) {
						System.err.println("Connection:");
						for(LineProxy lp : cp.getLines()) {
							System.err.println(lp + " " + lp.getTime());
						}
					}
				}*/
				Intent intent = new Intent(getContext(), ResultsActivity.class);
				startActivityForResult(intent, GET_RESULT_TO_DISPLAY);
			} else if(results.getError() != null && results.getError().length() > 0){
				Toast.makeText(getContext(), "Error: " + results.getError(), Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(getContext(), getContext().getResources().getString(R.string.noresults), Toast.LENGTH_LONG).show();
			}

		}
		
		@Override
		/*
		 * update progress bar every time a task finishes
		 */
		protected void onProgressUpdate(Integer... progress) {
			searching.setProgress(progress[0]);
		}

		/*
		 * here, child threads add their result before killing themselves
		 */
		public synchronized void addResult(SearchResultProxy result) {
			if(result.getConnections() != null && result.getConnections().size() > 0 && !results.getConnections().contains(result.getConnections().get(0))) {
				results.getConnections().addAll(result.getConnections());
			}
			if(result.getError() != null && !result.getError().equals(SearchResultProxy.noResults)) {
				results.setError(result.getError());
			}
		}
		
		public synchronized void reportFinish() {
			finishedThreads++;
			publishProgress((int) ((finishedThreads / (float) numThreads) * 100));
		}
	}

	/*
	 * stuff necessary for the login with google for synchronizing favorites
	 */
	private class GetAuthTokenCallback implements AccountManagerCallback<Bundle> {
		
		private boolean showFavs;
		
		public GetAuthTokenCallback(boolean showFavs) {
			this.showFavs = showFavs;
			Log.d("GetAuthTokenCallback", "GetAuthTokenCallback called.");
		}
		
		public void run(AccountManagerFuture<Bundle> result) {
			Bundle bundle;
			try {
				bundle = result.getResult();
				Intent intent = (Intent)bundle.get(AccountManager.KEY_INTENT);
				if(intent != null) {
					// User input required
					startActivityForResult(intent, ACCOUNT_MANAGER_INTENT);
				} else {
					String auth_token = bundle.getString(AccountManager.KEY_AUTHTOKEN);
					new GetCookieTask(showFavs).execute(auth_token);
				}
			} catch (OperationCanceledException e) {
				Log.e("GetAuthTokenCallback", "OperationCancelledException", e);
				e.printStackTrace();
			} catch (AuthenticatorException e) {
				Log.e("GetAuthTokenCallback", "AuthenticatorException", e);
				e.printStackTrace();
			} catch (IOException e) {
				Log.e("GetAuthTokenCallback", "IOException", e);
				e.printStackTrace();
			}
		}
	};

	/*
	 * stuff necessary for the login with google for synchronizing favorites
	 */
	private class GetCookieTask extends AsyncTask<String, Void, Boolean> {
		
		private boolean showFavs;
		
		public GetCookieTask(boolean showFavs) {
			this.showFavs = showFavs;
			Log.d("GetCookieTask", "GetCookieTask called.");
		}
		
		protected Boolean doInBackground(String... tokens) {
			DefaultHttpClient http_client = new DefaultHttpClient();
			try {
				// Don't follow redirects
				http_client.getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false);
				HttpGet http_get = new HttpGet("https://viaja-facil.appspot.com/_ah/login?continue=http://localhost/&auth=" + tokens[0]);
				HttpResponse response;
				response = http_client.execute(http_get);
				if(response.getStatusLine().getStatusCode() != 302) {
					Log.w("GetCookieTask", "Response was not a redirect: " + response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
					final char[] buffer = new char[0x10000];
					StringBuilder out = new StringBuilder();
					Reader in = new InputStreamReader(response.getEntity().getContent(), "UTF-8");
					int read;
					do {
					  read = in.read(buffer, 0, buffer.length);
					  if (read>0) {
					    out.append(buffer, 0, read);
					  }
					} while (read>=0);
					Log.w("GetCookieTask", out.toString());
					if(getFavsReRuns < 3) {
						getFavsReRuns++;
						AccountManager accountManager = AccountManager.get(getApplicationContext());
						accountManager.invalidateAuthToken(getStoredAccount().type, tokens[0]);
						accountManager.getAuthToken(getStoredAccount(), "ah", false, new GetAuthTokenCallback(showFavs), null);
					}
					return false;
				}

				for(Cookie cookie : http_client.getCookieStore().getCookies()) {
					Log.d("GetCookieTask", "Got cookie: " + cookie.getName());;
					if(cookie.getName().equals("ACSID") || cookie.getName().equals("SACSID")) {
						appEngineCookie = cookie;
						final SharedPreferences preferences = getSharedPreferences(PREFS_COOKIE_PREFS, Activity.MODE_PRIVATE);
			    		preferences.edit().putString(PREFS_COOKIE_NAME, cookie.getName())
			    		.putString(PREFS_COOKIE_VALUE, cookie.getValue())
			    		.putLong(PREFS_COOKIE_TIME, cookie.getExpiryDate().getTime())
			    		.commit();
						//System.err.println("Domain: " + cookie.getDomain() + ", name: " + cookie.getName() + ", path: " + cookie.getPath() + ", value: " + cookie.getValue());
						return true;
					}
				}
			} catch (ClientProtocolException e) {
				Log.e("GetCookieTask", "ClientProtocolException", e);
				e.printStackTrace();
			} catch (IOException e) {
				Log.e("GetCookieTask", "IOException", e);
				e.printStackTrace();
			}
			return false;
		}

		protected void onPostExecute(Boolean result) {
			if(result) {
				new GetFavoritesTask(showFavs).execute();
			} else {
				Log.e("GetCookieTask", "Not starting next task since no cookie was received.");
				if(showFavs) {
					Toast.makeText(getContext(), "Error: " + getContext().getResources().getString(R.string.failed_downloading_favs), Toast.LENGTH_LONG).show();
					Intent intent = new Intent(getBaseContext(), FavoritesActivity.class);
					startActivityForResult(intent, GET_FAVORITE);
				}
			}
		}
	}
	
	/*
	 * Task to update/download favorites from server
	 */
	private class GetFavoritesTask extends AsyncTask<Void, Void, LoginInfo> {
		
		private boolean showFavs;
		
		public GetFavoritesTask(boolean showFavs) {
			this.showFavs = showFavs;
			Log.d("GetFavoritesTask", "GetFavoritesTask called.");
		}
		
		@Override
		protected LoginInfo doInBackground(Void... params) {
			try {
				DefaultHttpClient http_client = new DefaultHttpClient();
				http_client.getCookieStore().addCookie(appEngineCookie);
				HttpGet method = new HttpGet("https://"+ViajaFacilActivity.appurlssl+"/rm/LoginServlet");
				HttpResponse response = http_client.execute(method);
				HttpEntity responseEntity = response.getEntity();
				LoginInfo li = LoginInfo.parseFrom(responseEntity.getContent());
				Log.d("GetFavoritesTask", "Logged in as: " + li.getEmailAddress());
				return li;
			} catch(Exception e) {
				Log.e("GetFavoritesTask", "Error", e);
				e.printStackTrace();
				return null;
			}
		}

		@Override
		protected void onPostExecute(LoginInfo result) {
			if(result != null && result.getLoggedIn()) {
				final SharedPreferences preferences = getSharedPreferences("favs", Activity.MODE_PRIVATE);
				preferences.edit().putLong("lastsaved", System.currentTimeMillis()).commit();
				downloadedFavs = true;
				FavoritesDbAdapter mDbHelper = new FavoritesDbAdapter(getContext());
				mDbHelper.open();
	            mDbHelper.clearDB();
	            for(UserFavouritePositionProxy fpp : result.getFavouritesList()) {
	            	mDbHelper.addFavorite(fpp.getKey(), fpp.getName(), fpp.getLat(), fpp.getLon());
	            }
	            mDbHelper.close();
				if(showFavs) {
					Intent intent = new Intent(getBaseContext(), FavoritesActivity.class);
					startActivityForResult(intent, GET_FAVORITE);
				}
			} else {
				if(getFavsReRuns < 4) {
					getFavsReRuns++;
					Account storedAccount = getStoredAccount();
					AccountManager accountManager = AccountManager.get(getApplicationContext());
					accountManager.getAuthToken(storedAccount, "ah", false, new GetAuthTokenCallback(showFavs), null);
				}
			}
		}
	}
	
	public void addFavorite(UserFavouritePositionProxy fpp) {
		new AddFavoriteTask().execute(fpp);
	}
	
	/*
	 * task to store an additional favorite on the server
	 */
	private class AddFavoriteTask extends AsyncTask<UserFavouritePositionProxy, Void, UserFavouritePositionProxy> {
		
		@Override
		protected UserFavouritePositionProxy doInBackground(UserFavouritePositionProxy... params) {
			Log.d("AddFavoriteTask", "AddFavoriteTask called.");
			try {
				DefaultHttpClient http_client = new DefaultHttpClient();
				http_client.getCookieStore().addCookie(appEngineCookie);
				HttpPost method = new HttpPost("https://"+ViajaFacilActivity.appurlssl+"/rm/AddFavoriteServlet");
				ByteArrayEntity byteArrEntity = new ByteArrayEntity(params[0].toByteArray());
				byteArrEntity.setContentType("application/x-protobuf");
				method.setEntity(byteArrEntity);
				HttpResponse response = http_client.execute(method);
				HttpEntity responseEntity = response.getEntity();
				UserFavouritePositionProxy fpp = UserFavouritePositionProxy.parseFrom(responseEntity.getContent());
				return fpp;
			} catch(Exception e) {
				Log.e("AddFavoriteTask", "Error", e);
				e.printStackTrace();
				return null;
			}
		}

		@Override
		protected void onPostExecute(UserFavouritePositionProxy result) {
			if(result != null) {
				FavoritesDbAdapter mDbHelper = new FavoritesDbAdapter(getContext());
				mDbHelper.open();
				mDbHelper.addFavorite(result.getKey(), result.getName(), result.getLat(), result.getLon());
	            mDbHelper.close();
			} else {
				Toast.makeText(getContext(), "Error: " + getContext().getResources().getString(R.string.failed_adding_fav), Toast.LENGTH_LONG).show();
			}
		}
	}

}