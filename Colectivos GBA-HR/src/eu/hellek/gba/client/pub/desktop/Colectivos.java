package eu.hellek.gba.client.pub.desktop;

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
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.maps.client.MapOptions;
import com.google.gwt.maps.client.MapTypeId;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.base.HasLatLng;
import com.google.gwt.maps.client.base.HasPoint;
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
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimpleCheckBox;
import com.google.gwt.user.client.ui.StackLayoutPanel;
import com.google.gwt.user.client.ui.TextBox;
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
import eu.hellek.gba.client.pub.desktop.resources.Resources;
import eu.hellek.gba.shared.ConnectionProxy;
import eu.hellek.gba.shared.ConnectionProxyComparator;
import eu.hellek.gba.shared.LineProxy;
import eu.hellek.gba.shared.LoginInfo;
import eu.hellek.gba.shared.SearchResultProxy;
import eu.hellek.gba.shared.UserFavouritePositionProxy;

public class Colectivos implements EntryPoint {
	private static final AppConstants CONSTANTS = GWT.create(AppConstants.class);
	private static final Resources resources = GWT.create(Resources.class);
	
	private final ListPointsServiceAsync listPointsService = GWT.create(ListPointsService.class);
	private final GetPointsServiceAsync getPointsService = GWT.create(GetPointsService.class);
	
	private List<String> mlkSet1 = null; // keys of lines in results of direct search. they are ignored in indirect search
	private List<String> mlkSet2 = null; 
	
	private MapWidget mapWidget;
	
	private boolean fromCoordDirty = true; // true if it is necessary to do geocoding stuff to get the coordinates before being able to start the search
	private boolean toCoordDirty = true;
	boolean ranSearch; // since the above use callbacks, this var is set to avoid starting the search twice
	private boolean typedFrom = false;
	private boolean typedTo = false;
	
	private Marker fromMarker;
	private Marker toMarker;
	
	private TextBox txtTo;
	private TextBox txtFrom;
	
	private Button btnSearch;
	private int searchmode = 0; // 0 = direct, 1 = indirect
	/*
	private static final double latSW = -35.1; // boundaries for stuff like optimizing geocoding-results
	private static final double lonSW = -59.25;
	private static final double latNE = -34.05;
	private static final double lonNE = -57.8;*/
	
	private HasLatLng eventLatLng;
	private SearchResultProxy resultFromDirectSearch;
	private List<SearchResultProxy> resultList;
	
	private SimpleCheckBox simpleCheckBoxTrains;
	private SimpleCheckBox simpleCheckBoxSubte;
	private StackLayoutPanel resultsPanel;
	private FlowPanel flowPanelSouth;
	private AbsolutePanel dummyPanel;
	private int selectedIndex = -5;
	private List<Polyline> polyLines = new LinkedList<Polyline>();
	private List<OverlayView> stations = new LinkedList<OverlayView>();
	private List<String> colors = new LinkedList<String>();
	private Circle markerCircle = null;
	private DecoratedPopupPanel searchErrorPopup;
	private LoginInfo currentLogin;
	private VerticalPanel favouritesMenu;
	private String fromOrToFavs; // set to from if it is displayed under the from field, or to if displayed under the to field
	private Image starAdd;
	private FlowPanel currentStreetPanel;
	private Label currentStreetLabel;
	private boolean justsafed = false;
	private int activeIndirectSearches;
	private boolean gotAnIndirectResult;
	private boolean searchRunning = false;
	private DecoratedPopupPanel spinningCircle;
	private ScrollPanel scrollPanel;
	
	private static final int resultsWidth = 300;

