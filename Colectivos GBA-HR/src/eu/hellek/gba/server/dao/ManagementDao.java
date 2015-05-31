package eu.hellek.gba.server.dao;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.Query;
import com.googlecode.objectify.util.DAOBase;

import eu.hellek.gba.model.Line;
import eu.hellek.gba.model.PQA;
import eu.hellek.gba.model.PlanQuadrat;
import eu.hellek.gba.model.Point;
import eu.hellek.gba.model.TrainNode;
import eu.hellek.gba.server.utils.Utils;

public class ManagementDao extends DAOBase {
	
	private static ManagementDao instance;
	@SuppressWarnings("unused")
	private static Dao daoInstance;
	private static String location = "ManagementDao";

	public static synchronized ManagementDao getInstance() {
		if(instance == null) {
			daoInstance = Dao.getInstance();
			instance = new ManagementDao();
		}
		return instance;
	}

	public Key<Line> addLine(Line l, Objectify ofy) {
		return ofy.put(l);
	}
	
	public void setPointsForLine(Line l, List<Point> pointsList, Objectify ofy) {
		if(l.getId() == 0) {
			System.err.println("error: line-id = 0");
		}
		Key<Line> lineKey = new Key<Line>(Line.class, l.getId());
		for(Point p : pointsList) {
			p.setOwner(lineKey);
		}
		safePoints(pointsList, ofy);
	}

	public void safePoints(List<Point> pointsList, Objectify ofy) {
		ofy.put(pointsList);
	}
	
	public synchronized void addOrUpdatePlanQuadrats(Set<String> geoCells, HashMap<String,Integer> directGeoCells, Line l, HashMap<String,Boolean> ignoreCells, HashMap<String,Boolean> twowayCells, boolean isMainLine) {
		String functionName = "addOrUpdatePlanQuadrats()";
		Objectify ofy1 = ObjectifyService.beginTransaction();
		Iterator<String> iter1 = geoCells.iterator();
		try {
			while(iter1.hasNext()) {
				PlanQuadrat pq = null;
				String geoCell = iter1.next();
/*				Query<PlanQuadrat> q = ofy.query(PlanQuadrat.class).filter("geoCell", geoCell);
				PlanQuadrat res = q.get();*/
				PlanQuadrat res = ofy1.find(new Key<PlanQuadrat>(Dao.getRootEntityBus(), PlanQuadrat.class, geoCell));
				if(res != null) {
					pq = res;
				} else {
					pq = new PlanQuadrat(geoCell, Dao.getRootEntityBus());
				}
				if(directGeoCells.containsKey(geoCell)) {
					Key<Line> lineKey = new Key<Line>(Line.class, l.getId());
					boolean twoway = false;
					if(twowayCells.containsKey(geoCell)) {
						twoway = true;
					}
					pq.addDirectLineKey(lineKey, directGeoCells.get(geoCell));
					if(isMainLine) {
						pq.addMainLineKey(lineKey, ignoreCells.get(geoCell), twoway);
					}
				}
				ofy1.put(pq);
			}
			ofy1.getTxn().commit();
		} finally {
			if (ofy1.getTxn().isActive()) {
		        ofy1.getTxn().rollback();
		        removeLine(l, ObjectifyService.begin());
		        Logger.getLogger(location).log(Level.SEVERE, functionName + ": Transaction failed for Line: " + l);
			}
		}
		
		Objectify ofy2 = ObjectifyService.beginTransaction();
		Iterator<String> iter2 = geoCells.iterator();
		try {
			while(iter2.hasNext()) {
				String geoCell = iter2.next();
				if(directGeoCells.containsKey(geoCell) && isMainLine) {
					PQA pq = null;
					PQA res = ofy2.find(new Key<PQA>(Dao.getRootEntityPQA(), PQA.class, geoCell));
					if(res != null) {
						pq = res;
					} else {
						pq = new PQA(geoCell, Dao.getRootEntityPQA());
					}

					Key<Line> lineKey = new Key<Line>(Line.class, l.getId());
					boolean twoway = false;
					if(twowayCells.containsKey(geoCell)) {
						twoway = true;
					}
					pq.addLine(lineKey, directGeoCells.get(geoCell), ignoreCells.get(geoCell), twoway);
					ofy2.put(pq);
				}
			}
			ofy2.getTxn().commit();
		} finally {
			if (ofy2.getTxn().isActive()) {
		        ofy2.getTxn().rollback();
		        removeLine(l, ObjectifyService.begin());
		        Logger.getLogger(location).log(Level.SEVERE, functionName + ": Transaction failed for Line: " + l);
			}
		}
	}
	
