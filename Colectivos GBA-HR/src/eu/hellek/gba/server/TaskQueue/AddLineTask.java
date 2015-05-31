package eu.hellek.gba.server.TaskQueue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.taskqueue.DeferredTask;
import com.google.appengine.api.taskqueue.DeferredTaskContext;
import com.googlecode.objectify.Objectify;

import eu.hellek.gba.model.Line;
import eu.hellek.gba.model.Point;
import eu.hellek.gba.server.dao.Dao;
import eu.hellek.gba.server.dao.ManagementDao;
import eu.hellek.gba.server.utils.Utils;

public class AddLineTask implements DeferredTask {

	private static final long serialVersionUID = 1L;

	private String payload;

	public AddLineTask(String payload) {
		this.payload = payload;
	}

	/**
	 * Format von String points: lat,lon,Strassenname,ignore als float,float,string ohne beistrich,boolean(0/1)
	 * Die ersten 3 Felder: Nummer, Ramal und ob zug/ubahn oder bus (nur bei letzteren werden zwischenpunkte erstellt)
	 */ 
	@Override
	public void run() {
		DeferredTaskContext.setDoNotRetry(true);
		String functionName = "AddLineTask";
		try {
/*			String fail = " "; // These two lines make the task fail. They are for testing
			System.out.println(fail.charAt(5));*/
			Objectify ofy = Dao.getInstance().getObjectify();
			String points = payload;
			List<Point> pointsList = new ArrayList<Point>();
			String[] parts = points.split(Pattern.quote(","));
			Line l = null;
			boolean isMainLine = true;
			boolean twowayPoint = false;
			if(parts[1].substring(0, 8).equals("XshortX ")) {
				isMainLine = false;
				parts[1] = parts[1].substring(8);
			}
			l = new Line(parts[0], parts[1], 1);
			if(ManagementDao.getInstance().checkIfLineExists(l, ofy)) {
				throw new Exception("Line already exists: "  + l);
			}
			HashSet<String> geoCells = new HashSet<String>();
			HashMap<String,Integer> directGeoCells = new HashMap<String,Integer>();
			HashMap<String,Boolean> ignoreCells = new HashMap<String,Boolean>();
			HashMap<String,Boolean> twowayCells = new HashMap<String,Boolean>();
			int start = 3; // felder 0,1,2 sind diese extradaten, ab feld 3 kommen die punkte
			for(int i = start; i<parts.length; i=i+3) {
				float a = Float.valueOf(parts[i]);
				float b = Float.valueOf(parts[i+1]);
				Point p = new Point(parts[i+2], a, b, null);
//				com.beoui.geocell.model.Point _p1 = new com.beoui.geocell.model.Point(a,b);
				twowayPoint = false;
				if(p.getStreet().length() > 6 &&  p.getStreet().substring(0, 6).equals(":2way:")) {
					p.setStreet(p.getStreet().substring(6));
					twowayPoint = true;
				}
				if(p.getStreet().length() > 4 && (p.getStreet().substring(0, 4).equals("Au. ") || p.getStreet().substring(0, 5).equals(":ign:"))) {
					p.setIgnore(true);
					if(p.getStreet().substring(0, 5).equals(":ign:")) {
						p.setStreet(p.getStreet().substring(5));
					}
				}
				String cell_p = Utils.computeGeoCell(p);
				geoCells.add(cell_p);
				/* i=2 and higher means that it is treating the second+ point
				   start creating interpolating points from here on
				   which means filling up the distance from the last point to the current (new) point
				   with many new points i.e. every 100m, unless type is not "bus" */
				if(i>=start+2) {
					Point lastPoint = pointsList.get(pointsList.size()-1);
					GeoPt vector = Utils.vectorBetween(p, lastPoint);
					double distance = Utils.distanceApprox(vector.getLatitude(), vector.getLongitude());
					double distanceM = distance*Utils.distanceMultiplikator;
					long times = Math.round(distanceM/100.0);
//					Logger.getLogger(functionName).log(Level.INFO, functionName + ": Times: " + times);
//					System.err.println("lastPoint: " + lastPoint.getLatlon().getLatitude() + ", " + lastPoint.getLatlon().getLongitude());
					for(int j = 1; j<times; j++) {
						Point intermediate = new Point(lastPoint.getStreet(), (vector.getLatitude()/times)*j+lastPoint.getLatlon().getLatitude(),(vector.getLongitude()/times)*j+lastPoint.getLatlon().getLongitude(), false, true, null);
//						com.beoui.geocell.model.Point _p2 = new com.beoui.geocell.model.Point(intermediate.getLatlon().getLatitude(),intermediate.getLatlon().getLongitude());
						String currentCell = Utils.computeGeoCell(intermediate);
						intermediate.setIndex(pointsList.size());
						geoCells.add(currentCell);
						if(!lastPoint.isIgnore()) {
							pointsList.add(intermediate);
							ignoreCells.put(currentCell, new Boolean(false));
						} else {
							if(!ignoreCells.containsKey(currentCell)) {
								ignoreCells.put(currentCell, new Boolean(true));
							}
						}
						if(!directGeoCells.containsKey(currentCell)) {
							directGeoCells.put(currentCell, intermediate.getIndex());
						}
						if(twowayPoint) {
							twowayCells.put(currentCell, true);
						}
					}
					lastPoint.setNextMainPointIndex(pointsList.size());
				}
				p.setIndex(pointsList.size());
				if(!directGeoCells.containsKey(cell_p)) {
					directGeoCells.put(cell_p, p.getIndex());
				}
				if(twowayPoint) {
					twowayCells.put(cell_p, true);
				}
				if(!p.isIgnore()) {
					ignoreCells.put(cell_p, new Boolean(false));
				} else {
					if(!ignoreCells.containsKey(cell_p)) {
						ignoreCells.put(cell_p, new Boolean(true));
					}
				}
				pointsList.add(p);
			}
			ManagementDao.getInstance().addLine(l, ofy);
			if(isMainLine) {
				ManagementDao.getInstance().setPointsForLine(l, pointsList, ofy);
			}
			try {
				ManagementDao.getInstance().addOrUpdatePlanQuadrats(geoCells, directGeoCells, l, ignoreCells, twowayCells, isMainLine);
			} catch (Exception e) {
				ManagementDao.getInstance().removeLine(l, ofy);
				e.printStackTrace();
				throw new Exception("Error occured when creating/updating planquadrats for new line " + l + ". Line was deleted again since it is useless without PQs." + e);
			}
			Logger.getLogger(functionName).log(Level.INFO, functionName + ": added Line " + l);
		} catch (Exception e) {
			Logger.getLogger(functionName).log(Level.SEVERE, functionName + ": "  + e);
			Logger.getLogger(functionName).log(Level.SEVERE, functionName + ": Payload was: "  + payload);
			e.printStackTrace();
			Utils.eMailError(e, functionName);
		}
	}

}