	public void onModuleLoad() {
		final RootLayoutPanel rootPanel = RootLayoutPanel.get();
//		Window.setTitle(CONSTANTS.appTitle());
		
		dummyPanel = new AbsolutePanel();
		dummyPanel.setStyleName("gwt-StackLayoutPanel");
		dummyPanel.setSize(resultsWidth + "px", "500px");
		rootPanel.add(dummyPanel);
		dummyPanel.getElement().setAttribute("style", "border-bottom: none;");
		
	    final AbsolutePanel contextMenu = new AbsolutePanel();
	    contextMenu.setStyleName("contextMenu");
	    contextMenu.setVisible(false);
	    DOM.setElementAttribute(contextMenu.getElement(), "id", "contextMenuDiv");
	    rootPanel.add(contextMenu);
	    
	    final Label lblSetFrom = new Label(CONSTANTS.lblSetFrom_text());
	    lblSetFrom.setStyleName("contextMenuLabel");
	    contextMenu.add(lblSetFrom);
	    
	    final Label lblSetTo = new Label(CONSTANTS.lblSetTo_text());
	    lblSetTo.setStyleName("contextMenuLabel");
	    contextMenu.add(lblSetTo);
	    
	    favouritesMenu = new VerticalPanel();
	    favouritesMenu.setStyleName("favouritesMenu");
	    favouritesMenu.setVisible(false);
	    DOM.setElementAttribute(favouritesMenu.getElement(), "id", "favouritesMenuDiv");
	    DOM.setStyleAttribute(favouritesMenu.getElement(), "left", "50px");
	    rootPanel.add(favouritesMenu);
	    
	    currentStreetPanel = new FlowPanel();
	    currentStreetPanel.setStyleName("favouritesMenuItem");
	    currentStreetLabel = new Label(CONSTANTS.youCanStoreFavourites());
	    currentStreetLabel.addStyleName("favouritesMenuLabel");
	    currentStreetPanel.add(currentStreetLabel);
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
	    
		final DockLayoutPanel dockLayoutPanel = new DockLayoutPanel(Unit.PX);
		rootPanel.add(dockLayoutPanel);
		
		int northPanelHeight = 100;
		final AbsolutePanel panelNorth = new AbsolutePanel();
		panelNorth.setStyleName("northPanel");
		dockLayoutPanel.addNorth(panelNorth, northPanelHeight);

		final int distanceFromLeft = 20;
		final int distanceFromTop = 15;
		final int buttonSize = 20;
		final int switchButtonWidth = 25; // defined in stylesheet
		final int spacer1 = 5;
		final int genericHeight = 30; // defined in stylesheet
		final int textPanelWidth = 360; // defined in stylesheet + 10 due to border/padding
		
		Image imgFrom = new Image(resources.MarkerACircle());
		imgFrom.setAltText(CONSTANTS.from());
		panelNorth.add(imgFrom, distanceFromLeft, distanceFromTop + (genericHeight - buttonSize)/2);
		imgFrom.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if(fromMarker != null) {
					mapWidget.getMap().panTo(fromMarker.getPosition());
				}
			}
		});
		
		txtFrom = new TextBox();
		txtFrom.getElement().setId("textbox-from");
		txtFrom.setTitle(CONSTANTS.instr1());
		panelNorth.add(txtFrom, distanceFromLeft + buttonSize + spacer1, distanceFromTop);
		
		final Button btnLocate = new Button();
		panelNorth.add(btnLocate, distanceFromLeft + buttonSize + spacer1 + textPanelWidth - 50, distanceFromTop + genericHeight/2 - buttonSize/2);
		btnLocate.setStyleName("locateButton");
		btnLocate.setTitle(CONSTANTS.loc());
		
		Image imgShowFavsFrom = new Image(resources.arrowDown());
		imgShowFavsFrom.setAltText(CONSTANTS.showFavourites());
		imgShowFavsFrom.setTitle(CONSTANTS.showFavourites());
		imgShowFavsFrom.setStyleName("expandImage");
		panelNorth.add(imgShowFavsFrom, distanceFromLeft + buttonSize + spacer1 + textPanelWidth - 25, distanceFromTop + genericHeight/2 - buttonSize/2);
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
					DOM.setStyleAttribute(favouritesMenu.getElement(), "left", "45px");
					DOM.setStyleAttribute(favouritesMenu.getElement(), "height", favouritesMenu.getWidgetCount()*22 + 5 + "px");
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
		
		Image imgTo = new Image(resources.MarkerBCircle());
		imgTo.setAltText(CONSTANTS.to());
		panelNorth.add(imgTo, distanceFromLeft, distanceFromTop + genericHeight + spacer1 + (genericHeight - buttonSize)/2);
		imgTo.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if(toMarker != null) {
					mapWidget.getMap().panTo(toMarker.getPosition());
				}
			}
		});
		
		txtTo = new TextBox();
		txtTo.getElement().setId("textbox-to");
		txtTo.setTitle(CONSTANTS.instr1());
		panelNorth.add(txtTo, distanceFromLeft + buttonSize + spacer1, distanceFromTop + genericHeight + spacer1);
		
		Image imgShowFavsTo = new Image(resources.arrowDown());
		imgShowFavsTo.setAltText(CONSTANTS.showFavourites());
		imgShowFavsTo.setTitle(CONSTANTS.showFavourites());
		imgShowFavsTo.setStyleName("expandImage");
		panelNorth.add(imgShowFavsTo, distanceFromLeft + buttonSize + spacer1 + textPanelWidth - 25, distanceFromTop + genericHeight + spacer1 + genericHeight/2 - buttonSize/2);
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
					DOM.setStyleAttribute(favouritesMenu.getElement(), "left", "45px");
					DOM.setStyleAttribute(favouritesMenu.getElement(), "height", favouritesMenu.getWidgetCount()*22 + 5 + "px");
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
		
		Button btnSwitch = new Button();
		btnSwitch.setStyleName("switchButton");
		btnSwitch.setTitle(CONSTANTS.btnSwitch());
		panelNorth.add(btnSwitch, distanceFromLeft + buttonSize + spacer1 + textPanelWidth + spacer1, distanceFromTop + spacer1 + 10);
		
		btnSearch = new Button(CONSTANTS.search());
		btnSearch.setStyleName("searchButton");
		panelNorth.add(btnSearch, distanceFromLeft + buttonSize + spacer1 + textPanelWidth + spacer1 + switchButtonWidth + spacer1, distanceFromTop + spacer1 + 5);
		
		Grid gridSelectLines = new Grid(3, 3);
		gridSelectLines.addStyleName("selectLinesGrid");
		panelNorth.add(gridSelectLines, btnSearch.getAbsoluteLeft() + 80, 15);
		
		Label lblColectivos = new Label(CONSTANTS.colectivos());
		gridSelectLines.setWidget(0, 0, lblColectivos);
		
		SimpleCheckBox simpleCheckBoxColectivos = new SimpleCheckBox();
		simpleCheckBoxColectivos.setValue(true);
		simpleCheckBoxColectivos.setEnabled(false);
		gridSelectLines.setWidget(0, 1, simpleCheckBoxColectivos);
		
		Label lblSubte = new Label(CONSTANTS.lblSubte_text());
		gridSelectLines.setWidget(1, 0, lblSubte);
		
		simpleCheckBoxSubte = new SimpleCheckBox();
		simpleCheckBoxSubte.setValue(true);
		gridSelectLines.setWidget(1, 1, simpleCheckBoxSubte);
		
		Label lblTrains = new Label(CONSTANTS.lblTrains_text());
		gridSelectLines.setWidget(2, 0, lblTrains);
		
		simpleCheckBoxTrains = new SimpleCheckBox();
		simpleCheckBoxTrains.setValue(true);
		gridSelectLines.setWidget(2, 1, simpleCheckBoxTrains);
		
		Image logo = new Image("img/logo-300.png");
		imgTo.setAltText("Viaja Facil Logo");
		panelNorth.add(logo, 625, 20);
		
		final FlowPanel selectLanguagePanel = new FlowPanel();
		selectLanguagePanel.setStyleName("selectLanguagePanel");
		panelNorth.add(selectLanguagePanel);
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
		
		final FlowPanel flowPanelEast = new FlowPanel();
		dockLayoutPanel.addEast(flowPanelEast, 160);
		//dockLayoutPanel.addEast(flowPanelEast, 0);
		
		FlowPanel westPanel = new FlowPanel();
		dockLayoutPanel.addWest(westPanel, resultsWidth);
		
		int southPanelHeight = 50;
		flowPanelSouth = new FlowPanel();
		flowPanelSouth.setStyleName("southPanel");
		dockLayoutPanel.addSouth(flowPanelSouth, southPanelHeight);
		
		final Grid gridSouth = new Grid(1, 3);
		gridSouth.getCellFormatter().setWidth(0, 0, "33.33%");
		gridSouth.getCellFormatter().setWidth(0, 1, "33.33%");
		gridSouth.getCellFormatter().setWidth(0, 2, "33.33%");
		gridSouth.getCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_LEFT);
		gridSouth.getCellFormatter().setHorizontalAlignment(0, 1, HasHorizontalAlignment.ALIGN_CENTER);
		gridSouth.getCellFormatter().setHorizontalAlignment(0, 2, HasHorizontalAlignment.ALIGN_RIGHT);
		gridSouth.setBorderWidth(0);
		flowPanelSouth.add(gridSouth);
		gridSouth.setSize("100%", "100%");
		
		Anchor lnkImpressum = new Anchor(CONSTANTS.lnkImpressum());
		lnkImpressum.setHref("/impressum.html");
		lnkImpressum.setTarget("_blank");
		lnkImpressum.addStyleName("valignmiddle");
		lnkImpressum.addStyleName("bigmarginleft");
		gridSouth.setWidget(0, 0, lnkImpressum);

		Anchor lnkFAQ = new Anchor(CONSTANTS.lnkFAQ());
		lnkFAQ.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		lnkFAQ.setHref(CONSTANTS.hrefFAQ());
		lnkFAQ.setTarget("_blank");
		lnkFAQ.addStyleName("valignmiddle");
		gridSouth.setWidget(0, 1, lnkFAQ);
		
		scrollPanel = new ScrollPanel();
		scrollPanel.setSize(resultsWidth + "px", "100%");
		westPanel.add(scrollPanel);
		
		resultsPanel = new StackLayoutPanel(Unit.PX);
		resultsPanel.setSize(resultsWidth + "px", 300+"px");
		resultsPanel.setVisible(false);
