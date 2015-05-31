package eu.hellek.gba.client.admin;

import java.util.Iterator;
import java.util.List;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArrayNumber;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;

import eu.hellek.gba.client.GetPointsService;
import eu.hellek.gba.client.GetPointsServiceAsync;
import eu.hellek.gba.shared.ConnectionProxy;
import eu.hellek.gba.shared.LineProxy;
import eu.hellek.gba.shared.SearchResultProxy;

public class AdminInterface implements EntryPoint {
	
	private final LineListServiceAsync lineListService = GWT.create(LineListService.class);
	private final GetPointsServiceAsync getPointsService = GWT.create(GetPointsService.class);
	
	private ListBox lb;
	private final Label userNameLabel = new Label();
	private List<String> dlkSet = null;

	// Gerli to Plaza de Mayo (default)
	/*float lat1 = -34.6788F;
    float lon1 = -58.372829F;
    float lat2 = -34.608416F;
    float lon2 = -58.372078F;*/
    
 // Gerli to Retiro
	/*float lat1 = -34.6788F;
    float lon1 = -58.372829F;
    float lat2 = -34.589693F;
    float lon2 = -58.372979F;*/
    
//  Retiro to Av. Rivadavia/Congreso
    /*float lat1 = -34.589693F;
    float lon1 = -58.372979F;
    float lat2 = -34.609193F;
    float lon2 = -58.394308F;*/
	
//  Retiro to Once
    float lat1 = -34.589693F;
    float lon1 = -58.372979F;
    float lat2 = -34.608628F;
    float lon2 = -58.406862F;
	
