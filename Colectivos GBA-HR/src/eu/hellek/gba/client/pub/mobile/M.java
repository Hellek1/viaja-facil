package eu.hellek.gba.client.pub.mobile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.google.code.gwt.geolocation.client.Coordinates;
import com.google.code.gwt.geolocation.client.Geolocation;
import com.google.code.gwt.geolocation.client.Position;
import com.google.code.gwt.geolocation.client.PositionCallback;
import com.google.code.gwt.geolocation.client.PositionError;
import com.google.code.gwt.geolocation.client.PositionOptions;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.maps.client.MapOptions;
import com.google.gwt.maps.client.MapTypeId;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.base.HasLatLng;
import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.base.LatLngBounds;
import com.google.gwt.maps.client.event.Event;
import com.google.gwt.maps.client.event.EventCallback;
import com.google.gwt.maps.client.event.HasMouseEvent;
import com.google.gwt.maps.client.event.MouseEventCallback;
import com.google.gwt.maps.client.geocoder.Geocoder;
import com.google.gwt.maps.client.geocoder.GeocoderCallback;
import com.google.gwt.maps.client.geocoder.GeocoderRequest;
import com.google.gwt.maps.client.geocoder.HasGeocoderResult;
import com.google.gwt.maps.client.overlay.Circle;
import com.google.gwt.maps.client.overlay.CircleOptions;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.maps.client.overlay.MarkerImage;
import com.google.gwt.maps.client.overlay.MarkerOptions;
import com.google.gwt.maps.client.overlay.OverlayView;
import com.google.gwt.maps.client.overlay.Polyline;
import com.google.gwt.maps.client.overlay.PolylineOptions;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.Navigator;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimpleCheckBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.StackLayoutPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import eu.hellek.gba.client.GetPointsService;
import eu.hellek.gba.client.GetPointsServiceAsync;
import eu.hellek.gba.client.ListPointsService;
import eu.hellek.gba.client.ListPointsServiceAsync;
import eu.hellek.gba.client.pub.common.AppConstants;
import eu.hellek.gba.client.pub.common.AutocompletePlace;
import eu.hellek.gba.client.pub.common.NullMap;
import eu.hellek.gba.client.pub.common.StationOverlayView;
import eu.hellek.gba.client.pub.mobile.resources.Resources;
import eu.hellek.gba.shared.ConnectionProxy;
import eu.hellek.gba.shared.ConnectionProxyComparator;
import eu.hellek.gba.shared.LineProxy;
import eu.hellek.gba.shared.LoginInfo;
import eu.hellek.gba.shared.SearchResultProxy;
import eu.hellek.gba.shared.UserFavouritePositionProxy;

public class M implements EntryPoint {
	
	private static final AppConstants CONSTANTS = GWT.create(AppConstants.class);
	private static final Resources resources = GWT.create(Resources.class);
	
	private final ListPointsServiceAsync listPointsService = GWT.create(ListPointsService.class);
	private final GetPointsServiceAsync getPointsService = GWT.create(GetPointsService.class);
	
	private List<String> mlkSet1 = null; // keys of lines in results of direct search. they are ignored in indirect search
	private List<String> mlkSet2 = null; 
	
	private boolean fromCoordDirty = true; // true if it is necessary to do geocoding stuff to get the coordinates before being able to start the search
	private boolean toCoordDirty = true;
	boolean ranSearch; // since the above use callbacks, this var is set to avoid starting the search twice
	private boolean typedFrom = false;
	private boolean typedTo = false;
	private int locationWatchId;
	private boolean stopGeoLocating;
	
	private int searchmode = 0; // 0 = direct, 1 = indirect
	
	private Marker fromMarker;
	private Marker toMarker;
	private Circle markerCircle = null;
	
	private TextBox txtTo;
	private TextBox txtFrom;
	
	private MapWidget mapWidget;
	
	private VerticalPanel favouritesMenu;
	private FlowPanel currentStreetPanel;
	private Label currentStreetLabel;
	private Image starAdd;
	private String fromOrToFavs; // set to from if it is displayed under the from field, or to if displayed under the to field
	
	private SearchResultProxy resultFromDirectSearch;
	private List<SearchResultProxy> resultList;
	private LoginInfo currentLogin;
	private boolean justsafed = false;
	
	private int activeIndirectSearches;
	private boolean gotAnIndirectResult;
	private boolean searchRunning = false;
	
	private SimpleCheckBox simpleCheckBoxTrains;
	private SimpleCheckBox simpleCheckBoxSubte;
	private StackLayoutPanel resultsPanel;
	private FlowPanel flowPanelSouth;
	private int selectedIndex = -5;
	private List<Polyline> polyLines = new LinkedList<Polyline>();
	private List<OverlayView> stations = new LinkedList<OverlayView>();
	private List<String> colors = new LinkedList<String>();
	private DecoratedPopupPanel searchErrorPopup;
	private Button btnSearch;
	private AbsolutePanel dummyPanel;
	private ToggleButton imgTo;
	private ToggleButton imgFrom;
	private boolean showedSelectFromMapOnce = false;
	private SimplePanel panelLogin;
	private DecoratedPopupPanel spinningCircle;
	
