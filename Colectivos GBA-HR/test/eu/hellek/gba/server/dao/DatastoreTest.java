package eu.hellek.gba.server.dao;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.Query;

import eu.hellek.gba.model.Line;
import eu.hellek.gba.model.PlanQuadrat;
import eu.hellek.gba.model.Point;
import eu.hellek.gba.model.TrainNode;
import eu.hellek.gba.server.holders.WayHolder;
import eu.hellek.gba.server.utils.AStarImpl;
import eu.hellek.gba.server.utils.AStarNode;
import eu.hellek.gba.server.utils.AStarNodeImpl;
import eu.hellek.gba.server.utils.Utils;

public class DatastoreTest {

	private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
	
	@Before
    public void setUp() {
        helper.setUp();
    }

    @After
    public void tearDown() {
        helper.tearDown();
    }
    
    @Test
    public void addLineTest() {
    	Line l = new Line("100", "Ramal A", 11);
    	Dao.getInstance();
    	Objectify ofy = ObjectifyService.begin();
		ofy.put(l);
		ofy = ObjectifyService.begin();
		Query<Line> q = ofy.query(Line.class);
		List<Line> lines = q.list();
		assertEquals(1, lines.size());
    }
    
    @Test
    public void AStarTestBusesOnly() {
    	String lineA1 = "1,Av. 9 de Julio von Süd nach Nord,bus,-34.622331,-58.380661,Av. 9 de Julio,-34.591672,-58.382378,Endstation Nord";
    	String lineA2 = "1,Av. 9 de Julio von rev Nord nach Süd,bus,-34.591672,-58.382378,Av. 9 de Julio,-34.622331,-58.380661,Endstation Nord";
    	String lineB1 = "2,Av. Rivadavia von Casa Rosada nach Westen,bus,-34.608063,-58.368044,Av. Rivadavia,-34.609899,-58.406839,Endstation West";
    	String lineB2 = "2,Av. Rivadavia rev von Westen nach Casa Rosada,bus,-34.609899,-58.406839,Av. Rivadavia,-34.608063,-58.368044,Endstation";
    	addLine(lineA1);
    	addLine(lineA2);
    	addLine(lineB1);
    	addLine(lineB2);
    	Objectify ofy = ObjectifyService.begin();
    	Query<Line> q = ofy.query(Line.class);
		List<Line> lines = q.list();
		assertEquals(4, lines.size());
		HashSet<Key<Line>> dlkSet = new HashSet<Key<Line>>();
		HashSet<Key<Line>> tabuTrainSet = new HashSet<Key<Line>>(); // leer
		for(Line l : lines) {
			System.err.println(l.getId() + "\t" + l.getLinenum() + "\t" + l.getRamal());
			dlkSet.add(new Key<Line>(Line.class, l.getId()));
		}
		ofy = ObjectifyService.begin();
		Query<PlanQuadrat> q2 = ofy.query(PlanQuadrat.class);
		List<PlanQuadrat> pqs = q2.list();
		assertEquals(27, pqs.size());
		List<Key<PlanQuadrat>> pqs_with_dlk = new LinkedList<Key<PlanQuadrat>>();
		for(PlanQuadrat pq : pqs) {
			if(pq.getDirectLineKeys().size() > 0) {
				pqs_with_dlk.add(new Key<PlanQuadrat>(PlanQuadrat.class, pq.getId()));
			}
		}
		assertEquals(27, pqs_with_dlk.size());
		HashSet<String> mapBusKey = new HashSet<String>();
		for(Key<PlanQuadrat> pq : pqs_with_dlk) {
			mapBusKey.add(pq.getName());
		}
		Dao.resetTrainNodes();
		WayHolder wh = new AStarImpl().aStarSearch(Utils.computeGeoCell(new com.beoui.geocell.model.Point(-34.622331,-58.380661)), Utils.computeGeoCell(new com.beoui.geocell.model.Point(-34.609899,-58.406839)), mapBusKey, dlkSet, tabuTrainSet, Dao.getInstance().getObjectify());
		org.junit.Assert.assertNotNull(wh);
		org.junit.Assert.assertEquals(16, wh.getWay().size());
		for(AStarNode asn : wh.getWay()) {
			System.err.print("\"" + asn.getGeoCell() + "\", ");
		}
		System.err.println();
		for(AStarNode asn : wh.getWay()) {
			System.err.print("\"" + asn.getOwningLine() + "\", ");
		}
		System.err.println();
		for(AStarNode asn : wh.getWay()) {
			AStarNodeImpl bus = (AStarNodeImpl)asn;
			System.err.print("\"" + bus.getIndex() + "\", ");
		}
		System.err.println();
		assertEquals(12, tns.size());
		HashSet<Key<Line>> dlkSet = new HashSet<Key<Line>>(); // 3 leere Sets übergeben
		HashSet<Key<Line>> tabuTrainSet = new HashSet<Key<Line>>();
		HashSet<String> mapBusKey = new HashSet<String>();
		Dao.resetTrainNodes();
		WayHolder wh = new AStarImpl().aStarSearch(Utils.computeGeoCell(new com.beoui.geocell.model.Point(-34.622331,-58.380661)), Utils.computeGeoCell(new com.beoui.geocell.model.Point(-34.609899,-58.406839)), mapBusKey, dlkSet, tabuTrainSet, Dao.getInstance().getObjectify());
		org.junit.Assert.assertNotNull(wh);
		org.junit.Assert.assertEquals(5, wh.getWay().size());
		for(AStarNode asn : wh.getWay()) {
			System.err.print("\"" + asn.getGeoCell() + "\", ");
		}
		System.err.println();
		for(AStarNode asn : wh.getWay()) {
			System.err.print("\"" + asn.getOwningLine() + "\", ");
		}
    }
    
    
    @Test
    public void AStarTestTrainThenBus() {
    	String lineA1 = "A,Av. 9 de Julio von Süd nach Nord,subte,-34.622331,-58.380661,Av. 9 de Julio,Av. 9 de Julio,-34.608946,-58.381391,cruce,cruce,-34.591672,-58.382378,Endstation Nord,Endstation Nord";
    	String lineA2 = "A,Av. 9 de Julio von rev Nord nach Süd,subte,-34.591672,-58.382378,Endstation Nord,Endstation Nord,-34.608946,-58.381391,cruce,cruce,-34.622331,-58.380661,Av. 9 de Julio,Av. 9 de Julio";
    	String lineB1 = "2,Av. Rivadavia von Casa Rosada nach Westen,bus,-34.608063,-58.368044,Av. Rivadavia,-34.609899,-58.406839,Endstation West";
    	String lineB2 = "2,Av. Rivadavia rev von Westen nach Casa Rosada,bus,-34.609899,-58.406839,Av. Rivadavia,-34.608063,-58.368044,Endstation";
    	addTrain(lineA1);
    	addTrain(lineA2);
    	addLine(lineB1);
    	addLine(lineB2);
    	Objectify ofy = ObjectifyService.begin();
    	Query<Line> q = ofy.query(Line.class);
		List<Line> lines = q.list();
		assertEquals(4, lines.size());
		HashSet<Key<Line>> dlkSet = new HashSet<Key<Line>>();
		HashSet<Key<Line>> tabuTrainSet = new HashSet<Key<Line>>();
		for(Line l : lines) {
			System.err.println(l.getId() + "\t" + l.getLinenum() + "\t" + l.getRamal());
			dlkSet.add(new Key<Line>(Line.class, l.getId()));
		}
		ofy = ObjectifyService.begin();
    	Query<Point> q2 = ofy.query(Point.class);
		List<Point> points = q2.list();
		assertEquals(86, points.size());
		ofy = ObjectifyService.begin();
    	Query<TrainNode> q3 = ofy.query(TrainNode.class);
		List<TrainNode> tns = q3.list();
		assertEquals(6, tns.size());
		ofy = ObjectifyService.begin();
    	Query<PlanQuadrat> q4 = ofy.query(PlanQuadrat.class);
		List<PlanQuadrat> pqs = q4.list();
		assertEquals(16, pqs.size());
		List<Key<PlanQuadrat>> pqs_with_dlk = new LinkedList<Key<PlanQuadrat>>();
		for(PlanQuadrat pq : pqs) {
			if(pq.getDirectLineKeys().size() > 0) {
				pqs_with_dlk.add(new Key<PlanQuadrat>(PlanQuadrat.class, pq.getId()));
			}
		}
		assertEquals(16, pqs_with_dlk.size());
		HashSet<String> mapBusKey = new HashSet<String>();
		for(Key<PlanQuadrat> pq : pqs_with_dlk) {
			mapBusKey.add(pq.getName());
		}
		Dao.resetTrainNodes();
		WayHolder wh = new AStarImpl().aStarSearch(Utils.computeGeoCell(new com.beoui.geocell.model.Point(-34.622331,-58.380661)), Utils.computeGeoCell(new com.beoui.geocell.model.Point(-34.609899,-58.406839)), mapBusKey, dlkSet, tabuTrainSet, Dao.getInstance().getObjectify());
		org.junit.Assert.assertNotNull(wh);
		org.junit.Assert.assertEquals(13, wh.getWay().size());
		for(AStarNode asn : wh.getWay()) {
			System.err.print("\"" + asn.getGeoCell() + "\", ");
		}
		System.err.println();
		for(AStarNode asn : wh.getWay()) {
			System.err.print("\"" + asn.getOwningLine() + "\", ");
		}
    }
    