	@Override
	public void onModuleLoad() {
	
		lb = new ListBox();
		lb.setVisibleItemCount(1);
		lb.addItem("Please select a line", "-1");
		
		RootPanel.get("maindiv").add(lb);
		
		// Stuff to add a line: Name and list of points
//		final TextBox tb_num = new TextBox();
//		final TextBox tb_name = new TextBox();
		final TextArea ta_points = new TextArea();
		VerticalPanel panel = new VerticalPanel();
		ta_points.setCharacterWidth(120);
	    ta_points.setVisibleLines(10);
//	    panel.add(tb_num);
//		panel.add(tb_name);
		panel.add(ta_points);
		final Button sendButton = new Button("Send");
		panel.add(sendButton);
		panel.add(userNameLabel);
		final Button getAllConnectionsButton = new Button("Find all connections");
		panel.add(getAllConnectionsButton);
		final Button getTrainConnectionsButton = new Button("Find all connections with trains");
		panel.add(getTrainConnectionsButton);
		final Button updateSrcDestButton = new Button("Set from/to");
		panel.add(updateSrcDestButton);
		final Button resetTNButton = new Button("Reset Trainnodes");
		panel.add(resetTNButton);
		final Button checkLinesButton = new Button("Check Buses");
		panel.add(checkLinesButton);
		final Button deletePointsButton = new Button("Delete Line");
		panel.add(deletePointsButton);
		final Button deleteAllTrainsButton = new Button("Delete all Trains/Subtes, etc.");
		panel.add(deleteAllTrainsButton);
		RootPanel.get("maindiv").add(panel);
		getLines();
		getUserMail();
		
		class MyHandler implements ClickHandler, ChangeHandler {

			public void onClick(ClickEvent event) {
				if(event.getSource() == sendButton) {
					addLine();
				} else if(event.getSource() == resetTNButton) {
					resetTrainNodes();
				} else if(event.getSource() == deletePointsButton) {
					int index = lb.getSelectedIndex();
					String value = lb.getValue(index);
					if(value.equals("-1")) {
						GWT.log("no line selected");
					} else {
						deleteLine(value);
					}	
				} else if(event.getSource() == getAllConnectionsButton) {
					getAllConnections();
				} else if(event.getSource() == getTrainConnectionsButton) {
					getTrainConnections();
				} else if(event.getSource() == updateSrcDestButton) {
					updateSrcDest();
				} else if(event.getSource() == deleteAllTrainsButton) {
					deleteAllTrains();
				} else if(event.getSource() == checkLinesButton) {
					checkLines();
				}
			}
			
			public void onChange(ChangeEvent event) {
//				GWT.log("onChange fired");
				if(event.getSource() == lb) {
					ListBox source = (ListBox) event.getSource();
					int index = source.getSelectedIndex();
					String value = source.getValue(index);
					if(value.equals("-1")) {
//						GWT.log("no line selected");
						try {
							JsArrayNumber jsnums = getNativeArray();
							ta_points.setText("");
							drawPoly(jsnums, true, "#000000");
						} catch (Exception e) {
							GWT.log("error", e);
						}
					} else {
//						GWT.log(value);
						getPoints(value);
					}
				}
				/*try {
					testtest();
				} catch (Exception e) {
					GWT.log("error", e);
				}*/
			}
			
			private void addLine() {
				GWT.log("addLine called");
				sendButton.setText("working ...");
				lineListService.addLine(ta_points.getText(),
					new AsyncCallback<String>() {
						public void onFailure(Throwable caught) {
							GWT.log("Error in addLine(): ", caught);
							sendButton.setText("Error");
						}
		
						public void onSuccess(String result) {
							sendButton.setText("Success");
						}
					});
			}
			
			private void updateSrcDest() {
				String[] parts = ta_points.getText().split(",");	
				lat1 = Float.parseFloat(parts[0]);
				lon1 = Float.parseFloat(parts[1]);
				lat2 = Float.parseFloat(parts[2]);
				lon2 = Float.parseFloat(parts[3]);
			}
			
			// get the Points of a certain line
			private void getPoints(String line) {
				lineListService.getPoints(line,
					new AsyncCallback<List<Float>>() {
						public void onFailure(Throwable caught) {
							GWT.log("Error in getPoints", caught);
						}

						public void onSuccess(List<Float> result) {
							Iterator<Float> i = result.iterator();
							String s = "";
							try {
								JsArrayNumber jsnums = getNativeArray();
								while(i.hasNext()) {
									Float f = i.next();
									s += f.toString() + ", ";
									jsnums.push(f);
								}
	//							GWT.log("Text: " + s);
								ta_points.setText(s);
								drawPoly(jsnums, true, "#000000");
							} catch (Exception e) {
								GWT.log("error", e);
							}
						}
					});
			}
			
			private void resetTrainNodes() {
				lineListService.resetTrainNodes(
					new AsyncCallback<String>() {
						public void onFailure(Throwable caught) {
							GWT.log("Error in resetTrainNodes", caught);
						}

						public void onSuccess(String result) {
							GWT.log("resetTrainNodes: " + result);
						}
					});
			}
			
			private void checkLines() {
				lineListService.checkLines(
					new AsyncCallback<String>() {
						public void onFailure(Throwable caught) {
							GWT.log("Error in checkLines", caught);
						}

						public void onSuccess(String result) {
							GWT.log("checkLines: " + result);
						}
					});
			}
			
			private void deleteLine(String line) {
				lineListService.deleteLine(line,
					new AsyncCallback<String>() {
						public void onFailure(Throwable caught) {
							GWT.log("Error in deletePoints", caught);
						}

						public void onSuccess(String result) {
							
						}
					});
			}
			
			private void deleteAllTrains() {
				lineListService.deleteAllTrains(
					new AsyncCallback<String>() {
						public void onFailure(Throwable caught) {
							GWT.log("Error in deleteAllLines", caught);
						}

						public void onSuccess(String result) {
							
						}
					});
			}
			
			private void getAllConnections() {
				lineListService.getAllConnections(lat1, lon1, lat2, lon2, false, false, 
					new AsyncCallback<SearchResultProxy>() {
						public void onFailure(Throwable caught) {
							GWT.log("Error in getAllConnections", caught);
						}

						public void onSuccess(SearchResultProxy result) {
							String total = iterateConnectionProxies(result);
							ta_points.setText(total);
						}
					});
			}
			
			private void getTrainConnections() {
				lineListService.getTrainConnections(lat1, lon1, lat2, lon2, false, false, 
					new AsyncCallback<SearchResultProxy>() {
						public void onFailure(Throwable caught) {
							GWT.log("Error in getTrainConnections", caught);
						}

						public void onSuccess(SearchResultProxy result) {
							String total = iterateConnectionProxies(result);
							ta_points.setText(total);
						}
					});
			}
			
		}
		
		MyHandler handler = new MyHandler();
		sendButton.addClickHandler(handler);
		resetTNButton.addClickHandler(handler);
		deletePointsButton.addClickHandler(handler);
		getAllConnectionsButton.addClickHandler(handler);
		getTrainConnectionsButton.addClickHandler(handler);
		updateSrcDestButton.addClickHandler(handler);
		deleteAllTrainsButton.addClickHandler(handler);
		checkLinesButton.addClickHandler(handler);
		lb.addChangeHandler(handler);
	}
	
//	poly.getPath().push(new google.maps.LatLng(47.781789,13.039398));
//	poly.getPath().push(new google.maps.LatLng(47.781789,13.0));
	