	public void onModuleLoad() {
		
		colors.add("#ff0000");
		colors.add("#0000ff");
		colors.add("#00ff00");
		colors.add("#ff00ff");
		colors.add("#00ffff");
		
		final RootPanel rootPanel = RootPanel.get();
//		Window.setTitle(CONSTANTS.appTitle());
		
		/*
		 * dummy-panel to measure size of results-labels
		 */
		dummyPanel = new AbsolutePanel();
		dummyPanel.setStyleName("gwt-StackLayoutPanel");
		dummyPanel.setSize("100%", "450px");
		rootPanel.add(dummyPanel);
		dummyPanel.getElement().setAttribute("style", "border-bottom: none;");
		
		
		/*
		 * stuff for showing and saving favourites
		 */
		favouritesMenu = new VerticalPanel();
	    favouritesMenu.setStyleName("favouritesMenu");
	    favouritesMenu.setVisible(false);
	    DOM.setElementAttribute(favouritesMenu.getElement(), "id", "favouritesMenuDiv");
	    rootPanel.add(favouritesMenu);
	    
	    /*FlowPanel mainPanel = new FlowPanel();
	    mainPanel.setStyleName("mainpanel");
		rootPanel.add(mainPanel);*/
	    Panel mainPanel = rootPanel;
		
		FlowPanel panelFrom = new FlowPanel();
		panelFrom.setStyleName("inputLineHolder");
		mainPanel.add(panelFrom);
		/*FlowPanel panelFrom2 = new FlowPanel();
		panelFrom2.setStyleName("inputLineHolder2");
		panelFrom.add(panelFrom2);*/
		FlowPanel panelImgFrom = new FlowPanel();
		panelImgFrom.setStyleName("imgHolder");
		panelFrom.add(panelImgFrom);
		imgFrom = new ToggleButton(new Image(resources.MarkerACircle()));
		imgFrom.setTitle(CONSTANTS.selectFromMap());
		imgFrom.addStyleName("topMarkerImage");
		imgFrom.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Event.clearListeners(mapWidget.getMap(), "click");
				if(imgTo.isDown()) {
					imgTo.setValue(false);
				}
				if(imgFrom.isDown()) {
					showSelectFromMap();
					Event.addListener(mapWidget.getMap(), "click", new MouseEventCallback() {
						@Override
						public void callback(HasMouseEvent event) {
							HasLatLng eventLatLng = event.getLatLng();
							updateFromMarker(eventLatLng);
							fromCoordDirty = false;
							GeocoderRequest gcReq = new GeocoderRequest();
			    			if (!GWT.isScript()) {
			    				   removeGwtObjectId(gcReq.getJso());
			    			}
			    			gcReq.setLatLng(fromMarker.getPosition());
			    			gcReq.setRegion("ar");
			    			Geocoder gc = new Geocoder();
			    			gc.geocode(gcReq, new MyInverseGeocoderCallback("from"));
			    			resetSearch();
			    			Event.clearListeners(mapWidget.getMap(), "click");
			    			imgFrom.setValue(false);
						}
					});
				}
			}		
		});
		panelImgFrom.add(imgFrom);
		FlowPanel panelTxtFrom = new FlowPanel();
		panelTxtFrom.setStyleName("textboxHolder");
		panelFrom.add(panelTxtFrom);
		txtFrom = new TextBox();
		txtFrom.getElement().setId("textbox-from");
		txtFrom.setTitle(CONSTANTS.instr1());
		panelTxtFrom.add(txtFrom);
		
		final Button btnLocate = new Button();
		btnLocate.setStyleName("locateButton");
		btnLocate.setTitle(CONSTANTS.loc());
		panelFrom.add(btnLocate);
		
		Image imgShowFavsFrom = new Image(resources.arrowDown());
		imgShowFavsFrom.setAltText(CONSTANTS.showFavourites());
		imgShowFavsFrom.setTitle(CONSTANTS.showFavourites());
		imgShowFavsFrom.setStyleName("expandImage");
		panelFrom.add(imgShowFavsFrom);
		imgShowFavsFrom.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if(!favouritesMenu.isVisible()) {
					fromOrToFavs = "from";
					if(txtFrom.getText() != null && txtFrom.getText().length() > 5) {
						currentStreetLabel.setText(txtFrom.getText());
						starAdd.setResource(resources.starOutline());
					}
					DOM.setStyleAttribute(favouritesMenu.getElement(), "top", txtFrom.getAbsoluteTop() + "px");
					DOM.setStyleAttribute(favouritesMenu.getElement(), "left", "46px");
					DOM.setStyleAttribute(favouritesMenu.getElement(), "height", favouritesMenu.getWidgetCount()*22 + 5 + "px");
					DOM.setStyleAttribute(favouritesMenu.getElement(), "width", (txtFrom.getOffsetWidth()-26)+"px");
					favouritesMenu.setVisible(true);
				} else {
					favouritesMenu.setVisible(false);
					if(justsafed) {
						getPointsService.login(GWT.getHostPageBaseURL(), new AsyncCallback<LoginInfo>() {
							@Override
							public void onFailure(Throwable caught) {
								GWT.log("Error in login service (to update favourites)");
							}

							@Override
							public void onSuccess(LoginInfo result) {
								updateFavourites(result);
							}		
						});
					}
				}
			}
			
		});
		
		FlowPanel panelTo = new FlowPanel();
		panelTo.setStyleName("inputLineHolder");
		mainPanel.add(panelTo);
		/*FlowPanel panelTo2 = new FlowPanel();
		panelTo2.setStyleName("inputLineHolder2");
		panelTo.add(panelTo2);*/	
		FlowPanel panelImgTo = new FlowPanel();
		panelImgTo.setStyleName("imgHolder");
		panelTo.add(panelImgTo);
		imgTo = new ToggleButton(new Image(resources.MarkerBCircle()));
		imgTo.setTitle(CONSTANTS.selectFromMap());
		imgTo.addStyleName("topMarkerImage");
		imgTo.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Event.clearListeners(mapWidget.getMap(), "click");
				if(imgFrom.isDown()) {
					imgFrom.setValue(false);
				}
				if(imgTo.isDown()) {
					showSelectFromMap();
					Event.addListener(mapWidget.getMap(), "click", new MouseEventCallback() {
						@Override
						public void callback(HasMouseEvent event) {
							HasLatLng eventLatLng = event.getLatLng();
							updateToMarker(eventLatLng);
							toCoordDirty = false;
							GeocoderRequest gcReq = new GeocoderRequest();
			    			if (!GWT.isScript()) {
			    				   removeGwtObjectId(gcReq.getJso());
			    			}
			    			gcReq.setLatLng(toMarker.getPosition());
			    			gcReq.setRegion("ar");
			    			Geocoder gc = new Geocoder();
			    			gc.geocode(gcReq, new MyInverseGeocoderCallback("to"));
			    			resetSearch();
			    			Event.clearListeners(mapWidget.getMap(), "click");
			    			imgTo.setValue(false);
						}
					});
				}
			}		
		});
		panelImgTo.add(imgTo);
		FlowPanel panelTxtTo = new FlowPanel();
		panelTxtTo.setStyleName("textboxHolder");
		panelTo.add(panelTxtTo);
		txtTo = new TextBox();
		txtTo.getElement().setId("textbox-to");
		txtTo.setTitle(CONSTANTS.instr1());
		panelTxtTo.add(txtTo);
		
		Image imgShowFavsTo = new Image(resources.arrowDown());
		imgShowFavsTo.setAltText(CONSTANTS.showFavourites());
		imgShowFavsTo.setTitle(CONSTANTS.showFavourites());
		imgShowFavsTo.setStyleName("expandImage");
		panelTo.add(imgShowFavsTo);
		imgShowFavsTo.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if(!favouritesMenu.isVisible()) {
					fromOrToFavs = "to";
					if(txtTo.getText() != null && txtTo.getText().length() > 5) {
						currentStreetLabel.setText(txtTo.getText());
						starAdd.setResource(resources.starOutline());
					}
					DOM.setStyleAttribute(favouritesMenu.getElement(), "top", txtTo.getAbsoluteTop() + "px");
					DOM.setStyleAttribute(favouritesMenu.getElement(), "left", "46px");
					DOM.setStyleAttribute(favouritesMenu.getElement(), "height", favouritesMenu.getWidgetCount()*22 + 5 + "px");
					DOM.setStyleAttribute(favouritesMenu.getElement(), "width", (txtTo.getOffsetWidth()-26)+"px");
					favouritesMenu.setVisible(true);
				} else {
					favouritesMenu.setVisible(false);
					if(justsafed) {
						getPointsService.login(GWT.getHostPageBaseURL(), new AsyncCallback<LoginInfo>() {
							@Override
							public void onFailure(Throwable caught) {
								GWT.log("Error in login service (to update favourites)");
							}

							@Override
							public void onSuccess(LoginInfo result) {
								updateFavourites(result);
							}		
						});
					}
				}
			}	
		});
		
		FlowPanel checkBoxes = new FlowPanel();
		checkBoxes.setStyleName("checkboxesPanel");
		mainPanel.add(checkBoxes);
		InlineLabel lblColectivos = new InlineLabel(CONSTANTS.colectivos());
		SimpleCheckBox simpleCheckBoxColectivos = new SimpleCheckBox();
		simpleCheckBoxColectivos.setValue(true);
		simpleCheckBoxColectivos.setEnabled(false);
		checkBoxes.add(lblColectivos);
		checkBoxes.add(simpleCheckBoxColectivos);	
		InlineLabel lblSubte = new InlineLabel(CONSTANTS.lblSubte_text());
		simpleCheckBoxSubte = new SimpleCheckBox();
		simpleCheckBoxSubte.setValue(true);
		checkBoxes.add(lblSubte);
		checkBoxes.add(simpleCheckBoxSubte);		
		InlineLabel lblTrains = new InlineLabel(CONSTANTS.lblTrains_text());
		simpleCheckBoxTrains = new SimpleCheckBox();
		simpleCheckBoxTrains.setValue(true);
		checkBoxes.add(lblTrains);
		checkBoxes.add(simpleCheckBoxTrains);

		btnSearch = new Button(CONSTANTS.search());
		btnSearch.setStyleName("searchButton");
		mainPanel.add(btnSearch);
		
		resultsPanel = new StackLayoutPanel(Unit.PX);
		resultsPanel.setSize("100%", 0+"px");
		resultsPanel.setVisible(false);
		mainPanel.add(resultsPanel);
		
		/*FlowPanel mapPanel = new FlowPanel();
	    mainPanel.add(mapPanel);
	    mapPanel.setStyleName("mappanel");
//	    mapPanel.setHeight(Window.getClientWidth() + "px");
	    mapPanel.setHeight("300px");*/
		final MapOptions options = new MapOptions();
		options.setZoom(11);
		options.setCenter(new LatLng(-34.604389,-58.410873));
		options.setMapTypeId(new MapTypeId().getRoadmap());
		options.setDisableDefaultUI(true);
		options.setDraggable(true);
		options.setMapTypeControl(false);
		options.setScaleControl(true);
		options.setNavigationControl(true);
		options.setScrollwheel(true);
		options.setDisableDoubleClickZoom(true);
	    mapWidget = new MapWidget(options);