    @Test
    public void AStarTestBusThenTrain() {
    	String lineA1 = "1,Av. 9 de Julio von Süd nach Nord,bus,-34.622331,-58.380661,Av. 9 de Julio,-34.591672,-58.382378,Endstation Nord";
    	String lineA2 = "1,Av. 9 de Julio von rev Nord nach Süd,bus,-34.591672,-58.382378,Av. 9 de Julio,-34.622331,-58.380661,Endstation Nord";
    	String lineB1 = "B,Av. Rivadavia von Casa Rosada nach Westen,tren,-34.608063,-58.368044,Av. Rivadavia,Av. Rivadavia,-34.608946,-58.381391,cruce,cruce,-34.609899,-58.406839,Endstation West,Endstation West";
    	String lineB2 = "B,Av. Rivadavia rev von Westen nach Casa Rosada,tren,-34.609899,-58.406839,Endstation West,Endstation West,-34.608946,-58.381391,cruce,cruce,-34.608063,-58.368044,Av. Rivadavia,Av. Rivadavia";
    	addLine(lineA1);
    	addLine(lineA2);
    	addTrain(lineB1);
    	addTrain(lineB2);
    	Objectify ofy = ObjectifyService.begin();
    	Query<Line> q = ofy.query(Line.class);
		List<Line> lines = q.list();
		assertEquals(4, lines.size());
		HashSet<Key<Line>> dlkSet = new HashSet<Key<Line>>();
		HashSet<Key<Line>> tabuTrainSet = new HashSet<Key<Line>>();
		for(Line l : lines) {
			System.err.println(l.getId() + "\t" + l.getLinenum() + "\t" + l.getRamal());
			dlkSet.add(new Key<Line>(Line.class, l.getId()));
		}
		ofy = ObjectifyService.begin();
    	Query<Point> q2 = ofy.query(Point.class);
		List<Point> points = q2.list();
		assertEquals(70, points.size());
		ofy = ObjectifyService.begin();
    	Query<TrainNode> q3 = ofy.query(TrainNode.class);
		List<TrainNode> tns = q3.list();
		assertEquals(6, tns.size());
		ofy = ObjectifyService.begin();
    	Query<PlanQuadrat> q4 = ofy.query(PlanQuadrat.class);
		List<PlanQuadrat> pqs = q4.list();
		assertEquals(12, pqs.size());
		List<Key<PlanQuadrat>> pqs_with_dlk = new LinkedList<Key<PlanQuadrat>>();
		for(PlanQuadrat pq : pqs) {
			if(pq.getDirectLineKeys().size() > 0) {
				pqs_with_dlk.add(new Key<PlanQuadrat>(PlanQuadrat.class, pq.getId()));
			}
		}
		assertEquals(12, pqs_with_dlk.size());
		HashSet<String> mapBusKey = new HashSet<String>();
		for(Key<PlanQuadrat> pq : pqs_with_dlk) {
			mapBusKey.add(pq.getName());
		}
		Dao.resetTrainNodes();
		WayHolder wh = new AStarImpl().aStarSearch(Utils.computeGeoCell(new com.beoui.geocell.model.Point(-34.622331,-58.380661)), Utils.computeGeoCell(new com.beoui.geocell.model.Point(-34.609899,-58.406839)), mapBusKey, dlkSet, tabuTrainSet, Dao.getInstance().getObjectify());
		org.junit.Assert.assertNotNull(wh);
		org.junit.Assert.assertEquals(8, wh.getWay().size());
		for(AStarNode asn : wh.getWay()) {
			System.err.print("\"" + asn.getGeoCell() + "\", ");
		}
		System.err.println();
		for(AStarNode asn : wh.getWay()) {
			System.err.print("\"" + asn.getOwningLine() + "\", ");
		}
    }
    