	public synchronized Key<TrainNode> addTrainNode(TrainNode tn) {
		String functionName = "addTrainNode()";
		Key<TrainNode> resKey = null;
		Objectify ofy = ObjectifyService.beginTransaction();
		try {
			Query<TrainNode> q = ofy.query(TrainNode.class).filter("uniqueName",  tn.getUniqueName()).ancestor(Dao.getRootEntityTrain());
			List<TrainNode> samestation = q.list();
			if(samestation.size() > 0) {
				if(Utils.distanceBetweenGeoCells(samestation.get(0).getGeoCell(), tn.getGeoCell()) <= 10) {
					tn.setGeoCell(samestation.get(0).getGeoCell());
					Logger.getLogger(location).log(Level.WARNING, functionName + ": joined/moved station " + tn.getUniqueName() + " for " + tn.getLineKey());
				} else {
					Logger.getLogger(location).log(Level.SEVERE, functionName + ": did not move station " + tn.getUniqueName() + " for " + tn.getLineKey() + " because distance is too big: " + Utils.distanceBetweenGeoCells(samestation.get(0).getGeoCell(), tn.getGeoCell()));
				}
			}
			resKey = ofy.put(tn);
			ofy.getTxn().commit();
		} finally {
			if (ofy.getTxn().isActive()) {
		        ofy.getTxn().rollback();
		        removeLine(Dao.getInstance().getLineByKey(tn.getLineKey(), ObjectifyService.begin()), ObjectifyService.begin());
		        Logger.getLogger(location).log(Level.SEVERE, functionName + ": Transaction failed for Line: " + tn.getLineKey());
			}
		}
		return resKey;
	}
	
	public synchronized void updateTrainNode(TrainNode tn) {
		String functionName = "updateTrainNode";
		Objectify ofy = ObjectifyService.beginTransaction();
		try {
			ofy.put(tn);
			ofy.getTxn().commit();
		} finally {
			if (ofy.getTxn().isActive()) {
		        ofy.getTxn().rollback();
		        removeLine(Dao.getInstance().getLineByKey(tn.getLineKey(), ObjectifyService.begin()), ObjectifyService.begin());
		        Logger.getLogger(location).log(Level.SEVERE, functionName + ": Transaction failed for Line: " + tn.getLineKey());
			}
		}
	}
	
	private synchronized void deleteLineFromPlanQuadrats(Line l) {
		Key<Line> lineKey = new Key<Line>(Line.class, l.getId());
		String functionName = "deleteLineFromPlanQuadrats";
		Objectify ofy1 = ObjectifyService.beginTransaction();
		try {
			Query<PlanQuadrat> q = ofy1.query(PlanQuadrat.class).filter("directLineKeys", lineKey).ancestor(Dao.getRootEntityBus());
			List<PlanQuadrat> pqs = q.list();
			for(PlanQuadrat pq : pqs) {
				pq.removeLine(lineKey);
				if(pq.getDirectLineKeys().size() > 0) {
					ofy1.put(pq);
				} else {
					ofy1.delete(pq);
				}
			}
			ofy1.getTxn().commit();
		} finally {
			if (ofy1.getTxn().isActive()) {
		        ofy1.getTxn().rollback();
		        Logger.getLogger(location).log(Level.SEVERE, functionName + ": Transaction failed for Line: " + l);
			}
		}
		
		Objectify ofy2 = ObjectifyService.beginTransaction();
		try {
			Query<PQA> q = ofy2.query(PQA.class).filter("lineKeys", lineKey).ancestor(Dao.getRootEntityPQA());
			List<PQA> pqs = q.list();
			for(PQA pq : pqs) {
				pq.removeLine(lineKey);
				if(pq.getLineKeys().size() > 0) {
					ofy2.put(pq);
				} else {
					ofy2.delete(pq);
				}
			}
			ofy2.getTxn().commit();
		} finally {
			if (ofy2.getTxn().isActive()) {
		        ofy2.getTxn().rollback();
		        Logger.getLogger(location).log(Level.SEVERE, functionName + ": Transaction failed for Line: " + l);
			}
		}
	}
	
	public synchronized void removeLine(Line l, Objectify ofy) {
		deleteLineFromPlanQuadrats(l);
		deletePointsForLine(l, ofy);
		deleteTrainNodesForLine(l, ofy);
		ofy.delete(l);
	}
	
	private synchronized void deletePointsForLine(Line l, Objectify ofy) {
		Query<Point> q = ofy.query(Point.class).ancestor(l);
		List<Key<Point>> keys = q.listKeys();
		ofy.delete(keys);
	}
	
	private synchronized void deleteTrainNodesForLine(Line l, Objectify ofy) {
		Query<TrainNode> q = ofy.query(TrainNode.class).filter("lineKey", new Key<Line>(Line.class, l.getId()));
		List<Key<TrainNode>> keys = q.listKeys();
		ofy.delete(keys);
	}
	
	public List<Line> getLines(Objectify ofy) {
        Query<Line> q = ofy.query(Line.class).order("linenum");
		List<Line> lines = q.list();
		return lines;
	}
	
	public synchronized boolean checkIfLineExists(Line l, Objectify ofy) {
        Query<Line> q = ofy.query(Line.class).filter("linenum", l.getLinenum()).filter("ramal", l.getRamal()).filter("type", l.getType());
		List<Line> lines = q.list();
		if(lines.size() > 0) {
			return true;
		} else {
			return false;
		}
	}
	
	public List<Key<Line>> getAllTrainKeys(Objectify ofy) {
		Query<Line> q = ofy.query(Line.class).filter("type >", 10);
		List<Key<Line>> keys = q.listKeys();
		return keys;
	}
	
}