//	    mapWidget.setHeight("70%");
//	    mapWidget.setSize("100%", "70%");
	    int clientHeight = Window.getClientHeight();
	    if(clientHeight < 300) {
	    	clientHeight = 480;
	    }
	    mapWidget.setHeight(Math.round(clientHeight * 0.6f) + "px");
	    mainPanel.add(mapWidget);
//	    mapPanel.add(mapWidget);
	    
	    FlowPanel panelLinks = new FlowPanel();
	    panelLinks.setStyleName("linksPanel");
	    mainPanel.add(panelLinks);  
	    panelLogin = new SimplePanel();
	    panelLinks.add(panelLogin);
	    
	    if(Navigator.getUserAgent() != null && Navigator.getUserAgent().contains("Android") && Navigator.getUserAgent().contains("Mobile")) {
	    	SimplePanel panelAndroidMarket = new SimplePanel();
		    panelLinks.add(panelAndroidMarket);
			Anchor lnkAndroidMarket = new Anchor(CONSTANTS.lnkAndroidMarket());
			lnkAndroidMarket.setHref(CONSTANTS.hrefAndroidMarket());
			lnkAndroidMarket.setTarget("_blank");
			panelAndroidMarket.add(lnkAndroidMarket);
	    }
	    
	    SimplePanel panelImpressum = new SimplePanel();
	    panelLinks.add(panelImpressum);
	    Anchor lnkImpressum = new Anchor(CONSTANTS.lnkImpressum());
		lnkImpressum.setHref("/impressum.html");
		lnkImpressum.setTarget("_blank");
		panelImpressum.add(lnkImpressum);
		
		SimplePanel panelFAQ = new SimplePanel();
	    panelLinks.add(panelFAQ);
		Anchor lnkFAQ = new Anchor(CONSTANTS.lnkFAQ());
		lnkFAQ.setHref(CONSTANTS.hrefFAQ());
		lnkFAQ.setTarget("_blank");
		panelFAQ.add(lnkFAQ);
		
		SimplePanel panelWeb = new SimplePanel();
	    panelLinks.add(panelWeb);
		Anchor lnkWeb = new Anchor(CONSTANTS.lnkWeb());
		lnkWeb.setHref(new UrlBuilder().setProtocol("http").setHost(Window.Location.getHost().replaceFirst("m.", "www.")).setPath("/").buildString());
		lnkWeb.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Date date = new Date();
				long currentTime = date.getTime();
				date.setTime(currentTime + 2592000000L);
				Cookies.setCookie("mobile", "no", date);
				Window.Location.replace(new UrlBuilder().setProtocol("http").setHost(Window.Location.getHost().replaceFirst("m.", "www.")).setPath("/").buildString());
			}
			
		});
		panelWeb.add(lnkWeb);
	    
	    final FlowPanel selectLanguagePanel = new FlowPanel();
		selectLanguagePanel.setStyleName("selectLanguagePanel");
		mainPanel.add(selectLanguagePanel);
		selectLanguagePanel.setSize("20px", "30px");
		
		class LanguageChangeHandler implements ClickHandler {
			String lang;
			public LanguageChangeHandler(String lang) {
				this.lang = lang;
			}
			public void onClick(ClickEvent event) {
				if(!LocaleInfo.getCurrentLocale().getLocaleName().equalsIgnoreCase(lang)) {
					Date date = new Date();
					long currentTime = date.getTime();
					date.setTime(currentTime + 2592000000L);
					Cookies.setCookie("locale", lang, date);
					Window.Location.reload();
				}
			}
		}
		
		Image imgEsp = new Image(resources.languageEsp());
		imgEsp.addClickHandler(new LanguageChangeHandler("es"));
		imgEsp.setAltText(CONSTANTS.esp());
		imgEsp.addStyleName(imgEsp.getStylePrimaryName()+"-selectLanguage");
		selectLanguagePanel.add(imgEsp);
		
		Image imgEng = new Image(resources.languageEng());
		imgEng.addClickHandler(new LanguageChangeHandler("en"));
		imgEng.setAltText(CONSTANTS.eng());
		imgEng.addStyleName(imgEng.getStylePrimaryName()+"-selectLanguage");
		selectLanguagePanel.add(imgEng);
		
		FlowPanel spinningCirclePanel = new FlowPanel();
		spinningCirclePanel.setSize("120px", "50px");
		spinningCircle = new DecoratedPopupPanel(false);
		spinningCircle.setWidget(spinningCirclePanel);
		Image spinningImage = new Image("img/spinning.gif");
		spinningImage.setStyleName("spinningImage");
		spinningCirclePanel.add(spinningImage);
		Label searchingLabel = new Label(CONSTANTS.searching() + " ...");
		searchingLabel.setStyleName("searchingLabel");
		spinningCirclePanel.add(searchingLabel);
	    
	    try {
			final AutocompletePlace acFrom = new AutocompletePlace("textbox-from");
			if(acFrom != null) {
				Event.addListener(acFrom, "place_changed", new AutocompleteCallback(acFrom, "from"));
			}
			final AutocompletePlace acTo = new AutocompletePlace("textbox-to");
			if(acTo != null) {
				Event.addListener(acTo, "place_changed", new AutocompleteCallback(acTo, "to"));
			}
		} catch (Exception e) {
			GWT.log("Error while adding autocomplete: " , e);
		}
		
		class GeolocationPositionCallback implements PositionCallback {
    		public void onFailure(PositionError error) {
    			GWT.log("locate failed");
    		}
    		public void onSuccess(Position position) {
//    			GWT.log("Got location");
    			if(locationWatchId != -1 && (position.getCoords().getAccuracy() < 500 || stopGeoLocating)) {
    				Geolocation.getGeolocation().clearWatch(locationWatchId);
    				locationWatchId = -1;
    			}
    			Coordinates coords = position.getCoords();
    			double lat = coords.getLatitude();
    			double lon = coords.getLongitude();
    			/*if(lat > 0) {
    				lat = -34.679523; 
    				lon = -58.370533;
    			}*/
    			mapWidget.getMap().setCenter(new LatLng(lat, lon));
    			fromCoordDirty = false;
    			updateFromMarker(new LatLng(lat, lon));
    			mapWidget.getMap().setZoom(15);
    			GeocoderRequest gcReq = new GeocoderRequest();
    			if (!GWT.isScript()) {
    				   removeGwtObjectId(gcReq.getJso());
    			}
    			gcReq.setLatLng(fromMarker.getPosition());
    			gcReq.setRegion("ar");
    			Geocoder gc = new Geocoder();
    			gc.geocode(gcReq, new MyInverseGeocoderCallback("from"));
    			resetSearch();
    		}
    	}
		
		class MyGeocoderCallback extends GeocoderCallback {	
	    	private String fromOrTo;
	    	private boolean doSearch;
	    	public MyGeocoderCallback(String fromOrTo, boolean doSearch) {
	    		super();
	    		this.fromOrTo = fromOrTo;
	    		this.doSearch = doSearch;
	    	}
			@Override
			public void callback(List<HasGeocoderResult> responses, String status) {
//				GWT.log("geocoder: " + status);
				if(status.equalsIgnoreCase("OK")) {
					HasGeocoderResult resp = responses.get(0);
					if(fromOrTo.equalsIgnoreCase("from")) {
						fromCoordDirty = false;
						updateFromMarker(resp.getGeometry().getLocation());
						currentStreetLabel.setText(txtFrom.getText());
					} else if(fromOrTo.equalsIgnoreCase("to")) {
						toCoordDirty = false;
						updateToMarker(resp.getGeometry().getLocation());
						currentStreetLabel.setText(txtTo.getText());
					} else {
						GWT.log("Error: neither from nor to in MyGeocoderCallback.");
					}
					currentStreetLabel.getElement().setPropertyDouble("x_coord", resp.getGeometry().getLocation().getLatitude());
					currentStreetLabel.getElement().setPropertyDouble("y_coord", resp.getGeometry().getLocation().getLongitude());
					
					if(doSearch && !ranSearch && !fromCoordDirty && !toCoordDirty) {
						ranSearch = true;
	    				doSearch();
	    			} else if(!doSearch) {
	    				if(fromOrTo.equalsIgnoreCase("from")) {
	    					mapWidget.getMap().setCenter(fromMarker.getPosition());	
	    				}
	    				if(fromOrTo.equalsIgnoreCase("to")) {
	    					mapWidget.getMap().setCenter(toMarker.getPosition());			
	    				}
	    				mapWidget.getMap().setZoom(15);
	    			}
				} else {
					GWT.log("Geocoder failed with status: " + status);
				}
			}
		}
		
		class LocHandler implements ClickHandler {
			public void onClick(ClickEvent event) {
				if(event.getSource() == btnLocate) {
					if (Geolocation.isSupported()) {
						locationWatchId = -1;
						stopGeoLocating = false;
						int timeOut = 15000;
				    	Geolocation geo = Geolocation.getGeolocation();
				    	PositionOptions pOpts = PositionOptions.create();
						pOpts.setEnableHighAccuracy(false);
				    	geo.getCurrentPosition(new GeolocationPositionCallback(), pOpts);
				    	
				    	Geolocation geo2 = Geolocation.getGeolocation();
				    	PositionOptions pOpts2 = PositionOptions.create();
						pOpts2.setEnableHighAccuracy(true);
						pOpts2.setMaximumAge(60000);
						pOpts2.setTimeout(timeOut);
						locationWatchId = geo2.watchPosition(new GeolocationPositionCallback(), pOpts);
						Timer t = new Timer() {
							public void run() {
								stopGeoLocating = true;
							}
						};
						t.schedule(timeOut - 500);
				    }
				}
			}
		}
		
		class ToHandler implements ChangeHandler, ClickHandler, KeyPressHandler {
			@Override
			public void onClick(ClickEvent event) {
				search();
			}
			@Override
			public void onChange(ChangeEvent event) {
				Timer t = new Timer() { // delayed call so that it is not called if autocomplete was used
					public void run() {
						if(typedTo) {
							textUpdated();
						}
					}
				};
				t.schedule(150);
			}		
			@Override
			public void onKeyPress(KeyPressEvent event) {
				if(event.getCharCode() == KeyCodes.KEY_ENTER) {
					textUpdated();
				} else {
					typedTo = true;
				}
			}
			private void textUpdated() {
				toCoordDirty = true;
				GeocoderRequest gcReq = new GeocoderRequest();
				if (!GWT.isScript()) {
					removeGwtObjectId(gcReq.getJso());
				}
				gcReq.setRegion("ar");
				gcReq.setBounds(mapWidget.getMap().getBounds());
				gcReq.setAddress(txtTo.getText());
				Geocoder gc = new Geocoder();
				gc.geocode(gcReq, new MyGeocoderCallback("to", false));
				resetSearch();
			}	
			private void search() {
				ranSearch = false;
    			if(fromCoordDirty) {
    				GeocoderRequest gcReq1 = new GeocoderRequest();
        			if (!GWT.isScript()) {
        				   removeGwtObjectId(gcReq1.getJso());
        			}
    				gcReq1.setRegion("ar");
    				gcReq1.setBounds(mapWidget.getMap().getBounds());
    				gcReq1.setAddress(txtFrom.getText());
    				Geocoder gc1 = new Geocoder();
    				gc1.geocode(gcReq1, new MyGeocoderCallback("from", true));
    			}
    			if(toCoordDirty) {
    				GeocoderRequest gcReq2 = new GeocoderRequest();
    				if (!GWT.isScript()) {
    					removeGwtObjectId(gcReq2.getJso());
    				}
    				gcReq2.setRegion("ar");
    				gcReq2.setBounds(mapWidget.getMap().getBounds());
    				gcReq2.setAddress(txtTo.getText());
    				Geocoder gc2 = new Geocoder();
    				gc2.geocode(gcReq2, new MyGeocoderCallback("to", true));
    			}
    			if(!ranSearch && !fromCoordDirty && !toCoordDirty) {
    				ranSearch = true;
    				doSearch();
    			}
			}
		}
		
		class FromHandler implements ChangeHandler, KeyPressHandler {
			@Override
			public void onKeyPress(KeyPressEvent event) {
				if(event.getCharCode() == KeyCodes.KEY_ENTER) {
					textUpdated();
				} else {
					typedFrom = true;
				}
			}
			@Override
			public void onChange(ChangeEvent event) {
				Timer t = new Timer() { // delayed call so that it is not called if autocomplete was used
					public void run() {
						if(typedFrom) {
							textUpdated();
						}
					}
				};
				t.schedule(150);		
			}
			private void textUpdated() {
				fromCoordDirty = true;
				GeocoderRequest gcReq = new GeocoderRequest();
				if (!GWT.isScript()) {
					removeGwtObjectId(gcReq.getJso());
				}
				gcReq.setRegion("ar");
				gcReq.setBounds(mapWidget.getMap().getBounds());
				gcReq.setAddress(txtFrom.getText());
				Geocoder gc = new Geocoder();
				gc.geocode(gcReq, new MyGeocoderCallback("from", false));
				resetSearch();
			}
		}
		
		LocHandler locHandler = new LocHandler();
		btnLocate.addClickHandler(locHandler);
		ToHandler toHandler = new ToHandler();
		btnSearch.addClickHandler(toHandler);
		txtTo.addChangeHandler(toHandler);
		txtTo.addKeyPressHandler(toHandler);
		FromHandler fromHandler = new FromHandler();
		txtFrom.addChangeHandler(fromHandler);
		txtFrom.addKeyPressHandler(fromHandler);
		
		currentStreetPanel = new FlowPanel();
	    currentStreetPanel.setStyleName("favouritesMenuItem");
	    DOM.setStyleAttribute(currentStreetPanel.getElement(), "width", (txtTo.getOffsetWidth()-45)+"px");
	    currentStreetLabel = new Label(CONSTANTS.youCanStoreFavourites());
	    currentStreetLabel.addStyleName("favouritesMenuLabel");
	    currentStreetPanel.add(currentStreetLabel);
	    DOM.setStyleAttribute(currentStreetLabel.getElement(), "width", (txtTo.getOffsetWidth()-71)+"px");
		currentStreetLabel.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				favouritesMenu.setVisible(false);
			}
		});
		
		starAdd = new Image(resources.starOutline());
	    starAdd.addStyleName("favouritesMenuStar");
	    starAdd.setTitle(CONSTANTS.addFavourite());
	    starAdd.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				UserFavouritePositionProxy fpp;
				if(!currentStreetLabel.getText().equalsIgnoreCase(CONSTANTS.youCanStoreFavourites())) {
					if(fromOrToFavs.equalsIgnoreCase("from")) {
						fpp = new UserFavouritePositionProxy(txtFrom.getText(), fromMarker.getPosition().getLatitude(), fromMarker.getPosition().getLongitude());
					} else if(fromOrToFavs.equalsIgnoreCase("to")) {
						fpp = new UserFavouritePositionProxy(txtTo.getText(), toMarker.getPosition().getLatitude(), toMarker.getPosition().getLongitude());
					} else {
						GWT.log("ERROR in add favourite handler - neither from nor to");
						fpp = null;
					}
					getPointsService.addFavourite(fpp, new AsyncCallback<UserFavouritePositionProxy>() {
						@Override
						public void onFailure(Throwable caught) {
							showMessageBox(CONSTANTS.errorWhileSavingFavourite());
						}
	
						@Override
						public void onSuccess(UserFavouritePositionProxy result) {
							starAdd.setResource(resources.starFull());
							justsafed = true;
						}
						
					});
				}
			}
	    	
	    });
	    starAdd.setAltText(CONSTANTS.addFavourite());
		currentStreetPanel.add(starAdd);
	    favouritesMenu.add(currentStreetPanel);
	    
	    resultsPanel.addSelectionHandler(new SelectionHandler<Integer>() {
			@Override
			public void onSelection(SelectionEvent<Integer> event) {
				if (resultsPanel.getVisibleIndex() != selectedIndex) {
	                selectedIndex = resultsPanel.getVisibleIndex();
//	                GWT.log("index: " + selectedIndex);
	                drawConnection(selectedIndex);
	            }
			}	
		});

		getPointsService.login(GWT.getHostPageBaseURL() + "/m.html", new AsyncCallback<LoginInfo>() {
			@Override
			public void onFailure(Throwable caught) {
				GWT.log("Error in login service");
			}
			@Override
			public void onSuccess(LoginInfo result) {
				Anchor loginOrOutLink;
				currentLogin = result;
				if(result.isLoggedIn()) {
					loginOrOutLink = new Anchor(CONSTANTS.logout() + " (" + result.getEmailAddress() + ")");
					loginOrOutLink.setHref(result.getLogoutUrl());
				} else {
					loginOrOutLink = new Anchor(CONSTANTS.login());
					loginOrOutLink.setHref(result.getLoginUrl());
					loginOrOutLink.setTitle(CONSTANTS.saveFavs());
				}
				updateFavourites(result);
				panelLogin.add(loginOrOutLink);
			}		
		});
		Timer t = new Timer() {
			public void run() {
				if(Window.getScrollTop() < 1) {
					Window.scrollTo(0, 1);
				}
			}
		};
		/*t.schedule(100);
		t.schedule(250);*/
		t.schedule(500);
	}

	private void showMessageBox(String message) {
		hideSpinningCircle();
		final DecoratedPopupPanel simplePopup = new DecoratedPopupPanel(true);
	    simplePopup.setWidth("150px");
	    simplePopup.setWidget(new HTML(message));
	    simplePopup.addStyleName("popup");
	    simplePopup.center();
	}
	
	private void showSpinningCircle() {
	    spinningCircle.center();
	}
	
	private void hideSpinningCircle() {
		spinningCircle.hide();
	}
	
	private void showSelectFromMap() {
		if(!showedSelectFromMapOnce) {
			showedSelectFromMapOnce = true;
			final DecoratedPopupPanel simplePopup = new DecoratedPopupPanel(true);
		    simplePopup.setWidth("150px");
		    simplePopup.setWidget(new HTML(CONSTANTS.selectFromMap()));
		    simplePopup.addStyleName("popup");
		    simplePopup.center();
		    Timer t = new Timer() {
				public void run() {
					simplePopup.hide();
				}
			};
			t.schedule(2000);
		}
	}
	
	
	private void searchError(Throwable e) {
		searchRunning = false;
		resetSearch();
	    showMessageBox(CONSTANTS.errorTryAgainLater());
	}
	
	private void createMarker(HasLatLng coord, String fromOrTo) {
		if(fromOrTo.equalsIgnoreCase("from")) {
			fromMarker = new Marker(getDefaultMarkerOptions(coord));    
			MarkerImage.Builder imageBuilder = new MarkerImage.Builder("img/red_MarkerA.png");
			fromMarker.setIcon(imageBuilder.build());
			Event.addListener(fromMarker, "dragend", new DragEventCallback("from"));
		} else if(fromOrTo.equalsIgnoreCase("to")) {
			toMarker = new Marker(getDefaultMarkerOptions(coord));   
			MarkerImage.Builder imageBuilder = new MarkerImage.Builder("img/red_MarkerB.png");
			toMarker.setIcon(imageBuilder.build());
			Event.addListener(toMarker, "dragend", new DragEventCallback("to"));
		} else {
			GWT.log("Error: neither from nor to in createMarker().");
		}
	}
	
	private MarkerOptions getDefaultMarkerOptions(HasLatLng coord) {
		MarkerOptions markerOpts = new MarkerOptions();
		markerOpts.setPosition(coord);
		markerOpts.setVisible(true);
		markerOpts.setMap(mapWidget.getMap());
		markerOpts.setDraggable(true);
		return markerOpts;
	}
	
	private void resetSearch() {
		searchmode = 0;
		if(!searchRunning) {
			hideSpinningCircle();
			btnSearch.setEnabled(true);
			btnSearch.setText(CONSTANTS.search());
		}
	}
	
	private void doSearch() {
		if(searchRunning) {
			showMessageBox(CONSTANTS.searchAlreadyRunning());
		} else {
			searchRunning = true;
			fitMapOnMarkers();
			btnSearch.setEnabled(false);
			showSpinningCircle();
			if(searchmode == 0) {
				getDirectConnections();
			} else if(searchmode == 1) {
				getIndirectConnections(); 
			}
		}
	}
	
	private void getDirectConnections() {
		float lat1 = (float)fromMarker.getPosition().getLatitude();
		float lon1 = (float)fromMarker.getPosition().getLongitude();
		float lat2 = (float)toMarker.getPosition().getLatitude();
		float lon2 = (float)toMarker.getPosition().getLongitude();
		GWT.log("starting direct search");
		listPointsService.getDirectConnections(lat1, lon1, lat2, lon2, !simpleCheckBoxTrains.getValue(), !simpleCheckBoxSubte.getValue(), 
				new AsyncCallback<SearchResultProxy>() {

			public void onFailure(Throwable caught) {
				searchRunning = false;
				GWT.log("Error in getDirectConnections", caught);
				searchError(caught);
			}
			public void onSuccess(SearchResultProxy result) {
				searchRunning = false;
				mlkSet1 = result.getMlkSet1String();
				mlkSet2 = result.getMlkSet2String();
				searchmode = 1;
				btnSearch.setEnabled(true);
				hideSpinningCircle();
				resultList = new LinkedList<SearchResultProxy>();
				resultList.add(result);
				if(result.getError() == null) {
					btnSearch.setText(CONSTANTS.searchIndirect());
					resultFromDirectSearch = result;
					showResults();
				} else { // if no results, try indirect search straight ahead
					if(result.getError().equals(SearchResultProxy.noResults)) {
						/*searchErrorPopup = new DecoratedPopupPanel(true);
						searchErrorPopup.setWidth("150px");
						searchErrorPopup.setWidget(new HTML(CONSTANTS.noResultsSearchingIndirect()));
						searchErrorPopup.center();*/
						resultFromDirectSearch = null;
						btnSearch.setText(CONSTANTS.searchIndirect());
						doSearch();
					} else if(result.getError().equals(SearchResultProxy.tooManyReqests)) {
						showMessageBox(CONSTANTS.tooManyRequests());
					} else if(result.getError().equals(SearchResultProxy.distanceTooLittle)) {
						showMessageBox(CONSTANTS.minDistance());
					} else {
						showMessageBox("Error: " + result.getError());
					}
				}
			}
		});
	}
	
	private void getIndirectConnections() {
		GWT.log("Starting indirect searches");
		int partitionMaxSize = 40;
		int partitions = 0;
		if(mlkSet1 != null && mlkSet1.size() != 0) {
			partitions = (mlkSet1.size() / partitionMaxSize)+1;
		}
		activeIndirectSearches = 0;
		gotAnIndirectResult = false;
		if(partitions == 0) {
			indirectSearchFinished();
		} else {
			for(int i = 0; i < partitions; i++) {
				GWT.log("Starting indirect search number: " + i);
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
				float lat1 = (float)fromMarker.getPosition().getLatitude();
				float lon1 = (float)fromMarker.getPosition().getLongitude();
				float lat2 = (float)toMarker.getPosition().getLatitude();
				float lon2 = (float)toMarker.getPosition().getLongitude();
				activeIndirectSearches++;
				listPointsService.getIndirectConnections(lat1, lon1, lat2, lon2, !simpleCheckBoxTrains.getValue(), !simpleCheckBoxSubte.getValue(), mlkSet1Part, mlkSet2, 
						new AsyncCallback<SearchResultProxy>() {

					public void onFailure(Throwable caught) {
						activeIndirectSearches--;
						GWT.log("Error in getIndirectConnections", caught);
						if(activeIndirectSearches == 0){
							indirectSearchFinished();
						}
						searchError(caught);
					}
					public void onSuccess(SearchResultProxy result) {
						activeIndirectSearches--;
						GWT.log("Result Error Msg: " + result.getError());
						if(result.getError() == null) {
							gotAnIndirectResult = true;
							if(resultList.size() <= 1) {
								resultList.add(result);
							} else {
								List<ConnectionProxy> existingIndirectConns = resultList.get(resultList.size()-1).getConnections();
								if(!existingIndirectConns.contains(result.getConnections().get(0))) { // indirect search only brings one result, so I just check whether that result already exists (it is possible that two searches yield the same result)
									existingIndirectConns.addAll(result.getConnections());
								}
							}
						} else {
							if(result.getError().equals(SearchResultProxy.noResults)) {
								// do nothing because it is treated later
							} else {
								if(result.getError().equals(SearchResultProxy.tooManyReqests)) {
									showMessageBox(CONSTANTS.tooManyRequests());
								} else if(result.getError().equals(SearchResultProxy.distanceTooLittle)) {
									showMessageBox(CONSTANTS.minDistance());
								} else {
									showMessageBox("Error: " + result.getError());
								}
							}
						}
						if(activeIndirectSearches == 0){
							indirectSearchFinished();
						}
					}
				});	
			}
		}
	}
	
	private void indirectSearchFinished() {
		searchRunning = false;
		resetSearch();
		if(searchErrorPopup != null) {
			searchErrorPopup.hide();
		}
		if(gotAnIndirectResult) {
			Collections.reverse(resultList);
			Collections.sort(resultList.get(0).getConnections(), new ConnectionProxyComparator());
			showResults();
		} else {
			showMessageBox(CONSTANTS.noResults());
		}
	}
	
	private void showResults() {
		resultsPanel.setVisible(false);
		resultsPanel.clear();
		boolean gotAResult = false;
		int stackPanelTotalHeight = 0;
		int biggestContentHeight = 0;
		for(SearchResultProxy resultProxy : resultList) {
			if(resultProxy.getConnections() != null) {
				for(ConnectionProxy connProxy : resultProxy.getConnections()) {
					if(connProxy != null) {
						gotAResult = true;
						int col = 0;
						String connection = connProxy.getTime() + CONSTANTS.min() + " " + CONSTANTS.with() + " ";
						FlowPanel connectionDetailsPanelnew = new FlowPanel();
						for(LineProxy l : connProxy.getLines()) {
							if(col >= colors.size()) {
								col = col % colors.size();
							}
							if(l.getType() != 0) {
								String lineText = l.getLinenum();
								if(l.getType() == 1 && connProxy.getLines().size() == 3) {
									String[] parts1 = l.getRamal().split("-");
									if(parts1.length == 2) {
										lineText += " " + parts1[0].substring(0, parts1[0].length() - 1);
									}
								}
								connection += l.getTypeAsString() + " ";
								connection += lineText + ", ";
								InlineLabel lineNum = new InlineLabel(l.getLinenum() + " ");
								connectionDetailsPanelnew.add(lineNum);
								lineNum.getElement().setAttribute("style", "color:" + colors.get(col) + ";font-size:120%");
								col++;
								InlineLabel lineRamal = new InlineLabel(" " + l.getRamal());
								lineRamal.getElement().setAttribute("style", "font-size:120%");
								connectionDetailsPanelnew.add(lineRamal);
								if(l.getAlternativeLines().size() > 0) {
									String alternativesText = " (" + CONSTANTS.alternatives() + ": ";
									for(String s : l.getAlternativeLines()) {
										alternativesText += s + ", ";
									}
									alternativesText = alternativesText.substring(0, alternativesText.length() - 2);
									alternativesText += ")";
									InlineLabel alternatives = new InlineLabel(alternativesText);
									connectionDetailsPanelnew.add(alternatives);
								}
								if(l.getStartStreet() != null && l.getDestStreet() != null) {
									FlowPanel streets = new FlowPanel();
									InlineLabel from = new InlineLabel(CONSTANTS.from() + " ");
									streets.add(from);
									InlineLabel fromStreet = new InlineLabel(l.getStartStreet());
									fromStreet.addStyleName("resultPosLink");
									fromStreet.getElement().setPropertyDouble("x_coord", l.getRelevantPoints().get(0));
									fromStreet.getElement().setPropertyDouble("y_coord", l.getRelevantPoints().get(1));
									fromStreet.addClickHandler(new ClickHandler() {
										@Override
										public void onClick(ClickEvent event) {
											InlineLabel source = (InlineLabel)event.getSource();
											panToAndMark(source.getElement().getPropertyDouble("x_coord"), source.getElement().getPropertyDouble("y_coord"));
										}					
									});
									streets.add(fromStreet);
									InlineLabel to = new InlineLabel(" " + CONSTANTS.to() + " ");
									streets.add(to);
									InlineLabel toStreet = new InlineLabel(l.getDestStreet());
									toStreet.addStyleName("resultPosLink");
									toStreet.getElement().setPropertyDouble("x_coord", l.getRelevantPoints().get(l.getRelevantPoints().size()-2));
									toStreet.getElement().setPropertyDouble("y_coord", l.getRelevantPoints().get(l.getRelevantPoints().size()-1));
									toStreet.addClickHandler(new ClickHandler() {
										@Override
										public void onClick(ClickEvent event) {
											InlineLabel source = (InlineLabel)event.getSource();
											panToAndMark(source.getElement().getPropertyDouble("x_coord"), source.getElement().getPropertyDouble("y_coord"));
										}					
									});
									streets.add(toStreet);
									connectionDetailsPanelnew.add(streets);
								}
							}
						}
						connection = connection.substring(0, connection.length()-2);
						Label lConnTemp = new Label(connection);
						Label lConn = new Label(connection);
						lConnTemp.setStyleName("gwt-StackLayoutPanelHeader");
						lConnTemp.setWidth("100%");
						dummyPanel.add(lConnTemp);
						int heightHeader = lConnTemp.getElement().getOffsetHeight();
						dummyPanel.remove(lConnTemp);
						
						connectionDetailsPanelnew.setStyleName("gwt-StackLayoutPanelContent");
						connectionDetailsPanelnew.setWidth("100%");
						dummyPanel.add(connectionDetailsPanelnew);
						int heightContent = connectionDetailsPanelnew.getElement().getOffsetHeight() + 5;
		//				GWT.log("eleD: " +lConnDetailsTemp.getElement().getOffsetHeight() + ", widgetD: " + lConnDetailsTemp.getOffsetHeight());
						dummyPanel.remove(connectionDetailsPanelnew);
						resultsPanel.add(connectionDetailsPanelnew, lConn, heightHeader);
						stackPanelTotalHeight += heightHeader;
						if(heightContent > biggestContentHeight) {
							biggestContentHeight = heightContent;
						}
					}
				}
			}
		}
		stackPanelTotalHeight += biggestContentHeight + 5;
//		GWT.log("Total height: " + stackPanelTotalHeight);
		resultsPanel.setHeight(stackPanelTotalHeight + "px");
		resultsPanel.setVisible(true);
		if(gotAResult) {
			drawConnection(0);
		}
	}
	
	private void drawConnection(int index) {
		int counter = 0;
		ConnectionProxy conn = null;
		for(SearchResultProxy srProxy : resultList) {
			if(srProxy.getConnections() != null) {
				for(ConnectionProxy cProxy : srProxy.getConnections()) {
					if(counter == index) {
						conn = cProxy;
					}
					counter++;
				}
			}
		}
		for(Polyline pl : polyLines) {
			pl.setMap(NullMap.getInstance());
		}
		for(OverlayView ol : stations) {
			ol.setMap(NullMap.getInstance());
		}
		polyLines.clear();
		stations.clear();
		int col = 0;
		if(conn != null) {
			for(int i = 0; i < conn.getLines().size(); i++) {
				LineProxy lp = conn.getLines().get(i);
				if(lp.getType() != 0 || (i != 0 && i != conn.getLines().size()-1)) {
					if(col >= colors.size()) {
						col = col % colors.size();
					}
					if(lp.getType() != 0) {
						List<HasLatLng> pathFull = new LinkedList<HasLatLng>();
						for(int j = 0; j < lp.getAllPoints().size(); j = j+2) {
							HasLatLng latlng = new LatLng(lp.getAllPoints().get(j), lp.getAllPoints().get(j+1));
							pathFull.add(latlng);
							if(lp.getStations() != null) {
								OverlayView ov;
								if(lp.getType() == 3) {
									ov = new StationOverlayView(lp.getStations().get(j/2), latlng, colors.get(col), 14);
								} else {
									ov = new StationOverlayView(lp.getStations().get(j/2), latlng, colors.get(col), 15);
								}
								ov.setMap(mapWidget.getMap());
								stations.add(ov);
							}
						}
						PolylineOptions plOptsFull = new PolylineOptions();
						plOptsFull.setPath(pathFull);
						plOptsFull.setStrokeColor(colors.get(col));
						plOptsFull.setStrokeOpacity(0.4);
						plOptsFull.setStrokeWeight(2);
						Polyline plFull = new Polyline(plOptsFull);
						plFull.setMap(mapWidget.getMap());
						polyLines.add(plFull);
					}
					
					List<HasLatLng> pathReal = new LinkedList<HasLatLng>();
					for(int j = 0; j < lp.getRelevantPoints().size(); j = j+2) {
						HasLatLng latlng = new LatLng(lp.getRelevantPoints().get(j), lp.getRelevantPoints().get(j+1));
						pathReal.add(latlng);
					}
					PolylineOptions plOptsReal = new PolylineOptions();
					plOptsReal.setPath(pathReal);
					if(lp.getType() == 0) {
						plOptsReal.setStrokeColor("#000000");
					} else {
						plOptsReal.setStrokeColor(colors.get(col));
						col++;
					}
					plOptsReal.setStrokeOpacity(1.0);
					plOptsReal.setStrokeWeight(3);
					Polyline plReal = new Polyline(plOptsReal);
					plReal.setMap(mapWidget.getMap());
					polyLines.add(plReal);
				}
			}
		} else {
			GWT.log("conn is null. counter: " + counter);
		}
	}
	
	private native void removeGwtObjectId(JavaScriptObject jso) /*-{
	   delete jso['__gwt_ObjectId'];
	}-*/;
	
	private void fitMapOnMarkers() {
		double currLatSW;
		double currLatNE;
		double currLonSW;
		double currLonNE;
		if(fromMarker.getPosition().getLatitude() < toMarker.getPosition().getLatitude()) {
			currLatSW = fromMarker.getPosition().getLatitude();
			currLatNE = toMarker.getPosition().getLatitude();
		} else {
			currLatSW = toMarker.getPosition().getLatitude();
			currLatNE = fromMarker.getPosition().getLatitude();
		}
		if(fromMarker.getPosition().getLongitude() < toMarker.getPosition().getLongitude()) {
			currLonSW = fromMarker.getPosition().getLongitude();
			currLonNE = toMarker.getPosition().getLongitude();
		} else {
			currLonSW = toMarker.getPosition().getLongitude();
			currLonNE = fromMarker.getPosition().getLongitude();
		}
		mapWidget.getMap().fitBounds(new LatLngBounds(new LatLng(currLatSW, currLonSW), new LatLng(currLatNE, currLonNE)));
//		mapWidget.getMap().panToBounds(new LatLngBounds(new LatLng(currLatSW, currLonSW), new LatLng(currLatNE, currLonNE)));
	}
	
	private void panToAndMark(double x, double y) {
		mapWidget.getMap().setZoom(15);
		mapWidget.getMap().panTo(new LatLng(x, y));
		if(markerCircle == null) {
			createMarkerCircle(x, y);
		} else {
			markerCircle.setCenter(new LatLng(x, y));
		}
		Window.scrollTo(0, mapWidget.getAbsoluteTop());
	}
	
	private void createMarkerCircle(double x, double y) {
		CircleOptions circleOpts = new CircleOptions();
		circleOpts.setCenter(new LatLng(x, y));
		circleOpts.setFillColor("#ff0000");
		circleOpts.setFillOpacity(0.3);
		circleOpts.setRadius(100);
		circleOpts.setStrokeOpacity(0.0);
		markerCircle = new Circle(circleOpts);
		markerCircle.setMap(mapWidget.getMap());
	}
	
	private void updateFromMarker(HasLatLng latlng) {
		if(fromMarker == null) {
			createMarker(latlng, "from");
		} else {
			fromMarker.setPosition(latlng);
		}
	}
	
	private void updateToMarker(HasLatLng latlng) {
		if(toMarker == null) {
			createMarker(latlng, "to");
		} else {
			toMarker.setPosition(latlng);
		}
	}
	
	class AutocompleteCallback extends EventCallback {
		
		AutocompletePlace instance;
		String fromOrTo;
		
		AutocompleteCallback(AutocompletePlace instance, String fromOrTo) {
			this.instance = instance;
			this.fromOrTo = fromOrTo;
		}
		
		@Override
		public void callback() {
			if(fromOrTo.equalsIgnoreCase("from")) {
				typedFrom = false;
				fromCoordDirty = false;
				if(fromMarker == null) {
					createMarker(instance.getLatLng(), "from");
    			} else {
    				fromMarker.setPosition(instance.getLatLng());
    			}
			} else if(fromOrTo.equalsIgnoreCase("to")) {
				typedTo = false;
				toCoordDirty = false;
				if(toMarker == null) {
					createMarker(instance.getLatLng(), "to");
    			} else {
    				toMarker.setPosition(instance.getLatLng());
    			}
			}
			mapWidget.getMap().setCenter(instance.getLatLng());
			mapWidget.getMap().setZoom(15);
			resetSearch();
		}
	}
	
	class DragEventCallback extends EventCallback {
    	
    	String fromOrTo;
    	
    	DragEventCallback(String fromOrTo) {
    		super();
    		this.fromOrTo = fromOrTo;
    	}

		@Override
		public void callback() {
			if(fromOrTo.equalsIgnoreCase("from")) {
//				GWT.log("event called for from-marker " + fromMarker.getPosition().getLatitude());
				fromCoordDirty = false;
				GeocoderRequest gcReq = new GeocoderRequest();
    			if (!GWT.isScript()) {
    				   removeGwtObjectId(gcReq.getJso());
    			}
    			gcReq.setLatLng(fromMarker.getPosition());
    			gcReq.setRegion("ar");
    			Geocoder gc = new Geocoder();
    			gc.geocode(gcReq, new MyInverseGeocoderCallback("from"));
			} else if(fromOrTo.equalsIgnoreCase("to")) {
//				GWT.log("event called for to-marker " + toMarker.getPosition().getLatitude());
				toCoordDirty = false;
				GeocoderRequest gcReq = new GeocoderRequest();
    			if (!GWT.isScript()) {
    				   removeGwtObjectId(gcReq.getJso());
    			}
    			gcReq.setLatLng(toMarker.getPosition());
    			gcReq.setRegion("ar");
    			Geocoder gc = new Geocoder();
    			gc.geocode(gcReq, new MyInverseGeocoderCallback("to"));
			} else {
				GWT.log("Error: neither from nor to in DragEventCallback.");
			}
			resetSearch();
		}
    }
	
	class MyInverseGeocoderCallback extends GeocoderCallback {
		
		String fromOrTo;
		
		public MyInverseGeocoderCallback(String fromOrTo) {
			super();
			this.fromOrTo = fromOrTo;
		}
		
		@Override
		public void callback(List<HasGeocoderResult> responses, String status) {
//			GWT.log("geocoder: " + status);
			if(status.equalsIgnoreCase("OK")) {
				HasGeocoderResult resp = responses.get(0);
				String text = resp.getAddressComponents().get(1).getShortName() + " " + resp.getAddressComponents().get(0).getShortName();
				if(fromOrTo.equalsIgnoreCase("from")) {
					txtFrom.setText(text);
				} else if(fromOrTo.equalsIgnoreCase("to")) {
					txtTo.setText(text);
				} else {
					GWT.log("Error: neither from nor to in MyInverseGeocoderCallback.");
				}
			} else {
				GWT.log("Inverse Geocoder failed with status: " + status);
			}
		}
	}
	
	private void updateFavourites(LoginInfo result) {
		justsafed = false;
		currentStreetLabel.setText(CONSTANTS.youCanStoreFavourites());
		favouritesMenu.clear();
		favouritesMenu.add(currentStreetPanel);
		if(result.isLoggedIn()) {
			List<UserFavouritePositionProxy> favs = result.getFavourites();
			if (favs != null && favs.size() > 0) {
				for(UserFavouritePositionProxy fav : favs) { 
					FlowPanel pan = new FlowPanel();
					pan.setStyleName("favouritesMenuItem");
					DOM.setStyleAttribute(pan.getElement(), "width", (txtTo.getOffsetWidth()-45)+"px");
					Label favourite = new Label(fav.getName());
					favourite.addStyleName("favouritesMenuLabel");
					DOM.setStyleAttribute(favourite.getElement(), "width", (txtTo.getOffsetWidth()-71)+"px");
					pan.add(favourite);
					favourite.getElement().setPropertyDouble("x_coord", fav.getLat());
					favourite.getElement().setPropertyDouble("y_coord", fav.getLon());
					favourite.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							Label source = (Label)event.getSource();
							double x = source.getElement().getPropertyDouble("x_coord");
							double y = source.getElement().getPropertyDouble("y_coord");
							if(fromOrToFavs.equalsIgnoreCase("from")) {
								txtFrom.setText(source.getText());
								if(fromMarker == null) {
									createMarker(new LatLng(x, y), "from");
								} else {
									fromMarker.setPosition(new LatLng(x, y));
								}
								mapWidget.getMap().panTo(fromMarker.getPosition());
							} else if(fromOrToFavs.equalsIgnoreCase("to")) {
								txtTo.setText(source.getText());
								if(toMarker == null) {
									createMarker(new LatLng(x, y), "to");
								} else {
									toMarker.setPosition(new LatLng(x, y));
								}
								mapWidget.getMap().panTo(toMarker.getPosition());
							} else {
								GWT.log("Error in eventhandler for a label in the favourites menu. Neither from nor to was set");
							}
							favouritesMenu.setVisible(false);
						}
					});
					Image starRem = new Image(resources.starFull());
					starRem.getElement().setPropertyString("id_string", fav.getKey());
					starRem.addClickHandler(new ClickHandler() {

						@Override
						public void onClick(ClickEvent event) {
							String key = ((Image)event.getSource()).getElement().getPropertyString("id_string");
							favouritesMenu.remove(((Widget)event.getSource()).getParent());
							/*Timer t = new Timer() {
								public void run() {
									favouritesMenu.setVisible(false);
								}
							};
							t.schedule(1000);*/	
//							((Image)event.getSource()).setResource(resources.starOutline());
							getPointsService.removeFavourite(key, new AsyncCallback<String>() {

								@Override
								public void onFailure(Throwable caught) {
									showMessageBox(CONSTANTS.errorWhileDeletingFavourite());
								}

								@Override
								public void onSuccess(String result) {
									justsafed = true;
								}

							});
						}

					});
					starRem.setAltText(CONSTANTS.deleteFavourite());
					starRem.addStyleName("favouritesMenuStar");
					pan.add(starRem);
					favouritesMenu.add(pan);
				}
			}
		} else {
			Anchor hinweis = new Anchor(CONSTANTS.logInToStoreFavs());
			hinweis.setStyleName("favouritesMenuItem");
			hinweis.setHref(result.getLoginUrl());
			favouritesMenu.add(hinweis);
		}
	}
}