//		resultsPanel.addStyleName("zindex10");
//		DOM.setStyleAttribute(resultsPanel.getElement(), "z-index", "10");
		
		scrollPanel.add(resultsPanel);

		final MapOptions options = new MapOptions();
		options.setZoom(11);
		options.setCenter(new LatLng(-34.604389,-58.410873));
		options.setMapTypeId(new MapTypeId().getRoadmap());
		options.setDraggable(true);
		options.setMapTypeControl(true);
		options.setNavigationControl(true);
		options.setScrollwheel(true);
		options.setScaleControl(true);
	    mapWidget = new MapWidget(options);
	    dockLayoutPanel.add(mapWidget);
	    
	    Event.addListener(mapWidget.getMap(), "rightclick", new MouseEventCallback() {
			@Override
			public void callback(HasMouseEvent event) {
//				GWT.log("rightclick: " + event.getLatLng().getLatitude());
				
				eventLatLng = event.getLatLng();
				
				double scale = Math.pow(2, mapWidget.getMap().getZoom());
				LatLng nw = new LatLng(mapWidget.getMap().getBounds().getNorthEast().getLatitude(), 
						mapWidget.getMap().getBounds().getSouthWest().getLongitude());
				HasPoint worldCoordinateNW = mapWidget.getMap().getProjection().fromLatLngToPoint(nw);
				HasPoint worldCoordinate = mapWidget.getMap().getProjection().fromLatLngToPoint(event.getLatLng());
				
				int mapWidth = mapWidget.getOffsetWidth();
				int mapHeight = mapWidget.getOffsetHeight();
				int menuWidth = contextMenu.getOffsetWidth();
				int menuHeight = contextMenu.getOffsetHeight();
				int x = (int) Math.floor((worldCoordinate.getX() - worldCoordinateNW.getX()) * scale); 
				int y = (int) Math.floor((worldCoordinate.getY() - worldCoordinateNW.getY()) * scale);
				
				if((mapWidth - x ) < menuWidth) {
			         x = x - menuWidth;
				}
			    if((mapHeight - y ) < menuHeight) {
			        y = y - menuHeight;
			    }
			    x += mapWidget.getAbsoluteLeft();
			    y += mapWidget.getAbsoluteTop();
//			    GWT.log("should display context menu at " + x + ", " + y);
			    DOM.setStyleAttribute(contextMenu.getElement(), "left", x+"px");
			    DOM.setStyleAttribute(contextMenu.getElement(), "top", y+"px");
			    contextMenu.setVisible(true);
			}
	    });
	    
	    Event.addListener(mapWidget.getMap(), "click", new MouseEventCallback() {   	
	    	@Override
			public void callback(HasMouseEvent event) {
	    		contextMenu.setVisible(false);
	    	}
	    });
	    /*
	    if (Geolocation.isSupported()) {
	    	Geolocation geo = Geolocation.getGeolocation();
	    	geo.getCurrentPosition(new PositionCallback() {
	    		public void onFailure(PositionError error) {
	    			GWT.log("locate failed");
	    		}
	    		public void onSuccess(Position position) {
	    			Coordinates coords = position.getCoords();
	    			double lat = coords.getLatitude();
	    			double lon = coords.getLongitude();
	    			if(lat < latNE && lat > latSW && lon < lonNE && lon > lonSW) {
	    				mapWidget.getMap().setCenter(new LatLng(lat, lon));
	    			}
	    		}
	    	});
	    }*/

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
						if(fromMarker == null) {
							createMarker(resp.getGeometry().getLocation(), "from");
		    			} else {
		    				fromMarker.setPosition(resp.getGeometry().getLocation());
		    			}
						currentStreetLabel.setText(txtFrom.getText());
					} else if(fromOrTo.equalsIgnoreCase("to")) {
						toCoordDirty = false;
						if(toMarker == null) {
							createMarker(resp.getGeometry().getLocation(), "to");
		    			} else {
		    				toMarker.setPosition(resp.getGeometry().getLocation());
		    			}
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
	    
	    class MyPositionCallback implements PositionCallback {
    		public void onFailure(PositionError error) {
    			GWT.log("locate failed");
    		}
    		public void onSuccess(Position position) {
//    			GWT.log("Got location");
    			Coordinates coords = position.getCoords();
    			double lat = coords.getLatitude();
    			double lon = coords.getLongitude();
    			/*lat = Double.parseDouble(txtFrom.getText());
    			lon = Double.parseDouble(txtTo.getText()); */
    			if(lat > 0) { // TODO: remove this block, it is only for debugging
    				lat = -34.679523; 
    				lon = -58.370533;
    			}
    			mapWidget.getMap().setCenter(new LatLng(lat, lon));
    			fromCoordDirty = false;
    			if(fromMarker == null) {
    				createMarker(new LatLng(lat, lon), "from");
    			} else {
    				fromMarker.setPosition(new LatLng(lat, lon));
    			}
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
	    
	    /*
	     * also handles the search button
	     */
		class ToHandler implements ChangeHandler, ClickHandler, KeyPressHandler {
			
			public void onClick(ClickEvent event) {
				search();
			}
			
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
		
		class ContextMenuHandler implements ClickHandler {

			@Override
			public void onClick(ClickEvent event) {
				contextMenu.setVisible(false);
				if(event.getSource() == lblSetFrom) {
					if(fromMarker == null) {
	    				createMarker(eventLatLng, "from");
	    			} else {
	    				fromMarker.setPosition(eventLatLng);
	    			}
					fromCoordDirty = false;
					GeocoderRequest gcReq = new GeocoderRequest();
	    			if (!GWT.isScript()) {
	    				   removeGwtObjectId(gcReq.getJso());
	    			}
	    			gcReq.setLatLng(fromMarker.getPosition());
	    			gcReq.setRegion("ar");
	    			Geocoder gc = new Geocoder();
	    			gc.geocode(gcReq, new MyInverseGeocoderCallback("from"));
				} else if(event.getSource() == lblSetTo) {
					if(toMarker == null) {
	    				createMarker(eventLatLng, "to");
	    			} else {
	    				toMarker.setPosition(eventLatLng);
	    			}
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
					GWT.log("Error: Got event for contextMenu but it did not match any of its labels.");
				}
				resetSearch();
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
		
		class SwitchHandler implements ClickHandler {
			public void onClick(ClickEvent event) {
				if(toMarker != null && fromMarker != null) {
					String tempStr = txtTo.getText();
					txtTo.setText(txtFrom.getText());
					txtFrom.setText(tempStr);
	
					boolean tempDirty = toCoordDirty;
					toCoordDirty = fromCoordDirty;
					fromCoordDirty = tempDirty;
					
					HasLatLng tempLatLng = toMarker.getPosition();
					toMarker.setPosition(fromMarker.getPosition());
					fromMarker.setPosition(tempLatLng);
				}
				
				resetSearch();
			}
		}
		
		class LocHandler implements ClickHandler {
			public void onClick(ClickEvent event) {
				if(event.getSource() == btnLocate) {
					if (Geolocation.isSupported()) {
				    	Geolocation geo = Geolocation.getGeolocation();
				    	PositionOptions pOpts = PositionOptions.create();
						pOpts.setEnableHighAccuracy(true);
				    	geo.getCurrentPosition(new MyPositionCallback(), pOpts);
				    }
				}
			}
		}
		
		ToHandler toHandler = new ToHandler();
		FromHandler fromHandler = new FromHandler();
		LocHandler locHandler = new LocHandler();
		SwitchHandler switchHandler = new SwitchHandler();
		ContextMenuHandler contextMenuHandler = new ContextMenuHandler();
//		locHandler.locate();
		txtFrom.addChangeHandler(fromHandler);
		txtFrom.addKeyPressHandler(fromHandler);
		btnSearch.addClickHandler(toHandler);
		txtTo.addChangeHandler(toHandler);
		txtTo.addKeyPressHandler(toHandler);
		btnLocate.addClickHandler(locHandler);
		btnSwitch.addClickHandler(switchHandler);
		lblSetFrom.addClickHandler(contextMenuHandler);
		lblSetTo.addClickHandler(contextMenuHandler);
		
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
		
		colors.add("#ff0000");
		colors.add("#0000ff");
		colors.add("#00ff00");
//		colors.add("#ffff00");
//		colors.add("#ff9900");
		colors.add("#ff00ff");
		colors.add("#00ffff");
		
		if(!Window.Navigator.isCookieEnabled()) {
			showMessageBox(CONSTANTS.needsCoockiesEnabled());
		}
		
//		if(!(Window.Navigator.getUserAgent().contains("msie") || Window.Navigator.getUserAgent().contains("MSIE"))) {
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
//		}
		/*
		String jsessionid = Cookies.getCookie("JSESSIONID");
//		GWT.log("Session id: " + jsessionid);
		if(jsessionid == null) {
			getPointsService.sayHello(new AsyncCallback<String>() {

				@Override
				public void onFailure(Throwable caught) {
					GWT.log("Error in sayHello: " + caught);
				}

				@Override
				public void onSuccess(String result) {
					GWT.log("said hello: " + result);
				}
				
			});
		}*/
			
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
//		showSpinningCircle();
		
		getPointsService.login(GWT.getHostPageBaseURL(), new AsyncCallback<LoginInfo>() {
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
				loginOrOutLink.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
				loginOrOutLink.addStyleName("valignmiddle");
				loginOrOutLink.addStyleName("bigmarginright");
				gridSouth.setWidget(0, 2, loginOrOutLink);
			}		
		});
		
		// new MapsAdvertising(mapWidget.getMap().getJso());
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
//			Window.alert("Alert: " + instance.getLatLng());
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
	
	private native void removeGwtObjectId(JavaScriptObject jso) /*-{
	   delete jso['__gwt_ObjectId'];
	}-*/;

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
								// showMessageBox(CONSTANTS.noResults());
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
	
	private void resetSearch() {
		searchmode = 0;
		if(!searchRunning) {
			hideSpinningCircle();
			btnSearch.setEnabled(true);
			btnSearch.setText(CONSTANTS.search());
		}
	}
	
	private void showResults() {
		// evtl. relevant falls es nötig ist das stackPanel zu resizen: http://stackoverflow.com/questions/4334216/stacklayoutpanel-has-no-more-place-to-show-the-children-when-there-are-too-many-h
		resultsPanel.setVisible(false);
		resultsPanel.clear();
		DOM.getElementById("leftcontainer").getStyle().setProperty("display", "none");
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
						FlowPanel connectionDetailsPanelnew = new FlowPanel();;
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
						lConnTemp.setWidth(resultsWidth + "px");
						dummyPanel.add(lConnTemp);
						int heightHeader = lConnTemp.getElement().getOffsetHeight();
						dummyPanel.remove(lConnTemp);
						
						connectionDetailsPanelnew.setStyleName("gwt-StackLayoutPanelContent");
						connectionDetailsPanelnew.setWidth(resultsWidth + "px");
						dummyPanel.add(connectionDetailsPanelnew);
						int heightContent = connectionDetailsPanelnew.getElement().getOffsetHeight() + 5;
		//				GWT.log("eleD: " +lConnDetailsTemp.getElement().getOffsetHeight() + ", widgetD: " + lConnDetailsTemp.getOffsetHeight());
						dummyPanel.remove(connectionDetailsPanelnew);
						resultsPanel.add(connectionDetailsPanelnew, lConn, heightHeader);
						stackPanelTotalHeight += heightHeader;
						if(heightContent > biggestContentHeight) {
							biggestContentHeight = heightContent;
						}
						/*GWT.log(connection);
						GWT.log(connectionDetails);*/
					}
				}
			}
		}
		stackPanelTotalHeight += biggestContentHeight + 5;
//		GWT.log("Total height: " + stackPanelTotalHeight);
		resultsPanel.setHeight(stackPanelTotalHeight + "px");
		/*int spacerheight = 120 + stackPanelTotalHeight;
		if(spacerheight > 120 + scrollPanel.getOffsetHeight()) {
			spacerheight = 120 + scrollPanel.getOffsetHeight();
		}
		DOM.getElementById("topspacer").getStyle().setProperty("height", spacerheight + "px");*/
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
//		polyLines = new LinkedList<Polyline>();
		polyLines.clear();
//		stations = new LinkedList<OverlayView>();
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
	
	private void searchError(Throwable e) {
		searchRunning = false;
		resetSearch();
	    showMessageBox(CONSTANTS.errorTryAgainLater());
	}
	
	private void showMessageBox(String message) {
		hideSpinningCircle();
		final DecoratedPopupPanel simplePopup = new DecoratedPopupPanel(true);
	    simplePopup.setWidth("150px");
	    simplePopup.setWidget(new HTML(message));
	    simplePopup.center();
	}
	
	private void showSpinningCircle() {
	    spinningCircle.center();
	}
	
	private void hideSpinningCircle() {
		spinningCircle.hide();
	}
	
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
	
	private MarkerOptions getDefaultMarkerOptions(HasLatLng coord) {
		MarkerOptions markerOpts = new MarkerOptions();
		markerOpts.setPosition(coord);
		markerOpts.setVisible(true);
		markerOpts.setMap(mapWidget.getMap());
		markerOpts.setDraggable(true);
		return markerOpts;
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
				/*for (HasAddressComponent a : responses.get(0).getAddressComponents()) {
					GWT.log(a.getLongName());
				}*/
			} else {
				GWT.log("Inverse Geocoder failed with status: " + status);
			}
		}
	}
	
	private void panToAndMark(double x, double y) {
		mapWidget.getMap().panTo(new LatLng(x, y));
		if(markerCircle == null) {
			createMarkerCircle(x, y);
		} else {
			markerCircle.setCenter(new LatLng(x, y));
		}
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
					Label favourite = new Label(fav.getName());
					favourite.addStyleName("favouritesMenuLabel");
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
					// imgEsp.addStyleName(imgEsp.getStylePrimaryName()+"-selectLanguage");
					pan.add(starRem);
					favouritesMenu.add(pan);
				}
			}
		} else {
			Anchor hinweis = new Anchor(CONSTANTS.logInToStoreFavs());
			hinweis.setStyleName("favouritesMenuItem");
			hinweis.setHref(result.getLoginUrl());
//			hinweis.addStyleName("favouritesMenuLabel");
			favouritesMenu.add(hinweis);
		}
	}
}