    @Test
    public void AStarTestBusesOnlyAllIgnore() {
    	String lineA1 = "1,Au. Av. 9 de Julio von Süd nach Nord,bus,-34.622331,-58.380661,Au. Av. 9 de Julio,-34.591672,-58.382378,Au. Endstation Nord";
    	String lineA2 = "1,Au. Av. 9 de Julio von rev Nord nach Süd,bus,-34.591672,-58.382378,Au. Av. 9 de Julio,-34.622331,-58.380661,Au. Endstation Nord";
    	String lineB1 = "2,Au. Av. Rivadavia von Casa Rosada nach Westen,bus,-34.608063,-58.368044,Au. Av. Rivadavia,-34.609899,-58.406839,Au. Endstation West";
    	String lineB2 = "2,Au. Av. Rivadavia rev von Westen nach Casa Rosada,bus,-34.609899,-58.406839,Au. Av. Rivadavia,-34.608063,-58.368044,Au. Endstation";
    	addLine(lineA1);
    	addLine(lineA2);
    	addLine(lineB1);
    	addLine(lineB2);
    	Objectify ofy = ObjectifyService.begin();
    	Query<Line> q = ofy.query(Line.class);
		List<Line> lines = q.list();
		assertEquals(4, lines.size());
		HashSet<Key<Line>> dlkSet = new HashSet<Key<Line>>();
		HashSet<Key<Line>> tabuTrainSet = new HashSet<Key<Line>>();
		for(Line l : lines) {
			System.err.println(l.getId() + "\t" + l.getLinenum() + "\t" + l.getRamal());
			dlkSet.add(new Key<Line>(Line.class, l.getId()));
		}
		ofy = ObjectifyService.begin();
    	Query<PlanQuadrat> q4 = ofy.query(PlanQuadrat.class);
		List<PlanQuadrat> pqs = q4.list();
		assertEquals(27, pqs.size());
		List<Key<PlanQuadrat>> pqs_with_dlk = new LinkedList<Key<PlanQuadrat>>();
		for(PlanQuadrat pq : pqs) {
			if(pq.getDirectLineKeys().size() > 0) {
				pqs_with_dlk.add(new Key<PlanQuadrat>(PlanQuadrat.class, pq.getId()));
			}
		}
		assertEquals(27, pqs_with_dlk.size());
		HashSet<String> mapBusKey = new HashSet<String>();
		for(Key<PlanQuadrat> pq : pqs_with_dlk) {
			mapBusKey.add(pq.getName());
		}
		Dao.resetTrainNodes();
		WayHolder wh = new AStarImpl().aStarSearch(Utils.computeGeoCell(new com.beoui.geocell.model.Point(-34.622331,-58.380661)), Utils.computeGeoCell(new com.beoui.geocell.model.Point(-34.609899,-58.406839)),mapBusKey, dlkSet, tabuTrainSet, Dao.getInstance().getObjectify());
		org.junit.Assert.assertNull(wh);
    }
    