	public static native void drawPoly(JsArrayNumber pts, boolean clear, String color) /*-{
//		var pts = [47.781789,13.039398,47.781789,13.0];
		$wnd.drawPolyLine(pts, clear, color);
	}-*/;
	
	native JsArrayNumber getNativeArray() /*-{
		var arr = [1.1, 2.2];
		arr.pop();
		arr.pop();
		return arr;
	}-*/;
	
	private void getLines() {
		lineListService.getLines(
			new AsyncCallback<List<String>>() {
				public void onFailure(Throwable caught) {
				}

				public void onSuccess(List<String> result) {
					Iterator<String> iterator = result.iterator();
					while(iterator.hasNext()) {
						String a = iterator.next();
						String b = iterator.next();
						lb.addItem(a, b);
					}
				}
			});
	}
	
	private void getUserMail() {
		getPointsService.getUserMail(
			new AsyncCallback<String>() {
				public void onFailure(Throwable caught) {
				}

				public void onSuccess(String result) {
					userNameLabel.setText(result);
				}
			});
	}
	
	private String iterateConnectionProxies(SearchResultProxy res) {
		List<ConnectionProxy> c = res.getConnections();
		Iterator<ConnectionProxy> cp_it = c.iterator();
		String total = "";
		while(cp_it.hasNext()) {
			ConnectionProxy cp = cp_it.next();
			String s = "Total distance: " + cp.getDistance() + "m: " + cp.getTime() + "min\n";
			Iterator<LineProxy> i = cp.getLines().iterator();
			while(i.hasNext()) {
				LineProxy lp = i.next();
				s += lp.toString();
				if(lp.getStartStreet() != null && lp.getDestStreet() != null) {
					s += " desde " + lp.getStartStreet() + " hasta " + lp.getDestStreet();
				}
				s += " /distance: " + lp.getDistance() + "m: " + lp.getTime() + "min\n";
				if(lp.getAlternativeLines().size() > 0) {
					s += "Alternatives: ";
					for(String ss : lp.getAlternativeLines()) {
						s += ss + ", ";
					}
					s = s.substring(0, s.length() - 2);
					s += "\n";
				}
				JsArrayNumber jsnums = getNativeArray();
				for(int j = 0; j < lp.getAllPoints().size(); j++) {
					jsnums.push(lp.getAllPoints().get(j));
				}
				drawPoly(jsnums, false, "#000000");
				jsnums = getNativeArray();
				for(int j = 0; j < lp.getRelevantPoints().size(); j++) {
					jsnums.push(lp.getRelevantPoints().get(j));
				}
				drawPoly(jsnums, false, "#ff0000");
			}
			s += "\n";
			total += s;
		}
		return total;
	}
	
}