    private void addLine(String points) {
    	List<Point> pointsList = new ArrayList<Point>();
		String[] parts = points.split(Pattern.quote(","));
		Line l = null;
		if(parts[2].equals("bus")) {
			l = new Line(parts[0], parts[1], 1);
		} 
		HashSet<String> geoCells = new HashSet<String>();
		HashMap<String,Integer> directGeoCells = new HashMap<String,Integer>();
		HashMap<String,Boolean> ignoreCells = new HashMap<String,Boolean>();
		int start = 3; // felder 0,1,2 sind diese extradaten, ab feld 3 kommen die punkte
		for(int i = start; i<parts.length; i=i+3) {
			float a = Float.valueOf(parts[i]);
			float b = Float.valueOf(parts[i+1]);
			Point p = new Point(parts[i+2], a, b, null);
			com.beoui.geocell.model.Point _p1 = new com.beoui.geocell.model.Point(a,b);
			if(p.getStreet().substring(0, 4).equals("Au. ")) {
				p.setIgnore(true);
				geoCells.add(Utils.computeGeoCell(_p1));
			} else {
				geoCells.add(Utils.computeGeoCell(_p1));
			}
			/* i=2 and higher means that it is treating the second+ point
			   start creating interpolating points from here on
			   which means filling up the distance from the last point to the current (new) point
			   with many new points i.e. every 100m, unless type is not "bus" */
			if(i>=start+2) {
				Point lastPoint = pointsList.get(pointsList.size()-1);
				if(parts[2].equals("bus")) {
					GeoPt vector = Utils.vectorBetween(p, lastPoint);
					double distance = Utils.distanceApprox(vector.getLatitude(), vector.getLongitude());
					double distanceM = distance*Utils.distanceMultiplikator;
					long times = Math.round(distanceM/100.0);
//					Logger.getLogger(functionName).log(Level.INFO, functionName + ": Times: " + times);
//					System.err.println("lastPoint: " + lastPoint.getLatlon().getLatitude() + ", " + lastPoint.getLatlon().getLongitude());
					for(int j = 1; j<times; j++) {
						Point intermediate = new Point(lastPoint.getStreet(), (vector.getLatitude()/times)*j+lastPoint.getLatlon().getLatitude(),(vector.getLongitude()/times)*j+lastPoint.getLatlon().getLongitude(), false, true, null);
						com.beoui.geocell.model.Point _p2 = new com.beoui.geocell.model.Point(intermediate.getLatlon().getLatitude(),intermediate.getLatlon().getLongitude());
						String currentCell = Utils.computeGeoCell(_p2);
						intermediate.setIndex(pointsList.size());
						if(!lastPoint.isIgnore()) {
							pointsList.add(intermediate);
							geoCells.add(Utils.computeGeoCell(_p2));
							ignoreCells.put(currentCell, new Boolean(false));
						} else {
							geoCells.add(currentCell);
							if(!ignoreCells.containsKey(currentCell)) {
								ignoreCells.put(currentCell, new Boolean(true));
							}
						}
						directGeoCells.put(currentCell, intermediate.getIndex());
					}
				}
				lastPoint.setNextMainPointIndex(pointsList.size());
			}
			p.setIndex(pointsList.size());
			if(parts[2].equals("bus")) {
				directGeoCells.put(Utils.computeGeoCell(_p1),p.getIndex());
				if(!p.isIgnore()) {
					ignoreCells.put(Utils.computeGeoCell(_p1), new Boolean(false));
				} else {
					if(!ignoreCells.containsKey(Utils.computeGeoCell(_p1))) {
						ignoreCells.put(Utils.computeGeoCell(_p1), new Boolean(true));
					}
				}
			}
			pointsList.add(p);
		}
		Objectify ofy = Dao.getInstance().getObjectify();
		ManagementDao.getInstance().addLine(l, ofy);
		ManagementDao.getInstance().setPointsForLine(l, pointsList, ofy);
		ManagementDao.getInstance().addOrUpdatePlanQuadrats(geoCells, directGeoCells, l, ignoreCells, new HashMap<String,Boolean>(), true);
    }
    
    private void addTrain(String points) {
    	String[] parts = points.split(Pattern.quote(","));
		Line l = null;
		List<Point> pointsList = new ArrayList<Point>();
		if(parts[2].equals("subte")) {
			if(parts[0].contains("Metrobus")) {
				l = new Line(parts[0], parts[1], 13);
			} else if(parts[0].contains("Premetro")) {
				l = new Line(parts[0], parts[1], 15);
			} else {
				l = new Line(parts[0], parts[1], 11);
			}
		} else if(parts[2].equals("tren")) {
			l = new Line(parts[0], parts[1], 21);
		}
		Objectify ofy = Dao.getInstance().getObjectify();
		ManagementDao.getInstance().addLine(l, ofy);
		TrainNode lastNode = null;
//		Key knLast = null;
		int start = 3; // felder 0,1,2 sind diese extradaten, ab feld 3 kommen die punkte
		for(int i = start; i<parts.length; i=i+4) {
			float a = Float.valueOf(parts[i]);
			float b = Float.valueOf(parts[i+1]);
			Point p = new Point(parts[i+2], a, b, new Key<Line>(Line.class, l.getId()));
			com.beoui.geocell.model.Point _p = new com.beoui.geocell.model.Point(a,b);
			String cell = Utils.computeGeoCell(_p);
			TrainNode n = new TrainNode(cell, cell, parts[i+3], parts[i+2], new Key<Line>(Line.class, l.getId()), l.getType(), pointsList.size(), Dao.getRootEntityTrain());
			Key<TrainNode> kN = ManagementDao.getInstance().addTrainNode(n);
			if(lastNode != null) {
				lastNode.setNextNode(kN);
				ManagementDao.getInstance().updateTrainNode(lastNode);
//				n.addConnectedNode(knLast);
//				Dao.getInstance().addOrUpdateTrainNode(n);
			}
			lastNode = n;
			if(i>=start+2) {
				Point lastPoint = pointsList.get(pointsList.size()-1);
				lastPoint.setNextMainPointIndex(pointsList.size());
			}
			p.setIndex(pointsList.size());
			pointsList.add(p);
		}
		ManagementDao.getInstance().safePoints(pointsList, ofy);
    }

}
